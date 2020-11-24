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

import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.SimpleTextExporterConfiguration;
import net.sf.jasperreports.export.SimpleTextReportConfiguration;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.TextExporterConfiguration;
import net.sf.jasperreports.export.TextReportConfiguration;
import net.sf.jasperreports.export.WriterExporterOutput;

/**
 * Exportiert einen Bericht in das Textformat "Comma Separated Values" -
 * kurz CSV.
 * 
 * @author Jörg Gottschling
 */
public class TextExporter
    extends
    AbstractJRExporterBasedExporter<JRTextExporter, ExporterInput, TextReportConfiguration, TextExporterConfiguration, WriterExporterOutput>
{

  /**
   * Erzeugt eine initialisierte Instanz.
   */
  public TextExporter()
  {
    super("TXT", "text/plain", JRTextExporter.class);
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
  protected void configureExporter(ReportContext context, JRTextExporter exporter,
      TextReportConfiguration reportConfiguration, TextExporterConfiguration exportConfiguration,
      WriterExporterOutput output)
  {
    // hier kann der Exporter konfiguriert werden
    // exporter.setParameter(FIELD_DELIMITER, fieldDelimiter);
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createReportConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected TextReportConfiguration createReportConfiguration(ReportContext context)
  {
    return new SimpleTextReportConfiguration();
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createExporterConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected TextExporterConfiguration createExporterConfiguration(ReportContext context)
  {
    return new SimpleTextExporterConfiguration();
  }

  @Override
  protected WriterExporterOutput createOutput(java.io.OutputStream target, String characterEncoding)
  {
    return new SimpleWriterExporterOutput(target, characterEncoding);
  }
}
