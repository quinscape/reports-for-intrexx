/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain.export;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.ooxml.*;
import net.sf.jasperreports.export.*;

import de.quinscape.intrexx.report.ReportContext;

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
   * @see de.quinscape.intrexx.report.domain.export.AbstractJRExporterBasedExporter#createExporter(de.quinscape.intrexx.report.ReportContext,
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
   * @see de.quinscape.intrexx.report.domain.export.AbstractJRExporterBasedExporter#createReportConfiguration(de.quinscape.intrexx.report.ReportContext)
   */
  @Override
  protected PptxReportConfiguration createReportConfiguration(ReportContext context)
  {
    return new SimplePptxReportConfiguration();
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.report.domain.export.AbstractJRExporterBasedExporter#createExporterConfiguration(de.quinscape.intrexx.report.ReportContext)
   */
  @Override
  protected PptxExporterConfiguration createExporterConfiguration(ReportContext context)
  {
    return new SimplePptxExporterConfiguration();
  }
}
