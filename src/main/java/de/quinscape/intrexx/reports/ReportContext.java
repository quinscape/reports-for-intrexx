/*
 * (C) Copyright 2008 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports;

import java.util.*;

import de.uplanet.jdbc.*;
import de.uplanet.lucy.server.*;
import de.uplanet.lucy.server.businesslogic.*;
import de.uplanet.lucy.server.connector.*;
import de.uplanet.lucy.server.engine.http.*;
import de.uplanet.lucy.server.session.*;
import de.uplanet.lucy.server.usermanager.*;

import de.quinscape.intrexx.reports.domain.*;

import net.sf.jasperreports.engine.*;

/**
 * Enthält den Context.
 * 
 * @author Jörg Gottschling
 */
public interface ReportContext
    extends net.sf.jasperreports.engine.ReportContext
{

  /**
   * Bequeme Abkürzung um an den UP-Intrexx-Kontext zu gelangen.
   * 
   * @return Liefert den aktuellen {@link IBusinessLogicProcessingContext}.
   */
  IBusinessLogicProcessingContext ix();

  /**
   * Bequeme Abkürzung um an den JasperReports-Kontext zu gelangen.
   * 
   * @return Liefert den aktuellen {@link net.sf.jasperreports.engine.ReportContext}.
   */
  net.sf.jasperreports.engine.ReportContext jr();

  /**
   * Veränderliche {@link Map} von {@link String} auf {@link Object} (wie
   * von Jasper Reports definiert) mit den Parametern zur Befüllung eines
   * Berichts. Die Hauptparameter werden zwar erst beim Füllen geschrieben,
   * aber einige gegebenenfalls schon vorher gesetzt.
   * 
   * @see JRParameter
   */
  Map<String, Object> getReportParameters();

  /**
   * Setzt den {@link IntrexxApplicationReport} der in diesem Context erzeugt wird.
   */
  void setReport(IntrexxApplicationReport report);

  /**
   * Liefert den {@link IntrexxApplicationReport} der in diesem Context erzeugt wird. Kann je
   * nach Phase <code>null</code> sein.
   */
  IntrexxApplicationReport getReport();

  /**
   * Liefert den aktuellen Request. Liefert <code>null</code> wenn nicht
   * vorhanden.
   */
  IServerBridgeRequest getRequest();

  /**
   * Liefert die aktuelle Session. Liefert <code>null</code> wenn nicht
   * vorhanden.
   */
  ISession getSession();

  /**
   * Liefert den aktuellen Benutzer. Liefert <code>null</code> wenn nicht
   * vorhanden. Es handelt sich um eine vereinfachende Methode, da der
   * Benutzer auch der Session entnommen werden könnte.
   */
  IUser getUser();

  /**
   * Liefert die aktuelle, an diesen Thread/Request gebundene
   * Datenbankverbindung zur Systemdatenbank. Liefert <code>null</code> wenn nicht vorhanden.
   * <p>
   * Bitte die direkte Benutzung von JDBC vermeiden!
   */
  JdbcConnection getDbConnection();

  /**
   * Liefert die aktuelle, an diesen Thread/Request gebundene
   * Datenbankverbindung zur spezifizierten Datenbank. Liefert <code>null</code> wenn nicht
   * vorhanden.
   * <p>
   * Bitte die direkte Benutzung von JDBC vermeiden!
   * 
   * @param name
   *          Der Name der Datenbankverbindung, wie er im Intrexx-Portal spezifiziert wurde.
   */
  JdbcConnection getDbConnection(String name);

  /** Liefert die Standard-{@link TimeZone}. */
  TimeZone getDefaultTimeZone();

  /**
   * Liefert den zweistellige ISO-Code der Standardsprache als {@link String}.
   */
  String getDefaultLanguage();

  /**
   * Liefert den zweistellige ISO-Code der aktuellen Sprache als {@link String}. Ermittelt diesen
   * aus dem Request (vom Benutzer aktuell
   * eingestellt) oder dem Benutzer (Einstellung des Benutzer in der
   * Benutzerverwaltung). Liefert im Zweifelsfall die Standard-Sprache des
   * Portals.
   * 
   * @return Eigentlich niemals <code>null</code>.
   */
  String getLanguage();

  /**
   * Liefert den Layout-Bezeichner als {@link String}. Ermittelt diesen aus
   * dem Request oder dem Benutzer. Liefert im Zweifelsfall das
   * Standard-Layout.
   * 
   * @return Eigentlich niemals <code>null</code>.
   */
  String getLayout();

  /**
   * Liefert den aktuellen {@link IBusinessLogicProcessingContext}.
   */
  IBusinessLogicProcessingContext getBusinessLogicProcessingContext();

  /**
   * Liefert den aktuellen {@link SharedState} von UP.
   * 
   * @see ISharedStateProcessingContext#getSharedState()
   */
  SharedState getSharedState();

  /**
   * Liefert die Daten aus dem Formular der <b>Quell</b>seite als {@link IViewData} von UP. Das
   * Objekt enthält alle im Intrexx-Designer
   * mit Bordmitteln angelegt Formularfelder. Auch solche, die keinem
   * Datenfeld zugeordnet sind.
   * 
   * @return Ein {@link IViewData} aus der Intrexx-API von United Planet.
   *         Kann <code>null</code> sein, wenn im aktuellen Kontext nicht
   *         vorhanden.
   * 
   * @see IHttpProcessingContext#getViewData()
   */
  IViewData getSourceViewData();

}
