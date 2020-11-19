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
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporterContext;
import net.sf.jasperreports.export.SimpleXlsxExporterConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.export.XlsxExporterConfiguration;
import net.sf.jasperreports.export.XlsxReportConfiguration;

/**
 * Exportiert einen Bericht in ein Dokument im Format "Office Open XML" für
 * Microsoft Excel - kurz XLSX.
 * 
 * @author Jörg Gottschling
 */
public class XlsxExporter
    extends
    AbstractExcelExporter<JRXlsxExporter, XlsxReportConfiguration, XlsxExporterConfiguration, JRXlsxExporterContext>
{

  /**
   * Erzeugt eine initialisierte Instanz.
   */
  public XlsxExporter()
  {
    super("XLSX", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
  }

  @Override
  protected JRXlsxExporter createExporter(ReportContext context, JasperReportsContext jrc)
  {
    return new JRXlsxExporter(jrc);
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createExporterConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected XlsxExporterConfiguration createExporterConfiguration(ReportContext context)
  {
    return new SimpleXlsxExporterConfiguration();
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createReportConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected XlsxReportConfiguration createReportConfiguration(ReportContext context)
  {
    return new SimpleXlsxReportConfiguration();
  }
}
