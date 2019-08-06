/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain.export;

import static org.apache.commons.lang.CharEncoding.UTF_8;
import static org.springframework.util.StringUtils.hasText;

import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import de.quinscape.intrexx.report.IntrexxReportException;
import de.quinscape.intrexx.report.ReportContext;
import de.quinscape.intrexx.report.domain.Exporter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.SimpleJasperReportsContext;
import net.sf.jasperreports.engine.export.JRHyperlinkProducer;
import net.sf.jasperreports.engine.export.JRHyperlinkProducerFactory;
import net.sf.jasperreports.export.ExporterConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.ReportExportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleReportExportConfiguration;

/**
 * Ein abstrakter {@link Exporter} basierend auf {@link net.sf.jasperreports.export.Exporter}.
 * Unterklassen können {@link #createExporter(ReportContext, JasperReportsContext)} und
 * {@link #configureExporter(ReportContext, net.sf.jasperreports.export.Exporter, ReportExportConfiguration, ExporterConfiguration, ExporterOutput)}
 * implementiert. Außerdem sollten <code>formatId</code> und <code>contentType</code> konfiguriert
 * werden. Die <code>fileExtension</code> wird standardmäßig aus der <code>formatId</code>
 * abgeleitet.
 * 
 * @author Jörg Gottschling
 */
