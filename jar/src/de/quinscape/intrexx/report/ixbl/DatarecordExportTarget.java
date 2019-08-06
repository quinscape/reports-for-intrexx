/*
 * (C) Copyright 2011 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.ixbl;

import static java.lang.System.currentTimeMillis;
import static org.springframework.util.StringUtils.hasText;

import java.io.*;

import de.uplanet.lucy.server.businesslogic.util.*;
import de.uplanet.lucy.server.rtcache.*;

import de.quinscape.intrexx.report.*;
import de.quinscape.intrexx.report.domain.*;

/**
 * <p>
 * DatarecordExportTarget.
 * </p>
 * 
 * DOKU: DatarecordExportTarget
 * 
 * Die Datei wird bei Export zunächst unter
 * {@code internal/tmp/<Session-ID>/<filename>_<Millisekunden>.<Endung>} erzeugt und dann über die
 * FileHelper-API von Intrexx importiert.
 * 
 * @author jgottschling
 * @author nberger
 */
public class DatarecordExportTarget
    implements ExportTarget
{

  private final DataGroupInfo datagroup;

  private final String recId;

  private final FieldInfo field;

  private String filename;

  private File url;

  /**
   * Erzeugt ein {@link DatarecordExportTarget} mit einem Zieldatensatz und
   * einem Zieldatenfeld.
   * 
   * @since 6.0.1
   */
  public DatarecordExportTarget(DataGroupInfo datagroup, String recId, FieldInfo field)
  {
    super();
    this.datagroup = datagroup;
    this.recId = recId;
    this.field = field;
    this.filename = null;
  }

  /**
   * Erzeugt ein {@link DatarecordExportTarget} mit einem Zieldatensatz,
   * einem Zieldatenfeld und einem Dateinamen.
   * 
   * @since 6.0.1
   */
  public DatarecordExportTarget(DataGroupInfo datagroup, String recId, FieldInfo field,
      String filename)
  {
    super();
    this.datagroup = datagroup;
    this.recId = recId;
    this.field = field;
    this.filename = filename;
  }

  @Override
  public void afterExport(ReportContext context, Exporter exporter,
      OutputStream os)
      throws Exception
  {
    os.close();

    context.ix().getSharedState().putAt(ReportDataHandler.FILEHELPER_INVOKED, true);
    FileUCHelper.copyFileToIntrexx(context.ix(), url, field.getGuid(), recId,
        filename + '.' + exporter.getFileExtension(), exporter.getContentType(), false);
  }

  @Override
  public OutputStream createOutputStream(ReportContext context,
      Exporter exporter)
      throws Exception
  {
    if(!hasText(filename)) filename = context.getReport().getDisplayName();

    url = new File("internal/tmp/" + context.getSession().getId() + '/' + filename + '_'
                   + currentTimeMillis() + '.' + exporter.getFileExtension());

    if(!url.getParentFile().exists())
      url.getParentFile().mkdirs();

    return new FileOutputStream(url);
  }

  /**
   * Liefert {@link #field}.
   * Seit 6.0.1. Vorher QXFC-Datafield
   * 
   * @return Liefert <code>DatarecordExportTarget.field</code> als <code>FieldInfo</code>.
   */
  public FieldInfo getField()
  {
    return field;
  }

  /**
   * Liefert {@link #filename}.
   * 
   * @return Liefert <code>DatarecordExportTarget.filename</code> als <code>String</code>.
   */
  public String getFilename()
  {
    return filename;
  }

  /**
   * Liefert {@link #recId}.
   * 
   * @return Liefert <code>DatarecordExportTarget.recId</code> als <code>String</code>.
   * @since 6.0.1
   */
  public String getRecId()
  {
    return recId;
  }

  /**
   * Liefert {@link #datagroup}.
   * 
   * @return Liefert <code>DatarecordExportTarget.datagroup</code> als <code>DataGroupInfo</code>.
   * @since 6.0.1
   */
  public DataGroupInfo getDatagroup()
  {
    return datagroup;
  }
}
