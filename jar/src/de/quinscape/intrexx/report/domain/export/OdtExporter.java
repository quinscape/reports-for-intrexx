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

import net.sf.jasperreports.engine.export.oasis.*;
import net.sf.jasperreports.export.*;

/**
 * Exportiert einen Bericht in das Textformat "OpenDocument-Text" - kurz
 * ODT. Dieses Format ist Teil der Formatfamilie
 * "OASIS Open Document Format for Office Applications" - kurz ODF.
 * 
 * @author Jörg Gottschling
 */
public class OdtExporter
    extends AbstractJRExporterBasedExporter<JROdtExporter, ExporterInput, OdtReportConfiguration, OdtExporterConfiguration, OutputStreamExporterOutput>
{

  /**
   * Erzeugt eine initialisierte Instanz.
   */
  public OdtExporter()
  {
    super("ODT", "application/vnd.oasis.opendocument.text", JROdtExporter.class);
  }

  /**
   * {@inheritDoc}
   * @see de.quinscape.intrexx.report.domain.export.AbstractJRExporterBasedExporter#createReportConfiguration(de.quinscape.intrexx.report.ReportContext)
   */
  @Override
  protected OdtReportConfiguration createReportConfiguration(ReportContext context)
  {
    return new SimpleOdtReportConfiguration();
  }

  /**
   * {@inheritDoc}
   * @see de.quinscape.intrexx.report.domain.export.AbstractJRExporterBasedExporter#createExporterConfiguration(de.quinscape.intrexx.report.ReportContext)
   */
  @Override
  protected OdtExporterConfiguration createExporterConfiguration(ReportContext context)
  {
    return new SimpleOdtExporterConfiguration();
  }
}