public abstract class AbstractJRExporterBasedExporter<E extends net.sf.jasperreports.export.Exporter<I, RC, C, O>, I extends ExporterInput, RC extends ReportExportConfiguration, C extends ExporterConfiguration, O extends ExporterOutput>
    implements Exporter, InitializingBean
{

  private String characterEncoding = UTF_8;

  private String contentType;

  private String fileExtension;

  private String formatId;

  private JRHyperlinkProducerFactory hyperlinkProducerFactory;

  private Class<E> jrExporterClass;

  /**
   * Konstruktor ohne Initialisierung.
   */
  protected AbstractJRExporterBasedExporter()
  {
    // empty
  }

  /**
   * Konstruktor mit formatId und contentType.
   */
  protected AbstractJRExporterBasedExporter(String formatId, String contentType)
  {
    this();
    this.formatId = formatId;
    this.contentType = contentType;
    this.fileExtension = formatId.toLowerCase();
  }

  /**
   * Konstruktor mit formatId und contentType.
   */
  protected AbstractJRExporterBasedExporter(String formatId,
      String contentType, Class<E> jrExporterClass)
  {
    this(formatId, contentType);
    this.jrExporterClass = jrExporterClass;
  }

  @Override
  public void afterPropertiesSet()
      throws Exception
  {
    Assert.hasText(formatId, "formatId not set.");
    Assert.hasText(contentType, "contentType not set.");
  }

  @Override
  public final void export(ReportContext context, JasperPrint print, OutputStream target)
  {
    Assert.notNull(print, "print was null");
    Assert.notNull(target, "target was null");

    Log log = LogFactory.getLog(getClass());
    log.debug("Starting export of " + print.getName());

    try
    {
      // erzeugen und spezifisch vorkonfigurieren

      SimpleJasperReportsContext jrc = new SimpleJasperReportsContext();

      RC reportConfig = createReportConfiguration(context);
      C exporterConfig = createExporterConfiguration(context);
      O output = createOutput(target, characterEncoding);

      // generelle Parameter
      if(hyperlinkProducerFactory != null)
        ((SimpleReportExportConfiguration)reportConfig).setHyperlinkProducerFactory(hyperlinkProducerFactory);

      // Formatspezifische Parameter
      E exporter = createExporter(context, jrc);

      // Quelle, Ziel, Export
      exporter.setConfiguration(exporterConfig);
      exporter.setConfiguration(reportConfig);
      configureExporter(context, exporter, reportConfig, exporterConfig, output);
      exporter.setExporterOutput(output);
      exporter.setExporterInput(createInput(print));

      exporter.exportReport();
    }
    catch(JRException exc)
    {
      throw new IntrexxReportException("Exception while export of "
                                       + print.getName(), exc);
    }

    log.debug("Finished export of " + print.getName());
  }

  @Override
  public String getCharacterEncoding()
  {
    return characterEncoding;
  }

  @Override
  public String getContentType()
  {
    return contentType;
  }

  @Override
  public String getFileExtension()
  {
    return fileExtension;
  }

  @Override
  public String getFormatId()
  {
    return formatId;
  }

  /**
   * Siehe {@link Exporter#getCharacterEncoding()}. Standard ist UTF-8.
   */
  public void setCharacterEncoding(String characterEncoding)
  {
    Assert.hasLength(characterEncoding, "characterEncoding is empty.");
    this.characterEncoding = characterEncoding;
  }

  /**
   * Siehe {@link Exporter#getContentType()}.
   */
  public void setContentType(String contentType)
  {
    this.contentType = contentType;
  }

  /**
   * Siehe {@link Exporter#getFileExtension()}. Wird, wenn nicht konfiguriert, aus
   * {@link Exporter#getFormatId()} hergeleitet.
   */
  public void setFileExtension(String fileExtension)
  {
    this.fileExtension = fileExtension;
  }

  /**
   * Siehe {@link Exporter#getFormatId()}.
   */
  public void setFormatId(String formatId)
  {
    this.formatId = formatId;
    // nur wenn noch nicht explizit gesetzt
    // Kann auch durch den Konstruktor schon geschehen sein.
    if(hasText(fileExtension)) fileExtension = formatId.toLowerCase();
  }

  /**
   * Die {@link JRHyperlinkProducerFactory} die während des Exports für verschiedene Hyperlinktypen
   * entsprechende {@link JRHyperlinkProducer} erzeugt. Letztere erzeugen dann die eigentlichen
   * Hyperlinks. Dies ist vermutlich wichtig für "drill down".
   * 
   * @see ReportExportConfiguration#getHyperlinkProducerFactory()
   * @see JRHyperlinkProducerFactory
   * @see JRHyperlinkProducer
   */
  public void setHyperlinkProducerFactory(
      JRHyperlinkProducerFactory hyperlinkProducerFactory)
  {
    this.hyperlinkProducerFactory = hyperlinkProducerFactory;
  }

  /**
   * Konfiguriert einen {@link net.sf.jasperreports.export.Exporter} mit den für das Format
   * benötigten individuellen Eigenschaften. Setzt aber <i>keine</i> allgemeine Eigenschaften wie
   * Quelle und Ziel des Exports und startet auch <i>nicht</i> den Export.
   * <p>
   * Leere Implementierung die nichts tut.
   * 
   * @param context
   *          Der aktuelle {@link ReportContext}.
   * @param exporter
   *          Der zu konfigurierende Exporter.
   */
  @SuppressWarnings("unused")
  protected void configureExporter(ReportContext context, E exporter, RC reportConfiguration,
      C exportConfiguration, O output)
  {
    // leer
  }

  /**
   * Erzeugt einen passenden {@link net.sf.jasperreports.export.Exporter}. Konfiguriert nichts.
   * <p>
   * Sollte entweder von Unterklassen implementiert werden oder diese müssen die
   * <code>jrExporterClass</code> im Konstruktor
   * {@link #AbstractJRExporterBasedExporter(String, String, Class)} übergeben. In letzterem Fall
   * wird dann der Exporter via Reflection erzeugt.
   * 
   * @param context
   *          Der aktuelle {@link ReportContext}.
   * @param jrcontext
   *          Der aktuelle {@link JasperReportsContext}.
   */
  protected E createExporter(ReportContext context, JasperReportsContext jrcontext)
  {
    Assert.state(jrExporterClass != null,
        "Subclasses have to configure a jrExporterClass or implement this method.");
    try
    {
      return jrExporterClass.getConstructor().newInstance();
    }
    catch(Exception exc)
    {
      throw new IntrexxReportException("Could not instantiate class:" + jrExporterClass.getName(),
          exc);
    }
  }

  /**
   * Erzeugt eine passende {@link ReportExportConfiguration}. Konfiguriert nichts.
   * <p>
   *
   * @param context
   *          Der aktuelle {@link ReportContext}.
   */
  protected abstract RC createReportConfiguration(ReportContext context);

  /**
   * Erzeugt eine passende {@link ExporterConfiguration}. Konfiguriert nichts.
   * <p>
   *
   * @param context
   *          Der aktuelle {@link ReportContext}.
   */
  protected abstract C createExporterConfiguration(ReportContext context);

  @SuppressWarnings("unchecked")
  protected I createInput(JasperPrint print)
  {
    return (I)new SimpleExporterInput(print);
  }

  /**
   * <p>
   * Erzeugt einen passenden {@link ExporterOutput}. Konfiguriert nichts
   * </p>
   * 
   * Standard-Implemetierung, die einen {@link SimpleOutputStreamExporterOutput} erzeugt.
   * Unterklassen, deren ExporterOutput-Typ nicht mit {@link OutputStreamExporterOutput} kompatibel
   * ist, müssen diese Methode überschreiben!
   * 
   * @param target
   *          OutputStream, in den der Report exportiert wird.
   * @param characterEncoding
   *          Zeichensatz für den Export.
   */
  @SuppressWarnings({"unchecked", "hiding", "unused"})
  protected O createOutput(OutputStream target, String characterEncoding)
  {
    return (O)new SimpleOutputStreamExporterOutput(target);
  }
}
