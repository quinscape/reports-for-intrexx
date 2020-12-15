/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.fill.params;

import java.util.Locale;
import java.util.TimeZone;

import de.uplanet.lucy.server.businesslogic.IBusinessLogicProcessingContext;
import de.uplanet.lucy.server.common.util.ServerLocale;
import de.uplanet.lucy.server.connector.IServerBridgeRequest;
import de.uplanet.lucy.server.session.ISession;
import de.uplanet.lucy.server.usermanager.IUser;
import de.uplanet.lucy.util.ILocale;
import de.quinscape.intrexx.reports.ReportContext;
import de.quinscape.intrexx.reports.domain.IntrexxApplicationReport;

import net.sf.jasperreports.engine.JRParameter;

/**
 * Ein spezieller {@link ParameterValueCreator} der typische Objekte aus dem
 * {@link ReportContext} als Parameterwerte übergibt, wenn entsprechende
 * Parameter gesetzt sind.
 * 
 * @author Jörg Gottschling
 */
public class ContextParameterValueCreator
    implements ParameterValueCreator
{

  /**
   * Für einen Parameter mit diesem Namen wird die für den Benutzer
   * eingestellte Zeitzone als {@link TimeZone} übergeben. Liefert im
   * Zweifelsfall die Standardzeitzone des Portals.
   */
  private static final String INTREXX_USER_TIME_ZONE = "INTREXX_USER_TIME_ZONE";

  /**
   * Für einen Parameter mit diesem Namen wird der zweistellige ISO-Code der
   * aktuellen Sprache als {@link String} übergeben. Ermittelt diesen aus
   * dem Request (vom Benutzer aktuelle umgestellt) oder dem Benutzer
   * (Einstellung des Benutzer in der Benutzerverwaltung). Liefert im
   * Zweifelsfall die Standard-Sprache des Portals.
   */
  public static final String INTREXX_LANGUAGE = "INTREXX_LANGUAGE";

  /**
   * Für einen Parameter mit diesem Namen wird die Intrexx-Session als
   * {@link ISession} übergeben, sofern vorhanden.
   */
  public static final String INTREXX_SESSION = "INTREXX_SESSION";

  /**
   * Für einen Parameter mit diesem Namen wird der komplette Kontext des
   * aktuellen Berichts als {@link ReportContext} übergeben.
   */
  public static final String REPORT_CONTEXT = "REPORT_CONTEXT";

  /**
   * Für einen Parameter mit diesem Namen wird der komplette Intrexx-Kontext
   * des aktuellen Berichts als {@link IBusinessLogicProcessingContext}
   * übergeben.
   */
  public static final String INTREXX_CONTEXT = "INTREXX_CONTEXT";

  /**
   * Für einen Parameter mit diesem Namen wird der aktuelle Intrexx-Benutzer
   * als {@link IUser} übergeben, sofern vorhanden. Achtung: Im Rahmen des
   * Prozessmanagers ist nicht unbedingt genau definiert, welcher Benutzer
   * hier übergeben wird.
   */
  public static final String INTREXX_USER = "INTREXX_USER";

  /**
   * Für einen Parameter mit diesem Namen wird der aktuelle Intrexx-Request
   * als {@link IServerBridgeRequest} übergeben, sofern vorhanden.
   */
  public static final String INTREXX_REQUEST = "INTREXX_REQUEST";

  /**
   * Für einen Parameter mit diesem Namen wird der aktuelle
   * {@link de.uplanet.lucy.server.SharedState} übergeben, sofern vorhanden.
   */
  public static final String INTREXX_SHARED_STATE = "INTREXX_SHARED_STATE";

  @Override
  public ReportParameterValueHolder createParameterValue(ReportContext context,
      IntrexxApplicationReport report,
      JRParameter parameter)
  {
    // TODO: Irgendwie sollte man hier vielleicht das Prinzip umdrehen?
    ReportParameterValueHolder value = new ReportParameterValueHolder();

    if(REPORT_CONTEXT.equalsIgnoreCase(parameter.getName()))
    {
      value.setValue(context);
    }
    else if(INTREXX_CONTEXT.equalsIgnoreCase(parameter.getName()))
    {
      value.setValue(context.ix());
    }
    else if(INTREXX_SHARED_STATE.equalsIgnoreCase(parameter.getName()))
    {
      value.setValue(context.ix().getSharedState());
    }
    else if(INTREXX_SESSION.equalsIgnoreCase(parameter.getName()))
    {
      value.setValue(context.ix().getSession());
    }
    else if(INTREXX_USER.equalsIgnoreCase(parameter.getName()))
    {
      value.setValue(context.ix().getSession().getUser());
    }
    else if(INTREXX_REQUEST.equalsIgnoreCase(parameter.getName()))
    {
      value.setValue(context.ix().getRequest());
    }
    else if(INTREXX_LANGUAGE.equalsIgnoreCase(parameter.getName()))
    {
      value.setValue(context.getLanguage());
    }
    else if(INTREXX_USER_TIME_ZONE.equalsIgnoreCase(parameter.getName()))
    {
      value.setValue(context.ix().getUserTimeZone());
    }
    else if("REPORT_LOCALE".equalsIgnoreCase(parameter.getName()))
    {
      ILocale serverLocale = (ILocale)(context.ix().getSession().get("locale"));
      value.setValue(Locale.forLanguageTag(serverLocale.getLanguageTag()));
    }

    return (value.hasValue() ? value : null);
  }
}
