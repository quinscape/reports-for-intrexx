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

import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.OdsExporterConfiguration;
import net.sf.jasperreports.export.OdsReportConfiguration;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleOdsExporterConfiguration;
import net.sf.jasperreports.export.SimpleOdsReportConfiguration;

/**
 * Exportiert einen Bericht in das Textformat
 * "OpenDocument-Tabellendokument" - kurz ODS. Dieses Format ist Teil der
 * Formatfamilie "OASIS Open Document Format for Office Applications" - kurz
 * ODF.
 * 
 * @author Jörg Gottschling
 */
public class OdsExporter
    extends
    AbstractJRExporterBasedExporter<JROdsExporter, ExporterInput, OdsReportConfiguration, OdsExporterConfiguration, OutputStreamExporterOutput>
{

  /**
   * Erzeugt eine initialisierte Instanz.
   */
  public OdsExporter()
  {
    super("ODS", "application/vnd.oasis.opendocument.spreadsheet", JROdsExporter.class);
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createReportConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected OdsReportConfiguration createReportConfiguration(ReportContext context)
  {
    return new SimpleOdsReportConfiguration();
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.reports.export.AbstractJRExporterBasedExporter#createExporterConfiguration(de.quinscape.intrexx.reports.ReportContext)
   */
  @Override
  protected OdsExporterConfiguration createExporterConfiguration(ReportContext context)
  {
    return new SimpleOdsExporterConfiguration();
  }
}
