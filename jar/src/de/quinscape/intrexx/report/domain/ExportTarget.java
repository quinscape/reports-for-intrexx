/*
 * (C) Copyright 2011 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain;

import java.io.*;

import de.quinscape.intrexx.report.*;

/**
 * Kennt das Ziel eines Exports und kann dieses entsprechend behandeln. Kann
 * zustandsbehaftet sein.
 * 
 * @author Jörg Gottschling
 */
public interface ExportTarget
{

  /**
   * Callback-Methode die <i>vor</i> dem Export aufgerufen wird und das
   * Ziel des Export als {@link OutputStream} liefert.
   */
  OutputStream createOutputStream(ReportContext context, Exporter exporter)
      throws Exception;

  /**
   * Callback-Methode die <i>nach</i> dem Export aufgerufen wird und das
   * Ziel des Export als {@link OutputStream} übergebene bekommt um es zu
   * schließen oder ähnliches.
   */
  void afterExport(ReportContext context, Exporter exporter, OutputStream os)
      throws Exception;

}
