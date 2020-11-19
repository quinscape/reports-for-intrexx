/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.export;

import static java.lang.System.currentTimeMillis;

import java.io.File;
import java.io.OutputStream;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.uplanet.lucy.server.composer.UrlBuilder;
import de.uplanet.lucy.server.util.URL;

import de.quinscape.intrexx.reports.ReportContext;

import net.sf.jasperreports.engine.export.FileHtmlResourceHandler;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.HtmlExporterConfiguration;
import net.sf.jasperreports.export.HtmlExporterOutput;
import net.sf.jasperreports.export.HtmlReportConfiguration;
import net.sf.jasperreports.export.SimpleHtmlExporterConfiguration;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleHtmlReportConfiguration;

/**
 * Exportiert einen Bericht in HTML.
 * 
 * @author Jörg Gottschling
 */
public class HtmlExporter
    extends
    AbstractJRExporterBasedExporter<net.sf.jasperreports.engine.export.HtmlExporter, ExporterInput, HtmlReportConfiguration, HtmlExporterConfiguration, HtmlExporterOutput>
{

  /**
   * Logger for this class
   */
  private static final Log log = LogFactory.getLog(HtmlExporter.class);

  private static final FastDateFormat dateFormat =
      FastDateFormat.getInstance("yyyyMMdd-HHmmss");

  private boolean removeEmptySpaceBetweenRows = true;

  private boolean usingImagesToAlign;

  private boolean wrapBreakWord = true;

  /**
   * Erzeugt eine initialisierte Instanz.
   */
  public HtmlExporter()
  {
    super("HTML", "text/html", net.sf.jasperreports.engine.export.HtmlExporter.class);
  }

  /**
   * Siehe {@link #setRemoveEmptySpaceBetweenRows(boolean)}.
   */
  public boolean isRemoveEmptySpaceBetweenRows()
  {
    return removeEmptySpaceBetweenRows;
  }

  /**
   * Siehe {@link #setUsingImagesToAlign(boolean)}.
   */
  public boolean isUsingImagesToAlign()
  {
    return usingImagesToAlign;
  }

  /**
   * Siehe {@link #setWrapBreakWord(boolean)}.
   */
  public boolean isWrapBreakWord()
  {
    return wrapBreakWord;
  }

  /**
   * Siehe {@link #removeEmptySpaceBetweenRows}
   */
  public void setRemoveEmptySpaceBetweenRows(boolean flag)
  {
    this.removeEmptySpaceBetweenRows = flag;
  }

  /**
   * Siehe {@link #usingImagesToAlign}
   */
  public void setUsingImagesToAlign(boolean usingImagesToAlign)
  {
    this.usingImagesToAlign = usingImagesToAlign;
  }

  /**
   * Siehe {@link #wrapBreakWord}
   */
  public void setWrapBreakWord(boolean wrapBreakWord)
  {
    this.wrapBreakWord = wrapBreakWord;
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#configureExporter(de.quinscape.intrexx.reports.ReportContext,
   *      net.sf.jasperreports.export.Exporter,
   *      net.sf.jasperreports.export.ReportExportConfiguration,
   *      net.sf.jasperreports.export.ExporterConfiguration,
   *      net.sf.jasperreports.export.ExporterOutput)
   */
  @Override
  protected void configureExporter(ReportContext context,
      net.sf.jasperreports.engine.export.HtmlExporter exporter,
      HtmlReportConfiguration reportConfiguration,
      net.sf.jasperreports.export.HtmlExporterConfiguration exportConfiguration,
      HtmlExporterOutput output)
  {
    SimpleHtmlExporterOutput out = (SimpleHtmlExporterOutput)output;
    SimpleHtmlReportConfiguration reportConf = (SimpleHtmlReportConfiguration)reportConfiguration;

    reportConf.setRemoveEmptySpaceBetweenRows(removeEmptySpaceBetweenRows);
    reportConf.setWrapBreakWord(wrapBreakWord);

    String portalLink;
    try
    {
      URL portalUrl;
      portalUrl =
          UrlBuilder.createAbsoluteBaseDirectoryUrl(context.getBusinessLogicProcessingContext());
      portalLink = portalUrl.asString();
    }
    catch(Exception exc)
    {
      log.warn("Intrexx UrlBuilder threw an exception "
               + exc.getClass().getName()
               + (exc.getMessage() != null ? "(message: " + exc.getMessage() + ") " : " ")
               + "when generating a base url for references (links, images, ...) in the html-report. Try setting the base url like described in the portals manual (delivered with intrexx).");
      portalLink = "";
    }
    String imgUri =
        "tmp/" + context.getSession().getId() + "/"
                    + context.getReport().getMainJasperReport().getName() + "-"
                    + dateFormat.format(currentTimeMillis()) + "/";
    File imgFile = new File("external/htmlroot", imgUri);
    imgFile.mkdirs();

    String pathPattern = portalLink + imgUri + "{0}";
    out.setImageHandler(new FileHtmlResourceHandler(imgFile, pathPattern));
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createOutput(java.io.OutputStream,
   *      java.lang.String)
   */
  @Override
  protected HtmlExporterOutput createOutput(OutputStream target, String characterEncoding)
  {
    return new SimpleHtmlExporterOutput(target, characterEncoding);
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createReportConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected HtmlReportConfiguration createReportConfiguration(ReportContext context)
  {
    return new SimpleHtmlReportConfiguration();
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createExporterConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected SimpleHtmlExporterConfiguration createExporterConfiguration(ReportContext context)
  {
    return new SimpleHtmlExporterConfiguration();
  }

}
