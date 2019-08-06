/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain.fill;

import net.sf.jasperreports.engine.*;

import de.quinscape.intrexx.report.ReportContext;
import de.quinscape.intrexx.report.domain.*;

/**
 * Erzeugt einen einzelnen Wert für einen bestimmten Parameter.
 * 
 * @author Jörg Gottschling
 */
public interface ParameterValueCreator
{

  /**
   * Liefert einen einzelnen Wert für den übergebenen Parameter. Der Wert
   * muss nicht konvertiert werden. Wenn kein Wert ermittelt werden kann,
   * muss <code>null</code> zurück geliefert werden. In diesem wird kein
   * Parameter zum Kontext hinzugefügt und eventuell ein anderer
   * {@link ParameterValueCreator} aufgerufen.
   * <p>
   * <b>Hinweis:</b> Wenn allerdings ein Wert ermittelt wird, darf kein
   * {@code Object} zurückgegeben werden, sondern ein
   * {@link RequestParameterValueCreator}. Ist aus historischen Gründen so
   * angelegt.
   * 
   * @param context
   *          Der aktuelle {@link ReportContext} aus dem in der Regel die
   *          Werte entnommen werden (z.B. Request) werden. Der Wert darf
   *          noch <i>nicht</i> direkt geschrieben werden.
   * 
   * @param report
   *          Der Bericht zu dem der Parameter gehört.
   * 
   * @param parameter
   *          Der Parameter für den der Wert erzeugt werden soll.
   * 
   * @return Der noch nicht konvertierte Wert. Ist <code>null</code> wenn
   *         kein Wert erzeugt werden konnte.
   */
  Object createParameterValue(ReportContext context, Report report,
      JRParameter parameter);
}
