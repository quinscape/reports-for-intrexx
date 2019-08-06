/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain.export;

import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.export.*;

import de.quinscape.intrexx.report.*;

/**
 * Exportiert einen Bericht in das Portable Document Format - kurz PDF.
 * Nutzt den {@link JRPdfExporter}.
 * 
 * @author Jörg Gottschling
 */
public class PdfExporter
    extends
    AbstractJRExporterBasedExporter<JRPdfExporter, ExporterInput, PdfReportConfiguration, PdfExporterConfiguration, OutputStreamExporterOutput>
{

  /**
   * Erzeugt eine initialisierte Instanz.
   */
  public PdfExporter()
  {
    super("PDF", "application/pdf", JRPdfExporter.class);
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.report.domain.export.AbstractJRExporterBasedExporter#createReportConfiguration(de.quinscape.intrexx.report.ReportContext)
   */
  @Override
  protected PdfReportConfiguration createReportConfiguration(ReportContext context)
  {
    return new SimplePdfReportConfiguration();
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.report.domain.export.AbstractJRExporterBasedExporter#createExporterConfiguration(de.quinscape.intrexx.report.ReportContext)
   */
  @Override
  protected PdfExporterConfiguration createExporterConfiguration(ReportContext context)
  {
    return new SimplePdfExporterConfiguration();
  }
}
