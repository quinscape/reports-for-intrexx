/*
 * (C) Copyright 2014 QuinScape GmbH
 * All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.ixbl;

import static de.quinscape.intrexx.reports.CONSTANTS.REPORTS_APPGUID;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.security.auth.Subject;

import de.uplanet.lucy.constants.*;
import de.uplanet.lucy.server.*;
import de.uplanet.lucy.server.auxiliaries.*;
import de.uplanet.lucy.server.businesslogic.*;
import de.uplanet.lucy.server.businesslogic.rtdata.*;
import de.uplanet.lucy.server.businesslogic.rtdata.jdbc.filter.*;
import de.uplanet.lucy.server.businesslogic.util.*;
import de.uplanet.lucy.server.dataobjects.*;
import de.uplanet.lucy.server.rtcache.*;
import de.uplanet.lucy.server.rtcache.filter.*;
import de.uplanet.lucy.server.spring.configuration.*;

/**
 * Stellt Methoden zum Zugriff auf die Intrexx Businesslogik (Schreiben und Lesen von Datensätzen,
 * ...) bereit.
 *
 * @author Markus Vollendorf
 */
public class IxblHelper
{

  /**
   * Verhindert die Instanziierung.
   */
  private IxblHelper()
  {
  }

  /**
   * Erstellt einen passenden Kontext.
   * 
   * @return Neuer Kontext basierend auf der {@link ContextConnection} und {@link ContextSession}.
   */
  static RestrictedBusinessLogicProcessingContext getContext()
  {
    SimpleProcessingContext c =
        new SimpleProcessingContext(IntrexxApplicationContext.getInstance(),
            ContextConnection.get(), ContextSession.get());
    return new RestrictedBusinessLogicProcessingContext(c);
  }

  /**
   * Holt den Sysident des Primärschlüssels einer Datengruppe
   * 
   * @param datagroupSysident
   *          Sysident der Datengruppe
   * @return Entsprechender Sysident
   */
  private static String getPrimaryKeySysident(String datagroupSysident)
  {
    DataGroupInfo dataGroup = getDatagroup(datagroupSysident);
    List<FieldInfo> primaryFields =
        RtCache.getPrimaryKeyFields(new FieldInfoDgGuidFilter(dataGroup.getGuid()));
    assert primaryFields.size() == 1;
    return primaryFields.get(0).getSysIdent();
  }

  /**
   * Holt einen einzelnen Datensatz.
   * 
   * @param datagroupSysident
   *          Sysident der Datengruppe
   * @param recordId
   *          ID des Datensatzes
   * @return Entsprechender Datensatz
   * @throws UncheckedBlException
   */
  public static IDataRecord getDataRecord(String datagroupSysident, String recordId)
      throws UncheckedBlException
  {
    IDataRecord dataRecord = new DataRecord(getDatagroup(datagroupSysident));
    dataRecord.setRecId(recordId);
    String primaryKey = getPrimaryKeySysident(datagroupSysident);
    // TODO Wieso braucht man das? Wie geht das sauber?
    if(!dataRecordExists(datagroupSysident, primaryKey, recordId) && !"-1".equals(recordId))
      setValue(dataRecord, primaryKey, recordId);
    dataRecord.setActionMode(ACTION.MERGE);
    return dataRecord;
  }

  /**
   * Verarbeitet einen Datensatz (Insert, Update, Delete je nach Aktion).
   * 
   * @param r
   *          Datensatz
   * @throws UncheckedBlException
   */
  public static void parseDataRecord(IDataRecord r)
      throws UncheckedBlException
  {
    try
    {
      IProcessingContext context = getContext();
      DataGroupInfo dataGroupInfo = r.getDataGroupInfo();
      IRtDataGroup datagroup = RtCreator.createDataGroup(context, dataGroupInfo);
      String datagroupSysident = dataGroupInfo.getSysIdent();
      if(dataRecordExists(datagroupSysident,
          getPrimaryKeySysident(datagroupSysident), r.getRecId()))
        datagroup.createDataUpdateHandler().updateAction(r);
      else
        datagroup.createDataUpdateHandler().createRecord(r);
    }
    catch(Exception exc)
    {
      throw new UncheckedBlException("Error in insertDataRecord.", exc);
    }
  }

