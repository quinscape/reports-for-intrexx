/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain;

import de.quinscape.intrexx.report.*;

/**
 * Erzeugt Parameterwerte für den (Haupt-)Bericht.
 * 
 * @author Jörg Gottschling
 */
public interface Parametriser
{

  /**
   * Erzeugt Parameterwerte für den übergebene (Haupt-)Bericht und
   * hinterlegt diese direkt im übergebenen {@link ReportContext}.
   * 
   * @param context
   *          Der aktuelle {@link ReportContext} aus dem in der Regel die
   *          Werte entnommen werden (z.B. Request) und in den sie auch
   *          wieder zurück geschrieben werden.
   * 
   * @param report
   *          Der Bericht für den die Werte erzeugt werden sollen.
   * 
   * @see ReportContext#getReportParameters()
   */
  void parametrise(ReportContext context, Report report);
}
