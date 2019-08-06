/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singletonMap;

import java.io.*;
import java.util.*;

import net.sf.jasperreports.engine.*;

import org.apache.commons.collections.map.*;
import org.apache.commons.logging.*;
import org.springframework.util.*;

import de.quinscape.intrexx.report.*;
import de.quinscape.intrexx.report.ReportContext;

/**
 * Basis für typische Implementierungen von Clients dieses Pakets.
 * 
 * TODO: Umbenennen, Doku und Interface
 * 
 * @author Jörg Gottschling
 */
public final class MainReportService
    implements ReportService
{

  /** Allgemeiner Profiling-Logger. TODO: eigene Profiling Klasse? */
  private static final Log profiling =
      LogFactory.getLog("de.quinscape.intrexx.report.Profiling");

  /** Siehe {@link #setExporters(List)}. */
  @SuppressWarnings("unchecked")
  private Map<String, Exporter> exporters = new CaseInsensitiveMap();

  /** Siehe {@link #setFiller(Filler)}. */
  private Filler filler;

  /** Siehe {@link #setParametriser(Parametriser)}. */
  private Parametriser parametriser;

  /** Siehe {@link #setRepository(ReportsRepository)}. */
  private PermissionGuard permissionGuard;

  /** Siehe {@link #setRepository(ReportsRepository)}. */
  private ReportsRepository repository;

  /**
   * Die {@link Exporter} welche genutzt werden sollen um die Berichte zu exportieren.
   */
  public void setExporters(List<Exporter> exporterList)
  {
    exporters.clear();
    for(Exporter exporter : exporterList)
      exporters.put(exporter.getFormatId(), exporter);
  }

  /**
   * Der {@link Filler} welcher genutzt werden soll um die Berichte mit Daten und Parametern zu
   * befüllen.
   */
  public void setFiller(Filler filler)
  {
    this.filler = filler;
  }

  /**
   * Der {@link Parametriser} der genutzt wird um Werte für die Parameter des Berichts zu erzeugen.
   */
  public void setParametriser(Parametriser parametriser)
  {
    this.parametriser = parametriser;
  }

  /**
   * Der {@link PermissionGuard} welcher verwendet werden soll um die Berechtigungen für die
   * Berichte zu prüfen.
   */
  public void setPermissionGuard(PermissionGuard permissionGuard)
  {
    this.permissionGuard = permissionGuard;
  }

  /**
   * Das {@link ReportsRepository} aus dem die {@link Report}s geholt werden.
   */
  public void setRepository(ReportsRepository repository)
  {
    this.repository = repository;
  }

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
  @Override
  public void createReport(ReportContext context, String reportIdentifier,
      Map<String, ? extends ExportTarget> exportTargets)
      throws Exception
  {
    Assert.notNull(context, "context was null!");
    Assert.hasText(reportIdentifier, "reportIdentifier was null or empty!");
    Assert.notEmpty(exportTargets, "exportTargets was null or empty!");

    final long start = currentTimeMillis();

    Report report = repository.findReport(reportIdentifier);
    context.setReport(report);
    Assert.notNull(report, "No report \"" + reportIdentifier + "\" found.");

    if(!permissionGuard.isReportCreationPermitted(context, report))
      throw new IntrexxReportException("Report creation not permitted!", "NO_PERMISSION");

    parametriser.parametrise(context, report);
    JasperPrint print = filler.fill(context, report);

    for(String formatId : exportTargets.keySet())
    {
      ExportTarget exportTarget = exportTargets.get(formatId);
      Assert.notNull(exportTarget, "No export target for  format \"" + formatId + "\".");

      Exporter exporter = exporters.get(formatId);
      Assert.notNull(exporter, "No exporter configured for format \"" + formatId + "\".");

      OutputStream out = exportTarget.createOutputStream(context, exporter);
      Assert.notNull(out, "No outputstream for export format \"" + formatId + "\".");

      exporter.export(context, print, out);
      exportTarget.afterExport(context, exporter, out);
    }

    final long duration = currentTimeMillis() - start;
    profiling.debug("Creation of report \"" + reportIdentifier + "\" took " + duration + " ms.");
  }

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
  @Override
  public void createReport(ReportContext context, String reportIdentifier, String exportFormat,
      ExportTarget exportTarget)
      throws Exception
  {
    Assert.hasText(exportFormat, "exportFormat was null or empty!");
    Assert.notNull(exportTarget, "exportTarget was null!");
    createReport(context, reportIdentifier, singletonMap(exportFormat, exportTarget));
  }

}