  /**
   * Entnimmt aus einem {@link IDataRecord} einen typisierten Wert.
   * 
   * @param r
   *          Datensatz
   * @param fieldSysident
   *          Sysident des Feldes
   * @param returnClass
   *          Erwarteter Typ
   * @return Entsprechender Wert
   */
  public static <T> T getTypedValue(IDataRecord r, String fieldSysident, Class<T> returnClass)
  {
    IValueHolder<?> valueHolder =
        r.getValueHolderByFieldGuid(getFieldGuid(r.getDataGroupGuid(), fieldSysident));
    return getTypedValue(valueHolder, returnClass);
  }

  /**
   * Entnimmt aus einer {@link IRow} einen typisierten Wert.
   * 
   * @param r
   *          Datensatz
   * @param fieldSysident
   *          Sysident des Feldes
   * @param returnClass
   *          Erwarteter Typ
   * @return Entsprechender Wert
   */
  public static <T> T getTypedValue(IRow r, String fieldSysident, Class<T> returnClass)
  {
    IValueHolder<?> valueHolder =
        r.getValueHolder(getFieldGuid(r.getDataRange().getDataGroupGuid(), fieldSysident));
    return getTypedValue(valueHolder, returnClass);
  }

  /**
   * Versucht einen Cast des Inhalts eines {@link IValueHolder} zu einem typisierten Wert.
   * 
   * @param valueHolder
   *          Valueholder
   * @param returnClass
   *          Erwarteter Typ
   * @return Entsprechender Wert
   */
  public static <T> T getTypedValue(IValueHolder<?> valueHolder, Class<T> returnClass)
  {
    Object valueHolderValue = valueHolder == null ? null : valueHolder.getValue();
    if(valueHolderValue == null)
      return null;
    else if(returnClass.isAssignableFrom(valueHolderValue.getClass()))
      return returnClass.cast(valueHolder.getValue());
    else
      throw new AssertionError("Valueholder has type "
                               + valueHolderValue.getClass().getCanonicalName()
                               + " but requested type is " + returnClass.getCanonicalName() + ".");
  }

  /**
   * Überprüft, ob mindestens ein Datensatz zu den angegebenen Kriterien existiert.
   * 
   * @param datagroupSysident
   *          Sysident der Datengruppe
   * @param fieldSysident
   *          Sysident des Felds
   * @param fieldValue
   *          Wert des Felds
   * @return {@code true} falls mindestens ein Datensatz existiert und sonst {@code false}.
   * @throws UncheckedBlException
   */
  public static boolean dataRecordExists(String datagroupSysident, String fieldSysident,
      String fieldValue)
      throws UncheckedBlException
  {
    try
    {
      List<IRow> matchingDataRecords =
          getRows(datagroupSysident, fieldSysident, fieldValue);
      return matchingDataRecords.size() > 0;
    }
    catch(Exception exc)
    {
      throw new UncheckedBlException("Error in dataRecordExists.", exc);
    }
  }

  /**
   * Holt eine einzelne Zeile bzw. Datensatz und prüft, ob es auch nur genau eine passende Zeile
   * bzw. Datensatz gibt.
   * 
   * @param datagroupSysident
   *          Sysident der Datengruppe
   * @param fieldSysident
   *          Sysident des Felds
   * @param fieldValue
   *          Wert des Felds
   * @return Entsprechende Zeile / Datensatz
   * @throws UncheckedBlException
   */
  public static IRow getRow(String datagroupSysident, String fieldSysident, String fieldValue)
      throws UncheckedBlException
  {
    try
    {
      List<IRow> matchingDataRecords =
          getRows(datagroupSysident, fieldSysident, fieldValue);
      assert matchingDataRecords.size() == 1;
      return matchingDataRecords.get(0);
    }
    catch(Exception exc)
    {
      throw new UncheckedBlException("Error in getRow.", exc);
    }
  }

