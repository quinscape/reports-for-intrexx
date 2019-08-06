/*
 * (C) Copyright 2012 QuinScape GmbH
 * All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain.fill;

import static org.apache.commons.lang.time.DateUtils.UTC_TIME_ZONE;

import java.sql.*;
import java.util.*;
import java.util.Date;

import net.sf.jasperreports.engine.*;

import org.apache.commons.logging.*;

import de.uplanet.lucy.server.businesslogic.util.*;
import de.uplanet.lucy.server.rtcache.*;
import de.uplanet.lucy.server.rtcache.filter.*;
import de.uplanet.lucy.server.util.db.*;
import de.uplanet.lucy.types.*;
import de.uplanet.util.*;
import de.uplanet.util.filter.*;

import de.quinscape.intrexx.report.ReportContext;
import de.quinscape.intrexx.report.domain.*;

/**
 * Ein {@link ParameterValueCreator} der aus dem aktuellen Datensatz (mit Hilfe der RecId) den
 * aktuellen Wert für das Datenfeld aus der Datenbank holt. Der Reportparameter muss benannt sein
 * wie die Datenbankspalte, der SysIdent oder die Guid.
 * 
 * Datengruppen mit zusammengesetztem Primärschlüssel werden bisher nicht unterstützt!
 * 
 * TODO Datenbanktest
 * 
 * @author Markus Vollendorf
 * @author Nils Berger
 */
public class DatabaseParameterValueCreator
    implements ParameterValueCreator
{

  /**
   * Logger for this class
   */
  private static final Log log = LogFactory.getLog(DatabaseParameterValueCreator.class);

  /**
   * Kalender mit UTC-Zeitzone für das setzen von Timestamp-Werten in
   * {@link #setPreparedStatementValue(DbPreparedStatement, Object, Calendar, FieldInfo, int)}.
   */
  private static final Calendar utcCal = new GregorianCalendar(UTC_TIME_ZONE);

  /**
   * Erzeugt eine Instanz von <code>DatabaseParameterValueCreator</code>.
   * 
   */
  public DatabaseParameterValueCreator()
  {
    super();
  }

  @Override
  public ReportParameterValueHolder createParameterValue(ReportContext context, Report report,
      JRParameter parameter)
  {

    // Rückgabewert
    ReportParameterValueHolder value = new ReportParameterValueHolder();

    // Betroffener Datensatz
    IDataRecord ixDatarecord = context.ix().internalPeekRecord();
    if(ixDatarecord != null)
    {

      // Suchen des Feldes im Intrexx RtCache
      final String dgGuid = ixDatarecord.getDataGroupGuid();
      final String jrParamName = parameter.getName();
      FieldInfo qsDataField = RtCache.getFirstField(new IFilter<FieldInfo>()
      {

        @Override
        public boolean accept(FieldInfo f)
        {
          return f.getDataGroupGuid().equals(dgGuid)
                 && (jrParamName.equals(f.getSysIdent())
                     || jrParamName.equalsIgnoreCase(f.getColumnName()) || jrParamName.equalsIgnoreCase(f.getGuid()));
        }
      });

      // Datenfeld existiert
      if(qsDataField != null)
      {
        // PK-Feld ermitteln im RtCache
        List<FieldInfo> pkFields =
            RtCache.getPrimaryKeyFields(new FieldInfoDgGuidFilter(ixDatarecord.getDataGroupGuid()));
        if(pkFields.size() != 1)
        {
          // TODO: zusammengesetzte Primärschlüssel werden nicht unterstützt
          log.error("Resolving values from datarecord is not supported for "
                    + pkFields.size() + " primary key fields.");
        }
        else
        {
          // Datenbankwert holen
          FieldInfo pkField = pkFields.get(0);
          String query =
              "SELECT " + qsDataField.getColumnName() + " FROM " + qsDataField.getTableName()
                  + " WHERE " + pkField.getColumnName() + " = ?";
          DbQuery dbQuery = null;
          DbPreparedStatement stmt = null;
          try
          {
            dbQuery = new DbQuery();
            stmt = dbQuery.prepare(context.ix().getConnection(), query);
            setPreparedStatementValue(stmt, ixDatarecord.getRecId(), utcCal, pkField, 1);
            value.setValue(stmt.executeAndGetScalarValue());
          }
          catch(SQLException exc)
          {
            log.error(
                "Can't read value for datafield " + qsDataField.getName() + "("
                    + qsDataField.getGuid() + ") from Database.", exc);
          }
          finally 
          {
            Safely.close(stmt);
            Safely.close(dbQuery);
          }
        }
      }
    }

    // Rückgabe
    return (value.hasValue() ? value : null);
  }

  private void setPreparedStatementValue(DbPreparedStatement pst, Object value, Calendar cal,
      FieldInfo field,
      int position)
      throws SQLException
  {
    UP_TYPE datatype = field.getDataType();
    if(datatype == UP_TYPE.DATETIME && value != null)
    {
      Timestamp dtVal = new Timestamp(((Date)value).getTime());
      pst.setTimestamp(position, dtVal, (cal == null) ? utcCal : cal);
    }
    else if((datatype.SQL == UP_TYPE.INTEGER.SQL)
            && value != null
            && (value instanceof String || value instanceof Number))
    {
      // Diese Vorgabe kann eigentlich gar nicht zum tragen kommen, da es
      // String oder Number sein MUSS, wenn er hier vorbei kommt
      int intValue = 0;
      if(value instanceof String) intValue = Integer.parseInt((String)value);
      if(value instanceof Number) intValue = ((Number)value).intValue();
      pst.setInt(position, intValue);
    }
    else if((datatype.SQL == UP_TYPE.DOUBLE.SQL)
            && value != null
            && (value instanceof String || value instanceof Number))
    {
      // Diese Vorgabe kann eigentlich gar nicht zum tragen kommen, da es
      // String oder Number sein MUSS, wenn er hier vorbei kommt
      float floatValue = 0;
      if(value instanceof String) floatValue = Float.parseFloat((String)value);
      if(value instanceof Number) floatValue = ((Number)value).floatValue();
      pst.setFloat(position, floatValue);
    }
    else if(value instanceof CharSequence)
    {
      pst.setString(position, value.toString());
    }
    else
    {
      throw new SQLException("Can't set value '" + value + "' in statement. Unsupport data type.");
    }
  }

}
