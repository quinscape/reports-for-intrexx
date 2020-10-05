/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.domain;

import de.quinscape.intrexx.reports.ReportContext;

/**
 * Ein PermissionGuard prüft ob ein Bericht im aktuell Kontext, insbesondere vom aktuellen Benutzer,
 * erzeugt werden darf, oder nicht. Dabei werden die im Bericht hinterlegten Berechtigten
 * berücksichtigt.
 * 
 * @author Jörg Gottschling
 */
public interface PermissionGuard
{

  /**
   * Prüft ob der übergebene Bericht im übergebenen Kontext erzeugt werden darf oder nicht.
   * 
   * @return Liefert <code>true</code> wenn der Bericht erzeugt werden darf, sonst
   *         <code>false</code>.
   */
  boolean isReportCreationPermitted(ReportContext context, IntrexxApplicationReport report);

}