  /**
   * Holt alle zu den Kriterien passenden Zeilen / Datensätze.
   * 
   * @param datagroupSysident
   *          Sysident der Datengruppe
   * @param fieldSysident
   *          Sysident des Felds
   * @param fieldValue
   *          Wert des Felds
   * @return Entsprechende Zeilen / Datensätze
   * @throws UncheckedBlException
   */
  public static List<IRow> getRows(String datagroupSysident, String fieldSysident,
      String fieldValue)
      throws UncheckedBlException
  {
    try
    {
      String datagroupGuid = getDatagroupGuid(datagroupSysident);
      DataGroupInfo dataGroupInfo = RtCache.getDataGroup(datagroupGuid);
      IProcessingContext context = getContext();
      IRtDataGroup datagroup = RtCreator.createDataGroup(context, dataGroupInfo);
      List<String> fieldGuids = new ArrayList<String>();
      Collection<FieldInfo> allFields = RtCache.getFields(new FieldInfoDgGuidFilter(datagroupGuid));
      for(FieldInfo f : allFields)
        fieldGuids.add(f.getGuid());
      IDataRangeDataService dataRange =
          new DataRangeDataService(REPORTS_APPGUID, datagroupGuid,
              fieldGuids,
              getContext().getUserTimeZone());
      IRtFilter filter = dataRange.getFilter();
      StringBuilder filterXml =
          new StringBuilder().append("<filter>").append("<conc op=\"and\">").append(
              "  <exp op=\"=\" type=\"infix\">").append("    <arg content=\"").append(
                  getFieldGuid(datagroupGuid, fieldSysident)).append(
                      "\" type=\"fieldguid\"/>").append("    <arg content=\"").append(
                          fieldValue).append("\" type=\"value\"/>").append("  </exp>").append(
                              "</conc>").append("</filter>");
      filter.addFilter(new FilterElement(filterXml.toString()));
      dataRange.setFilter(filter);
      dataRange.setPageSize(-1);
      Subject.doAsPrivileged(de.uplanet.lucy.server.LocalSystem.INSTANCE.SUBJECT,
          new PrivilegedAction<Void>()
          {

            @Override
            public Void run()
            {
              try
              {
                datagroup.parseDataRange(dataRange, PERMISSION_CHECK.YES);
              }
              catch(Exception e)
              {
                throw new UncheckedBlException("Error in getRows.", e);
              }
              return null;
            }
          }, null);
      return dataRange.getRows();
    }
    catch(Exception exc)
    {
      throw new UncheckedBlException("Error in getRows.", exc);
    }
  }

  /**
   * Löscht alle zu den Kriterien passenden Zeilen / Datensätze.
   * 
   * @param datagroupSysident
   *          Sysident der Datengruppe
   * @param fieldSysident
   *          Sysident des Felds
   * @param fieldValue
   *          Wert des Felds
   * @throws UncheckedBlException
   */
  public static void deleteRows(String datagroupSysident, String fieldSysident, String fieldValue)
      throws UncheckedBlException
  {
    try
    {
      List<IRow> matchingRows = getRows(datagroupSysident, fieldSysident, fieldValue);
      for(IRow r : matchingRows)
      {
        IDataRecord matchingRecord = getDataRecord(datagroupSysident, r.getRecId());
        matchingRecord.setActionMode(ACTION.DELETE);
        parseDataRecord(matchingRecord);
      }
    }
    catch(Exception exc)
    {
      throw new UncheckedBlException("Error in deleteRows.", exc);
    }
  }

