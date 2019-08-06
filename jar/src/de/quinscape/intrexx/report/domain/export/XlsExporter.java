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
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.export.*;

import de.quinscape.intrexx.report.ReportContext;

/**
 * Exportiert einen Bericht in das (alte) Dateiformat von Excel "XLS".
 * 
 * @author Jörg Gottschling
 */
public class XlsExporter
    extends AbstractExcelExporter<JRXlsExporter, XlsReportConfiguration, XlsExporterConfiguration, JRXlsExporterContext>
{

  /**
   * Erzeugt eine initialisierte Instanz.
   */
  public XlsExporter()
  {
    super("XLS", "application/msexcel");
  }

  @Override
  protected JRXlsExporter createExporter(ReportContext context, JasperReportsContext jrc)
  {
    return new JRXlsExporter(jrc);
  }
  
  /**
   * {@inheritDoc}
   * @see de.quinscape.intrexx.report.domain.export.AbstractJRExporterBasedExporter#createExporterConfiguration(de.quinscape.intrexx.report.ReportContext)
   */
  @Override
  protected XlsExporterConfiguration createExporterConfiguration(ReportContext context)
  {
    return new SimpleXlsExporterConfiguration();
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.report.domain.export.AbstractJRExporterBasedExporter#createReportConfiguration(de.quinscape.intrexx.report.ReportContext)
   */
  @Override
  protected XlsReportConfiguration createReportConfiguration(ReportContext context)
  {
    return new SimpleXlsReportConfiguration();
  }
}
