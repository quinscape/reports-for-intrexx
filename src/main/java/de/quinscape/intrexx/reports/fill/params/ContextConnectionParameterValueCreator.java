/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.fill.params;

import java.sql.Connection;

import de.uplanet.jdbc.JdbcConnection;

import de.quinscape.intrexx.reports.ReportContext;
import de.quinscape.intrexx.reports.domain.IntrexxApplicationReport;

import net.sf.jasperreports.engine.JRParameter;

/**
 * Ein {@link Parametriser} der für Reportparameter die mit dem
 * {@link #CONNECTION_PREFIX} beginnen, versucht eine Datenbankverbindung
 * mit dem Parameternamen ohne Prefix aus dem Kontext zu holen.
 * 
 * @author Jörg Gottschling
 */
public class ContextConnectionParameterValueCreator
    implements ParameterValueCreator
{

  /**
   * Der Prefix auf den dieser
   * {@link ContextConnectionParameterValueCreator} standardmässig
   * horcht.
   */
  public static final String CONNECTION_PREFIX = "CONNECTION_";

  @Override
  public ReportParameterValueHolder createParameterValue(ReportContext context,
      IntrexxApplicationReport report,
      JRParameter parameter)
  {
    ReportParameterValueHolder result = new ReportParameterValueHolder();
    if(parameter.getName().startsWith(CONNECTION_PREFIX)
       && parameter.getName().length() > 11)
    {
      Connection tmp = context.getDbConnection(parameter.getName().substring(11));
      if(tmp instanceof JdbcConnection)
        tmp = ((JdbcConnection)tmp).getNativeConnection();
      result.setValue(tmp);
    }
    return (result.hasValue() ? result : null);
  }
}
