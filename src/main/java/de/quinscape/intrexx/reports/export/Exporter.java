/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.export;

import java.io.OutputStream;

import de.quinscape.intrexx.reports.ReportContext;
import de.quinscape.intrexx.reports.domain.MainReportService;

import net.sf.jasperreports.engine.JasperPrint;

/**
 * Exportiert einen bereits gefüllten Bericht in ein bestimmtes Format und
 * stellt Informationen über das Format bereit.
 * <p>
 * Service-Interface: Implementationen müssen wie ein Service zustandslos
 * und Thread-sicher sein. Die jeweilige Implementation bestimmt in welches
 * Format und unter welchen Voraussetzungen exportiert wird.
 * 
 * @author Jörg Gottschling
 */
public interface Exporter
{

  /**
   * Exportiert einen bereits gefüllten Bericht.
   * 
   * @param context
   *          Der aktuelle {@link ReportContext}.
   * 
   * @param print
   *          Der zu exportierende Report als {@link JasperPrint}.<br>
   *          TODO: JasperPrint im Context ablegen?
   * 
   * @param target
   *          Der {@link OutputStream} in den der Bericht exportiert werden
   *          soll.
   */
  void export(ReportContext context, JasperPrint print, OutputStream target);

  /**
   * Der Name des Zeichensatzes ("character encoding") in dem der Export
   * kodiert wird.
   */
  String getCharacterEncoding();

  /**
   * Der Inhaltstyp (der "HTTP content type" oder auch "MimeType") des
   * Exportformat als {@link String}. Wird zum Beispiel für das Speichern
   * von Dateien in Intrexx oder das direkte Ausliefern per Servlet
   * benötigt.
   */
  String getContentType();

  /**
   * Die übliche Erweiterung für Dateien des Formats dieses Exports. Gemeint
   * ist die Erweiterung nach dem Punkt im Dateinamen. Zum Beispiel "pdf" in
   * "Bericht2009.pdf".
   */
  String getFileExtension();

  /**
   * Ein Identifier für das Exportformat als {@link String}. Laut Konvention
   * einfach die übliche Abkürzung und/oder Dateierweiterung des Formats in
   * Großbuchstaben. Zum Beispiel "PDF", "XLS" oder "DOCX". Wird von
   * {@link MainReportService} allerdings wegen Abwärtskompatibilität
   * case-insensitive genutzt.
   */
  String getFormatId();
}
