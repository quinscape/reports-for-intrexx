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

import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.export.DocxExporterConfiguration;
import net.sf.jasperreports.export.DocxReportConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleDocxExporterConfiguration;
import net.sf.jasperreports.export.SimpleDocxReportConfiguration;

/**
 * Exportiert einen Bericht in ein Dokument im Format "Office Open XML" für
 * Microsoft Word - kurz DOCX.
 * 
 * @author Jörg Gottschling
 */
public class DocxExporter
    extends
    AbstractJRExporterBasedExporter<JRDocxExporter, ExporterInput, DocxReportConfiguration, DocxExporterConfiguration, OutputStreamExporterOutput>
{

  /**
   * Erzeugt eine initialisierte Instanz.
   */
  public DocxExporter()
  {
    super(
        "DOCX",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        JRDocxExporter.class);
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createReportConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected DocxReportConfiguration createReportConfiguration(ReportContext context)
  {
    return new SimpleDocxReportConfiguration();
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createExporterConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected DocxExporterConfiguration createExporterConfiguration(ReportContext context)
  {
    return new SimpleDocxExporterConfiguration();
  }

}
