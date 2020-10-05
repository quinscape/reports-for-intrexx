/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.export;

import de.quinscape.intrexx.reports.ReportContext;

import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.PptxExporterConfiguration;
import net.sf.jasperreports.export.PptxReportConfiguration;
import net.sf.jasperreports.export.SimplePptxExporterConfiguration;
import net.sf.jasperreports.export.SimplePptxReportConfiguration;

/**
 * Exportiert einen Bericht in ein Dokument im Format "Office Open XML" für
 * Microsoft Excel - kurz XLSX.
 * 
 * @author Jörg Gottschling
 */
public class PptxExporter
    extends
    AbstractJRExporterBasedExporter<JRPptxExporter, ExporterInput, PptxReportConfiguration, PptxExporterConfiguration, OutputStreamExporterOutput>
{

  /**
   * Erzeugt eine initialisierte Instanz.
   */
  public PptxExporter()
  {
    super("PPTX",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation");
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createExporter(de.quinscape.intrexx.reports.ReportContext,
   *      net.sf.jasperreports.engine.JasperReportsContext)
   */
  @Override
  protected JRPptxExporter createExporter(ReportContext context, JasperReportsContext jrcontext)
  {
    return new JRPptxExporter();
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createReportConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected PptxReportConfiguration createReportConfiguration(ReportContext context)
  {
    return new SimplePptxReportConfiguration();
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createExporterConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected PptxExporterConfiguration createExporterConfiguration(ReportContext context)
  {
    return new SimplePptxExporterConfiguration();
  }
}
