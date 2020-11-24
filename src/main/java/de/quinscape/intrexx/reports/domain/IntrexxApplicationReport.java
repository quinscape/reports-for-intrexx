/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.domain;

import java.io.File;
import java.util.Map;
import java.util.Set;

import de.quinscape.intrexx.reports.IntrexxReportException;
import de.quinscape.intrexx.reports.dao.IntrexxDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * Ein Bericht, der aus der Intrexx-Applikation "Reports für Intrexx" geladen wurde.
 * 
 * @author Jörg Gottschling
 */
public class IntrexxApplicationReport
{

  private File mainReport;

  private Map<String, File> resources;

  private IntrexxDataSource datasource;

  private String displayName;

  private String guid;

  private Set<String> permittedOrgGuids;

  /**
   * Liefert den als Hauptbericht gekennzeichneten Bericht.
   * 
   * @return Den Hauptbericht als {@link JasperReport}.
   */
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
   * Gibt an, ob dieser Intrexx-Bericht einen Hauptbericht konfiguriert hat.
   * 
   * @return <code>true</code>, falls ein Hauptbericht konfiguriert ist, <code>false</code> sonst.
   */
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

  /**
   * Legt die {@link IntrexxDataSource} für diesen Bericht fest, das ist die in der Applikation
   * Reports für Intrexx definierte Datenquelle.
   * 
   * @param datasource
   *          Die {@link IntrexxDataSource}.
   */
  public void setDataSource(IntrexxDataSource datasource)
  {
    this.datasource = datasource;
  }

  /**
   * Liefert die {@link IntrexxDataSource}.
   * 
   * @see #setDataSource(IntrexxDataSource)
   * @return Die konfigurierte {@link IntrexxDataSource}.
   */
  public IntrexxDataSource getDataSource()
  {
    return this.datasource;
  }

  /**
   * Liefert den anzuzeigenden Namen dieses Berichts, zum Beispiel <code>"Telefonliste"</code>.
   * 
   * @return Der Name des Berichts.
   */
  public String getDisplayName()
  {
    return displayName;
  }

  /**
   * Liefert die GUID des Berichts, zum Beispiel
   * <code>"4D8C1B707B124036BF64A8E3EAF242CC03E39108"</code>.
   * 
   * @return Die GUID des Berichts.
   */
  public String getGuid()
  {
    return guid;
  }

  /**
   * Liefert die Menge der Benutzerverzeichnis-Objekte, die Zugriff auf diesen Bericht haben.
   * 
   * @return Die Menge der GUIDs der Benutzerverzeichnis-Objekte.
   */
  public Set<String> getPermittedOrgGuids()
  {
    return permittedOrgGuids;
  }

  /**
   * Legt den anzuzeigenden Namen des Berichts fest.
   * 
   * @see #getDisplayName()
   * @param displayName
   *          Der anzuzeigende Name.
   */
  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  /**
   * Legt die GUID dieses Berichts fest.
   * 
   * @param guid
   *          Die GUID, zum Beispiel <code>"4D8C1B707B124036BF64A8E3EAF242CC03E39108"</code>.
   * @see #getGuid()
   */
  public void setGuid(String guid)
  {
    this.guid = guid;
  }

  /**
   * Legt die Menge der Benutzerverzeichnis-Objekte fest, die Zugriff auf diesen Bericht haben
   * sollen.
   * 
   * @param permittedOrgGuids
   *          Die GUIDs der Benutzerverzeichnis-Objekte.
   * @see #getPermittedOrgGuids()
   */
  public void setPermittedOrgGuids(Set<String> permittedOrgGuids)
  {
    this.permittedOrgGuids = permittedOrgGuids;
  }

}
