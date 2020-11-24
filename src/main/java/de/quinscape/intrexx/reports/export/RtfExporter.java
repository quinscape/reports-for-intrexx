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

import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.RtfExporterConfiguration;
import net.sf.jasperreports.export.RtfReportConfiguration;
import net.sf.jasperreports.export.SimpleRtfExporterConfiguration;
import net.sf.jasperreports.export.SimpleRtfReportConfiguration;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.WriterExporterOutput;

/**
 * Exportiert einen Bericht in das Rich Text Format - kurz RTF.
 * 
 * @author Jörg Gottschling
 */
public class RtfExporter
    extends
    AbstractJRExporterBasedExporter<JRRtfExporter, ExporterInput, RtfReportConfiguration, RtfExporterConfiguration, WriterExporterOutput>
{

  /**
   * Erzeugt eine initialisierte Instanz.
   */
  public RtfExporter()
  {
    super("RTF", "application/rtf", JRRtfExporter.class);
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createReportConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected RtfReportConfiguration createReportConfiguration(ReportContext context)
  {
    return new SimpleRtfReportConfiguration();
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createExporterConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected RtfExporterConfiguration createExporterConfiguration(ReportContext context)
  {
    return new SimpleRtfExporterConfiguration();
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
