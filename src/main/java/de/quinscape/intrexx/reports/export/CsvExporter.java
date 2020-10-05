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

import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.export.CsvExporterConfiguration;
import net.sf.jasperreports.export.CsvReportConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.SimpleCsvExporterConfiguration;
import net.sf.jasperreports.export.SimpleCsvReportConfiguration;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.WriterExporterOutput;

/**
 * Exportiert einen Bericht in das Textformat "Comma Separated Values" -
 * kurz CSV.
 * 
 * @author Jörg Gottschling
 */
public class CsvExporter
    extends
    AbstractJRExporterBasedExporter<JRCsvExporter, ExporterInput, CsvReportConfiguration, CsvExporterConfiguration, WriterExporterOutput>
{

  private String fieldDelimiter = ";";

  private String recordDelimiter;

  /**
   * Erzeugt eine initialisierte Instanz.
   */
  public CsvExporter()
  {
    super("CSV", "text/comma-separated-values", JRCsvExporter.class);
  }

  /**
   * Siehe {@link #setFieldDelimiter(String)}.
   */
  public String getFieldDelimiter()
  {
    return fieldDelimiter;
  }

  /**
   * Siehe {@link #setRecordDelimiter(String)}.
   */
  public String getRecordDelimiter()
  {
    return recordDelimiter;
  }

  /**
   * Das Trennzeichen für die Spalten.
   * 
   * @see SimpleCsvExporterConfiguration#setFieldDelimiter(String)
   */
  public void setFieldDelimiter(String fieldDelimiter)
  {
    this.fieldDelimiter = fieldDelimiter;
  }

  /**
   * Das Trennzeichen für die Datensätze.
   * 
   * @see SimpleCsvExporterConfiguration#setRecordDelimiter(String)
   */
  public void setRecordDelimiter(String recordDelimiter)
  {
    this.recordDelimiter = recordDelimiter;
  }

  @Override
  protected void configureExporter(ReportContext context, JRCsvExporter exporter,
      CsvReportConfiguration reportConfiguration, CsvExporterConfiguration exportConfiguration,
      WriterExporterOutput output)
  {
    SimpleCsvExporterConfiguration c = (SimpleCsvExporterConfiguration)exportConfiguration;
    // TODO? if(StringUtils.hasText(fieldDelimiter))
    c.setFieldDelimiter(fieldDelimiter);
    // TODO? if(StringUtils.hasText(fieldDelimiter))
    c.setRecordDelimiter(recordDelimiter);
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createReportConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected CsvReportConfiguration createReportConfiguration(ReportContext context)
  {
    return new SimpleCsvReportConfiguration();
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createExporterConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected CsvExporterConfiguration createExporterConfiguration(ReportContext context)
  {
    return new SimpleCsvExporterConfiguration();
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createOutput(java.io.OutputStream,
   *      java.lang.String)
   */
  @Override
  protected WriterExporterOutput createOutput(java.io.OutputStream target, String characterEncoding)
  {
    return new SimpleWriterExporterOutput(target, characterEncoding);
  }
}
