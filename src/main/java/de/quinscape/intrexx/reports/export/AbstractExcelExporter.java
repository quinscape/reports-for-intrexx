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

import net.sf.jasperreports.engine.export.JRExporterContext;
import net.sf.jasperreports.engine.export.JRXlsAbstractExporter;
import net.sf.jasperreports.export.AbstractXlsReportConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import net.sf.jasperreports.export.XlsExporterConfiguration;
import net.sf.jasperreports.export.XlsReportConfiguration;

/**
 * Exportiert einen Berichte für Microsoft Excel. Siehe Unterklassen.
 * <p>
 * Implementiert
 * {@link #configureExporter(ReportContext, JRXlsAbstractExporter, XlsReportConfiguration, XlsExporterConfiguration, OutputStreamExporterOutput)}
 * so das Unterklassen nur noch den passenden Exporter erzeugen müssen. Die Parameter sind bei den
 * Excel-Implementierungen eh gleich.
 * 
 * @author Jörg Gottschling
 */
public abstract class AbstractExcelExporter<E extends JRXlsAbstractExporter<RC, C, EC>, RC extends XlsReportConfiguration, C extends XlsExporterConfiguration, EC extends JRExporterContext>
    extends
    AbstractJRExporterBasedExporter<E, ExporterInput, RC, C, OutputStreamExporterOutput>
{

  // default: false
  private boolean collapseRowSpan;

  private boolean detectCellType = true;

  private boolean fontSizeFixEnabled = true;

  // default: false
  private boolean onePagePerSheet;

  private boolean removeEmptySpaceBetweenColumns = true;

  private boolean removeEmptySpaceBetweenRows = true;

  // default: false
  private boolean whitePageBackground;

  /**
   * Erzeugt eine initialisierte Instanz.
   * 
   * @see AbstractJRExporterBasedExporter#AbstractJRExporterBasedExporter(String, String)
   */
  protected AbstractExcelExporter(String formatId, String contentType)
  {
    super(formatId, contentType);
  }

  /**
   * @see #setCollapseRowSpan(boolean)
   */
  public boolean isCollapseRowSpan()
  {
    return collapseRowSpan;
  }

  /**
   * @see #setDetectCellType(boolean)
   */
  public boolean isDetectCellType()
  {
    return detectCellType;
  }

  /**
   * @see #setFontSizeFixEnabled(boolean)
   */
  public boolean isFontSizeFixEnabled()
  {
    return fontSizeFixEnabled;
  }

  /**
   * @see #setOnePagePerSheet(boolean)
   */
  public boolean isOnePagePerSheet()
  {
    return onePagePerSheet;
  }

  /**
   * @see #setRemoveEmptySpaceBetweenColumns(boolean)
   */
  public boolean isRemoveEmptySpaceBetweenColumns()
  {
    return removeEmptySpaceBetweenColumns;
  }

  /**
   * @see #setRemoveEmptySpaceBetweenRows(boolean)
   */
  public boolean isRemoveEmptySpaceBetweenRows()
  {
    return removeEmptySpaceBetweenRows;
  }

  /**
   * @see #collapseRowSpan
   */
  public void setCollapseRowSpan(boolean flag)
  {
    this.collapseRowSpan = flag;
  }

  /**
   * @see SimpleXlsReportConfiguration#isDetectCellType()
   */
  public void setDetectCellType(boolean detectCellType)
  {
    this.detectCellType = detectCellType;
  }

  /**
   * @see SimpleXlsReportConfiguration#isFontSizeFixEnabled()
   */
  public void setFontSizeFixEnabled(boolean fontSizeFixEnabled)
  {
    this.fontSizeFixEnabled = fontSizeFixEnabled;
  }

  /**
   * @see SimpleXlsReportConfiguration#isOnePagePerSheet()
   */
  public void setOnePagePerSheet(boolean flag)
  {
    this.onePagePerSheet = flag;
  }

  /**
   * @see SimpleXlsReportConfiguration#isRemoveEmptySpaceBetweenColumns()
   */
  public void setRemoveEmptySpaceBetweenColumns(
      boolean removeEmptySpaceBetweenColumns)
  {
    this.removeEmptySpaceBetweenColumns = removeEmptySpaceBetweenColumns;
  }

  /**
   * @see SimpleXlsReportConfiguration#isRemoveEmptySpaceBetweenRows()
   */
  public void setRemoveEmptySpaceBetweenRows(boolean removeEmptySpaceBetweenRows)
  {
    this.removeEmptySpaceBetweenRows = removeEmptySpaceBetweenRows;
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
  protected void configureExporter(ReportContext context, E exporter, RC reportConfiguration,
      C exportConfiguration, OutputStreamExporterOutput output)
  {
    AbstractXlsReportConfiguration c = (AbstractXlsReportConfiguration)reportConfiguration;
    c.setOnePagePerSheet(onePagePerSheet);
    c.setRemoveEmptySpaceBetweenColumns(removeEmptySpaceBetweenColumns);
    c.setRemoveEmptySpaceBetweenRows(removeEmptySpaceBetweenRows);
    c.setDetectCellType(detectCellType);
    c.setFontSizeFixEnabled(fontSizeFixEnabled);
    c.setCollapseRowSpan(collapseRowSpan);
    c.setWhitePageBackground(whitePageBackground);
  }

  /**
   * @see #setWhitePageBackground(boolean)
   */
  protected boolean isWhitePageBackground()
  {
    return whitePageBackground;
  }

  /**
   * @see SimpleXlsReportConfiguration#isWhitePageBackground()
   */
  protected void setWhitePageBackground(boolean whitePageBackground)
  {
    this.whitePageBackground = whitePageBackground;
  }
}
