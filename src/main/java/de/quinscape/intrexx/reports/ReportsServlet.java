/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports;

import static org.springframework.util.StringUtils.hasText;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import de.uplanet.lucy.server.businesslogic.BusinessLogicProcessingContext;
import de.uplanet.lucy.server.businesslogic.IBusinessLogicProcessingContext;
import de.uplanet.lucy.server.connector.IServerBridgeRequest;
import de.uplanet.lucy.server.connector.IServerBridgeResponse;
import de.uplanet.lucy.server.engine.http.IIxServlet;
import de.uplanet.lucy.server.engine.http.IWebProcessingContext;

import de.quinscape.intrexx.reports.domain.ReportService;
import de.quinscape.intrexx.reports.export.ExportTarget;
import de.quinscape.intrexx.reports.export.Exporter;

/**
 * Ein {@link IIxServlet} welches einen Report in einem Format erzeugt und
 * direkt an den Client ausliefert. Ausgeliefert wird pro Aufruf eine
 * einzige Datei, also nur ein Exportformat. Die Ausgabe kann in einem Text-
 * oder Binärformat sein kann.
 * 
 * @author Jörg Gottschling
 */
public class ReportsServlet
    implements IIxServlet, ExportTarget
{

  /**
   * Der Name des Request-POST-Parameters welcher den Namen zu Formates
   * enthält in den der Bericht exportiert werden soll.
   */
  public static final String qsExportFormat = "qsExportFormat";

  /**
   * Der Name des Request-POST-Parameters welcher den Namen der Datei enthält welche das Servlet als
   * Bericht ausliefert. Diesen Namen wird der Browser zum Speichern vorschlagen. Vorbelegung ist
   * der Anzeigename des Berichts sofern vorhanden, sonst "Report"; jeweils erweitert um die zum
   * Exportformat passende Dateiendung. Dieses Vorgehen ist konsistent mit der Groovy-API.
   */
  public static final String qsFileName = "qsFileName";

  /**
   * Der Name des Request-POST-Parameters welcher den Namen des zu
   * erzeugenden Berichts enthält.
   */
  public static final String qsReport = "qsReport";

  /**
   * Der Name des Request-POST-Parameters welcher bestimmt, ob die Datei als Attachment oder Inline
   * ausgeliefert wird.
   */
  public static final String qsAsAttachment = "qsAsAttachment";

  /**
   * Der Name des Request-GET-Parameters welcher den Namen zu Formates
   * enthält in den das Berichts exportiert werden soll.
   */
  public static final String rq_qsExportFormat = "rq_qsExportFormat";

  /**
   * Der Name des Request-GET-Parameters welcher den Namen der Datei enthält welche das Servlet als
   * Bericht ausliefert. Diesen Namen wird der Browser zum Speichern vorschlagen. Vorbelegung ist
   * der Anzeigename des Berichts sofern vorhanden, sonst "Report"; jeweils erweitert um die zum
   * Exportformat passende Dateiendung. Dieses Vorgehen ist konsistent mit der Groovy-API.
   */
  public static final String rq_qsFileName = "rq_qsFileName";

  /**
   * Der Name des Request-GET-Parameters welcher den Namen des zu
   * erzeugenden Berichts enthält.
   */
  public static final String rq_qsReport = "rq_qsReport";

  /**
   * Der Name des Request-GET-Parameters welcher bestimmt, ob die Datei als Attachment oder Inline
   * ausgeliefert wird.
   */
  public static final String rq_qsAsAttachment = "rq_qsAsAttachment";

  /** Logger for this class. */
  private static final Log log = LogFactory.getLog(ReportsServlet.class);

  private ReportService reportService;

  @Override
  public void init()
  {
    Assert.notNull(reportService, "No report service configured.");
  }

  /**
   * Wird von Intrexx aufgerufen, wenn ein Report erzeugt werden soll.
   */
  @Override
  public void service(IWebProcessingContext webContext,
      IServerBridgeRequest request, IServerBridgeResponse response)
  {
    // webContext könnte schon BusinessLogicProcessingContext sein
    IBusinessLogicProcessingContext blContext;
    if(webContext instanceof IBusinessLogicProcessingContext)
    {
      blContext = (IBusinessLogicProcessingContext)webContext;
    }
    else
    {
      // TODO Unter welchen Umständen wird dieser Zweig aufgerufen? Was muss dann als
      // ApplicationContext (1. Parameter) angegeben werden?
      blContext = new BusinessLogicProcessingContext(null, webContext.getConnection(), request,
          response, webContext.getSession(), webContext.getSharedState(), webContext.getViewData());
    }

    ReportContext context = new DefaultReportContext(blContext);

    try
    {
      reportService.createReport(context, reportIdentifier(context),
          exportFormat(context), this);
    }
    catch(Exception exc)
    {
      String msg = "Error while creating report: " + exc.getMessage();
      log.error(msg, exc);

      try
      {
        StringWriter stackTrace = new StringWriter(1024);
        exc.printStackTrace(new PrintWriter(stackTrace));
        msg += stackTrace.toString();
        response.sendError(500, msg);
      }
      catch(Exception garbage)
      {
        ; // Silently
      }
    }
  }

  @Override
  public OutputStream createOutputStream(ReportContext context,
      Exporter exporter)
      throws Exception
  {
    IServerBridgeResponse response = context.ix().getResponse();

    // HTTP Header
    response.setStatus(200);
    response.setHeaderValue("Content-Type", contentType(exporter));
    response.setHeaderValue("Content-Disposition",
        disposition(context, exporter, asAttachment(context)));
    response.setHeaderValue("Cache-Control", "no-cache");

    return response.getOutputStream();
  }

  String contentType(Exporter exporter)
  {
    String contentType = exporter.getContentType();
    if(hasText(exporter.getCharacterEncoding()))
      contentType += "; charset=" + exporter.getCharacterEncoding();
    return contentType;
  }

  /**
   * Erzeugt den Wert für die "Content-Disposition" für den HTTP-Header als <code>String</code>.
   */
  String disposition(ReportContext context, Exporter exporter, boolean asAttachment)
  {

    StringBuilder fileName;
    if(asAttachment)
      fileName = new StringBuilder("attachment; filename=");
    else
      fileName = new StringBuilder("inline; filename=");

    if(hasText(context.getRequest().get(rq_qsFileName)))
    {
      fileName.append(context.getRequest().get(rq_qsFileName));
    }
    else if(hasText(context.getRequest().get(qsFileName)))
    {
      fileName.append(context.getRequest().get(qsFileName));
    }
    else if(hasText(context.getReport().getDisplayName()))
    {
      fileName.append(context.getReport().getDisplayName().replaceAll("\\s", "_"));
    }
    else
    {
      fileName.append("report");
    }

    // Verhalten ist Konsistent mit der Groovy-API.
    fileName.append('.').append(exporter.getFileExtension());

    return fileName.toString();
  }

  /**
   * Liefert das Export-Format aus dem {@link ReportContext} als <code>String</code> zurück.
   */
  String exportFormat(ReportContext context)
  {
    return context.getRequest().get(rq_qsExportFormat,
        context.getRequest().get(qsExportFormat));
  }

  /**
   * Liefert einen Identifier für den Report aus dem {@link ReportContext} als <code>String</code>
   * zurück.
   */
  String reportIdentifier(ReportContext context)
  {
    return context.getRequest().get(rq_qsReport,
        context.getRequest().get(qsReport));
  }

  boolean asAttachment(ReportContext context)
  {
    return "1".equals(context.getRequest().get(rq_qsAsAttachment,
        context.getRequest().get(qsAsAttachment)));
  }

  @Override
  public void afterExport(ReportContext context, Exporter exporter,
      OutputStream os)
      throws Exception
  {
    // Nichts zu tun
  }

  /**
   * Der {@link ReportService} der zur Erzeugung des Reports genutzt werden soll.
   */
  public void setReportService(ReportService reportService)
  {
    this.reportService = reportService;
  }

}
