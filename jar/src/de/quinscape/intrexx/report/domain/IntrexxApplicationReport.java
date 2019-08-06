/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain;

import java.io.*;
import java.util.*;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.*;

import de.quinscape.intrexx.report.*;

/**
 * Ein {@link Report} welches aus der Intrexx-Applikation "Reports für
 * Intrexx" geladen wurde.
 * 
 * @author Jörg Gottschling
 */
public class IntrexxApplicationReport
    extends AbstractReport
{

  private File mainReport;

  /**
   * Der Name der Standard-Datenbankverbindung, über die dieser Report
   * befüllt werden soll.
   */
  private String defaultConnection;

  private Map<String, File> resources;

  @Override
  public JasperReport getMainJasperReport()
  {
    try
    {
      return (JasperReport)JRLoader.loadObject(mainReport);
    }
    catch(JRException exc)
    {
      throw new IntrexxReportException(exc);
    }
  }

  /**
   * Der Hauptbericht als Datei.
   */
  public void setMainReport(File mainReport)
  {
    this.mainReport = mainReport;
  }

  @Override
  public String toString()
  {
    return getClass().getName() + "[guid=" + getGuid() + ", displayName=\""
           + getDisplayName() + "\", mainReport=" + mainReport + "]";
  }

  /**
   * Setzt den Namen der Standard-Datenbankverbindung, über die dieser
   * Report befüllt werden soll.
   * 
   * @param defaultConnection
   *          Der Name der Verbindung, z.B. "IxSysDb".
   */
  public void setDefaultConnection(String defaultConnection)
  {
    this.defaultConnection = defaultConnection;
  }

  /**
   * Liefert den Namen der Standard-Datenbankverbindung, über die dieser
   * Report befüllt werden soll.
   * 
   * @return Den Namen der Verbindung, z.B. "IxSysDb".
   */
  @Override
  public String getDefaultConnection()
  {
    return defaultConnection;
  }

  @Override
  public boolean knowsConnectionName()
  {
    return defaultConnection != null;
  }

  @Override
  public boolean hasMainReport()
  {
    return mainReport != null;
  }

  /**
   * Setzt die Ressourcen dieses Reports.
   * 
   * @param resources
   *          Eine {@link Map}, die die Namen der Ressourcen (zum Beispiel
   *          <code>&quot;Bild.png&quot;</code> auf die tatsächliche Datei (zum Beispiel
   *          <code>Bild_0.png</code>) abbildet.
   */
  public void setResources(Map<String, File> resources)
  {
    this.resources = resources;
  }

  /**
   * Liefert die Ressourcen, die dieser Report nutzt.
   * 
   * @return Eine {@link Map}, die die Namen der Ressourcen (zum Beispiel
   *         <code>&quot;Bild.png&quot;</code> auf die tatsächliche Datei (zum Beispiel
   *         <code>Bild_0.png</code>) abbildet.
   */
  public Map<String, File> getResources()
  {
    return resources;
  }

}
