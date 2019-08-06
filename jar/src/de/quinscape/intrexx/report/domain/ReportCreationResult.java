/*
 * (C) Copyright 2008 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain;

import static java.util.Collections.emptyMap;

import java.util.*;

import org.springframework.util.*;

/**
 * Enthält alle Informationen zum Ergebnis der Generierung eines Berichts,
 * wie beispielsweise der Dateiname zu jedem Export oder Fehlermeldungen.
 * Wird an Velocity zurückgeliefert.
 * 
 * @see VelocityExportResult
 * 
 * @author Jörg Gottschling
 */
public class ReportCreationResult
{

  private final Exception exception;

  private final Map<String, VelocityExportResult> exportTargets;
  
  private final boolean successful;

  private String reportName;

  /**
   * Erzeugt ein {@link ReportCreationResult} für einen (Teil-)Erfolg.
   */
  public ReportCreationResult(Map<String, VelocityExportResult> exportTargets)
  {
    this.exportTargets = exportTargets;
    this.successful = true;
    this.exception = null;
  }

  /**
   * Erzeugt ein {@link ReportCreationResult} für einen Fehlschlag.
   */
  public ReportCreationResult(Exception exception)
  {
    this.exportTargets = emptyMap();
    this.successful = false;
    this.exception = exception;
  }

  /** Die Fehlermeldung bei Misserfolg. */
  public Exception getException()
  {
    return exception;
  }

  /**
   * Liefert für alle exportierten Formate ein {@link VelocityExportResult}.
   */
  public Collection<VelocityExportResult> getExports()
  {
    return exportTargets.values();
  }

  /**
   * Waren Übersetzung und Füllen erfolgreich? Schliesst die Exporte nicht
   * mit ein!
   */
  public boolean isSuccessful()
  {
    return successful;
  }

  /**
   * Liefert das {@link ReportResult} an Hand des Namens des Formats.
   * Erlaubt eine Abkürzung in Velocity, z.B.:
   * <code>$reportResult.pdf.externalFileUrl</code>
   */
  public VelocityExportResult get(String formatId)
  {
    Assert.hasText(formatId, "Format id was null or empty.");
    return exportTargets.get(formatId.toUpperCase());
  }

  /**
   * Der Name des erzeugten Reports.
   */
  public String getReportName()
  {
    return reportName;
  }

  /**
   * Der Name des erzeugten Reports.
   */
  void setReportName(String reportName)
  {
    this.reportName = reportName;
  }
}
