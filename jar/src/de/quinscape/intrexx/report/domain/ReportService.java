/*
 * (C) Copyright 2011 QuinScape GmbH
 * All Rights Reserved.
 *
 * http://www.quinscape.de
 *
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain;

import java.util.*;

import de.quinscape.intrexx.report.*;

/**
 * <p>ReportService.</p>
 * 
 * DOKU: ReportService
 *
 * @author Jörg Gottschling
 */
public interface ReportService
{

  /**
   * Die eigentliche Berichtserzeugung.
   * 
   * @param context
   *          Der aktuelle ReportContext
   * 
   * @param reportIdentifier
   *          Der GUID oder Name des Berichts.
   * 
   * @param exportTargets
   *          {@link Collection} von {@link ExportTarget}s.
   */
  void createReport(ReportContext context, String reportIdentifier,
      Map<String, ? extends ExportTarget> exportTargets)
      throws Exception;

  /**
   * Die eigentliche Berichtserzeugung.
   * 
   * @param context
   *          Der aktuelle ReportContext
   * 
   * @param reportIdentifier
   *          Der GUID oder Name des Berichts
   * 
   * @param exportFormat
   *          Das ExportFormat.
   * 
   * @param exportTarget
   *          Das {@link ExportTarget}.
   */
  void createReport(ReportContext context, String reportIdentifier,
      String exportFormat, ExportTarget exportTarget)
      throws Exception;

}
