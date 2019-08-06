/*
 * (C) Copyright 2003-2007 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain;

import static org.apache.commons.lang.StringUtils.split;
import static org.springframework.util.StringUtils.hasText;

import java.util.*;

import org.apache.commons.logging.*;
import org.springframework.util.*;

import de.quinscape.intrexx.report.*;

/**
 * Diese Wrapper-Klasse dient zur Integration von Reports in Intrexx via Velocity.
 * 
 * @author Jörg Gottschling (joerg.gottschling@quinscape.de)
 * @author Philip Moston (phil.moston@quinscape.de) (Original)
 */
public class VelocityReportCreator
{

  /**
   * Name des Parameters der die ExportFormate enthält. Der Parameter-Wert
   * muss die Namen der Exportformate enthalten. Getrennt durch Komma,
   * Semikolon oder Pipe.
   */
  public static final String rq_qsExportFormats = "rq_qsExportFormats";

  static final String[] identifierKeys =
      {"rq_ReportName", "rq_qsReportName", ReportsServlet.qsReport,
       ReportsServlet.rq_qsReport};

  /** Logger für diese Klasse. */
  private static final Log log = LogFactory.getLog(VelocityReportCreator.class);

  private ReportService reportService;

  /**
   * Der {@link ReportService} der zur Erzugung des Reports genutzt werden
   * soll.
   */
  public void setReportService(ReportService reportService)
  {
    this.reportService = reportService;
  }

  /**
   * Erzeugt den eigentlichen Report mit Parametern aus dem Request.
   * 
   * @return Ein {@link ReportCreationResult} das alles Informationen über
   *         die fertigen Berichte enthält.
   */
  public ReportCreationResult createReport(ReportContext context)
  {
    Assert.notNull(context, "Context was null!");

    Map<String, VelocityExportResult> exportTargets =
        resolveExportTargets(context);
    ReportCreationResult result = new ReportCreationResult(exportTargets);

    // FIXME: result.setReportName(report.getDisplayName());

    try
    {
      reportService.createReport(context, resolveIdentifier(context),
          exportTargets);
    }
    catch(Exception exc)
    {
      log.error(exc.getMessage(), exc);
      result = new ReportCreationResult(exc);
    }
    catch(NoClassDefFoundError err)
    {
      String msg =
          "Ein Teil der benötigten Bibliotheken wurde nicht gefunden. Bitte überprüfen Sie die Installation.";
      log.error(msg, err);
      result = new ReportCreationResult(new IntrexxReportException(msg));
    }

    return result;
  }

  /**
   * Liefert alle Exportformate, die bei der Generierung berücksichtigt
   * werden sollen, an Hand des Requests. Die Werte werden aus dem Parameter
   * {@link #rq_qsExportFormats} gelesen.
   */
  Map<String, VelocityExportResult> resolveExportTargets(ReportContext context)
  {
    Map<String, VelocityExportResult> exportTargets =
        new HashMap<String, VelocityExportResult>();

    String requestValue = context.getRequest().get(rq_qsExportFormats);
    if(hasText(requestValue))
    {
      for(String format : split(requestValue, ",;|"))
        exportTargets.put(format.trim(), new VelocityExportResult());
    }
    else
    {
      exportTargets.put("PDF", new VelocityExportResult());
    }

    return exportTargets;
  }

  String resolveIdentifier(ReportContext context)
  {
    for(String key : identifierKeys)
    {
      String identifier = context.getRequest().get(key);
      if(hasText(identifier)) return identifier;
    }
    throw new IntrexxReportException("No report identifier in request!");
  }

}