  private static FieldInfo getField(String datagroupGuid, String fieldSysident)
  {
    FieldInfo f = RtCache.getFirstField(new FieldInfoSysIdentFilter(datagroupGuid, fieldSysident));
    if(f == null)
      throw new NoSuchElementException("No datafield with sysident " + fieldSysident + ".");
    return f;
  }

  static String getFieldGuid(String datagroupGuid, String fieldSysident)
  {
    return getField(datagroupGuid, fieldSysident).getGuid();
  }

  private static DataGroupInfo getDatagroup(String datagroupSysident)
  {
    DataGroupInfo d = RtCache.getFirstDataGroup(
        new DataGroupSysIdentFilter(REPORTS_APPGUID, datagroupSysident));
    if(d == null)
      throw new NoSuchElementException("No datagroup with sysident " + datagroupSysident + ".");
    return d;
  }

  static String getDatagroupGuid(String datagroupSysident)
  {
    return getDatagroup(datagroupSysident).getGuid();
  }

  /**
   * Setzt einen Wert in einem Datensatz.
   * 
   * @param r
   *          Datensatz
   * @param fieldSysident
   *          Sysident des Felds
   * @param fieldValue
   *          Wert des Felds
   */
  public static void setValue(IDataRecord r, String fieldSysident, Object fieldValue)
  {
    IValueHolder<?> vh;
    if(fieldValue instanceof IValueHolder)
      vh = (IValueHolder<?>)fieldValue;
    else
      vh = ValueHolderFactory.getValueHolder(fieldValue);
    r.setValue(getFieldGuid(r.getDataGroupGuid(), fieldSysident), vh);
  }

  /**
   * Holt den zu einem Datensatz gehörenden {@link IFileValueHolder}.
   * 
   * @param datagroupSysident
   *          Sysident der Datengruppe
   * @param fieldSysident
   *          Sysident des Felds
   * @param recordId
   *          ID des Datensatzes
   * @return Entsprechender FileValueHolder
   * @throws UncheckedBlException
   */
  public static IFileValueHolder getFileValueHolder(String datagroupSysident, String fieldSysident,
      String recordId)
      throws UncheckedBlException
  {
    try
    {
      IFileValueHolder h =
          Subject.doAsPrivileged(de.uplanet.lucy.server.LocalSystem.INSTANCE.SUBJECT,
              new PrivilegedAction<IFileValueHolder>()
              {

                @Override
                public IFileValueHolder run()
                {
                  IFileValueHolder tmpFileHolder;
                  try
                  {
                    tmpFileHolder =
                        FileUCHelper.getFileValueHolder(getContext(),
                            getFieldGuid(getDatagroupGuid(datagroupSysident), fieldSysident),
                            recordId);
                  }
                  catch(Exception e)
                  {
                    throw new UncheckedBlException("Error in getFileValueHolder.", e);
                  }
                  return tmpFileHolder;
                }
              }, null);
      return h;
    }
    catch(Exception exc)
    {
      throw new UncheckedBlException("Error in getFileValueHolder.", exc);
    }
  }

  /**
   * Ersetzt die bestehende Datei an einem Datensatz durch eine neue Datei.
   * 
   * @param datagroupSysident
   *          Sysident der Datengruppe
   * @param fieldSysident
   *          Sysident des Felds
   * @param recordId
   *          ID des Datensatzes
   * @param copyFile
   *          Die zu kopierende Datei
   * @param fileName
   *          Der in Intrexx verwendete Dateiname
   * @throws UncheckedBlException
   */
  public static void setFileValueHolder(String datagroupSysident, String fieldSysident,
      String recordId, File copyFile, String fileName)
      throws UncheckedBlException
  {
    try
    {
      FileUCHelper.copyFileToIntrexx(getContext(), copyFile,
          getFieldGuid(getDatagroupGuid(datagroupSysident), fieldSysident),
          recordId, fileName, false);
    }
    catch(Exception exc)
    {
      throw new UncheckedBlException("Error in setFileValueHolder.", exc);
    }
  }

}
