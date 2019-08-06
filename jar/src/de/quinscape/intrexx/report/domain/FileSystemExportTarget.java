/*
 * (C) Copyright 2015 QuinScape GmbH
 * All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain;

import static org.springframework.util.StringUtils.hasText;

import java.io.*;

import de.quinscape.intrexx.report.*;

/**
 * <p>
 * FileSystemExportTarget.
 * </p>
 * 
 * DOKU: FileSystemExportTarget
 *
 * @author Markus Vollendorf
 */
public class FileSystemExportTarget
    implements ExportTarget
{

  private String filename;

  private String path;

  /**
   * Setzt {@link #filename}.
   *
   * @param filename
   *          <code>String</code> als neuen Wert
   *          für <code>FileSystemExportTarget.filename</code>.
   */
  public void setFilename(String filename)
  {
    this.filename = filename;
  }

  /**
   * Setzt {@link #path}.
   *
   * @param path
   *          <code>String</code> als neuen Wert
   *          für <code>FileSystemExportTarget.path</code>.
   */
  public void setPath(String path)
  {
    this.path = path;
  }

  /**
   * Liefert {@link #filename}.
   *
   * @return Liefert <code>FileSystemExportTarget.filename</code> als <code>String</code>.
   */
  public String getFilename()
  {
    return filename;
  }

  /**
   * Liefert {@link #path}.
   *
   * @return Liefert <code>FileSystemExportTarget.path</code> als <code>String</code>.
   */
  public String getPath()
  {
    return path;
  }

  /**
   * Erzeugt eine Instanz von <code>FileSystemExportTarget</code>.
   *
   */
  public FileSystemExportTarget(String path, String filename)
  {
    this.path = path;
    this.filename = filename;
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.report.domain.ExportTarget#createOutputStream(de.quinscape.intrexx.report.ReportContext,
   *      de.quinscape.intrexx.report.domain.Exporter)
   */
  @Override
  public OutputStream createOutputStream(ReportContext context, Exporter exporter)
      throws Exception
  {
    if(!hasText(filename)) filename = context.getReport().getDisplayName();

    // TODO: Verzeichnis erstellen, ggf. Berechtigungsprüfung
    /*
     * url = new File("internal/tmp/" + context.getSession().getId() + '/' + filename + '_'
     * + currentTimeMillis() + '.' + exporter.getFileExtension());
     */
    File url = new File(path, filename + '.' + exporter.getFileExtension());

    if(!url.getParentFile().exists())
      url.getParentFile().mkdirs();

    return new FileOutputStream(url);
  }

  /**
   * {@inheritDoc}
   * 
   * @see de.quinscape.intrexx.report.domain.ExportTarget#afterExport(de.quinscape.intrexx.report.ReportContext,
   *      de.quinscape.intrexx.report.domain.Exporter, java.io.OutputStream)
   */
  @Override
  public void afterExport(ReportContext context, Exporter exporter, OutputStream os)
      throws Exception
  {
    os.close();
  }

}
