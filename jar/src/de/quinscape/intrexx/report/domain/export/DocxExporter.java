/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain.export;

import de.quinscape.intrexx.report.*;

import net.sf.jasperreports.engine.export.ooxml.*;
import net.sf.jasperreports.export.*;

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
   * @see de.quinscape.intrexx.report.domain.export.AbstractJRExporterBasedExporter#createReportConfiguration(de.quinscape.intrexx.report.ReportContext)
   */
  @Override
  protected DocxReportConfiguration createReportConfiguration(ReportContext context)
  {
    return new SimpleDocxReportConfiguration();
  }

  /**
   * {@inheritDoc}
   * @see de.quinscape.intrexx.report.domain.export.AbstractJRExporterBasedExporter#createExporterConfiguration(de.quinscape.intrexx.report.ReportContext)
   */
  @Override
  protected DocxExporterConfiguration createExporterConfiguration(ReportContext context)
  {
    return new SimpleDocxExporterConfiguration();
  }

}
