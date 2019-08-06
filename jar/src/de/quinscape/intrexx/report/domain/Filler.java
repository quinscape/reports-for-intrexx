/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain;

import net.sf.jasperreports.engine.*;

import de.quinscape.intrexx.report.ReportContext;

/**
 * Befüllt einen {@link Report} mit Daten und liefert ein
 * {@link JasperPrint} zurück das im Anschluss exportiert werden kann. Es
 * handelt sich um eine Service-Schnittstelle. Implementationen diese
 * Interfaces sollen zustandslos und nebenläufig benutzbar sein.
 * 
 * @author Jörg Gottschling
 */
public interface Filler
{

  /**
   * Befüllt den übergebenen Report.
   * 
   * @param context
   *          Der aktuelle {@link ReportContext}.
   * 
   * @param report
   *          Der zu befüllende Bericht.
   * 
   * @return Ein fertiges {@link JasperPrint} das im Anschluss exportiert
   *         werden kann.
   */
  JasperPrint fill(ReportContext context, Report report);

}
