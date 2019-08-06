/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain;

import java.util.*;

import net.sf.jasperreports.engine.*;

/**
 * Repräsentiert einen kompletten Bericht mit allen dazugehörigen
 * Ressourcen. Dazu gehören auch Unterberichte ("subreports"). Enthält alle
 * (Meta-)Informationen über diesen Bericht. Darf keinen Anwendungsstatus
 * speichern, d.h. es muss "stateless" und "thread safe" sein wie ein
 * Service.
 * 
 * @author Jörg Gottschling
 */
public interface Report
{

  /**
   * GUID des Berichts.
   */
  String getGuid();

  /**
   * Anzeige Name des Berichts.
   */
  String getDisplayName();

  /**
   * Dieser Bericht als {@link JasperReport}, also die Hauptressource.
   */
  JasperReport getMainJasperReport();
  
  /**
   * Hat dieser Bericht einen Hauptreport zugeordnet?
   */
  boolean hasMainReport();

  /**
   * Zeigt an, ob dieser Bericht eine eigens konfigurierte
   * Datenbankverbindung braucht, die ihm als Eigenschaft mitgegeben wurde.
   * 
   * @return <code>true</code>, falls ein Verbindungsname konfiguriert
   *         wurde, <code>false</code> sonst.
   */
  boolean knowsConnectionName();

  /**
   * Liefert den Namen der Standard-Datenbankverbindung für diesen Report.
   * Reports, die nicht aus der Intrexx-Anwendung "Reports für Intrexx"
   * stammen, mögen nicht über eine Intrexx-Datenbankverbindung befüllt
   * werden. "Standard" bezieht sich auf den Report, nicht auf Intrexx.
   * 
   * @return Den Namen der Verbindung, oder <code>null</code>, falls keine
   *         konfiguriert wurde (in diesem Fall liefert
   *         {@link #knowsConnectionName()} <code>false</code>).
   */
  String getDefaultConnection();

  /**
   * Menge der GUIDs von Organisationsobjekten aus dem Verzeichnisdienst von
   * Intrexx denen oder deren Mitgliedern es erlaubt ist diesen Bericht zu
   * erzeugen. Kann die GUIDs von Benutzern, Gruppen,
   * Organisationseinheiten, Verteilerlisten und beliebigen anderen Objekten
   * aus der "Benutzerverwaltung" enthalten.
   */
  Set<String> getPermittedOrgGuids();

}
