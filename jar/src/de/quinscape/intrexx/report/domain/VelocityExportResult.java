
package de.quinscape.intrexx.report.domain;

import static java.lang.System.currentTimeMillis;

import java.io.*;

import org.apache.commons.lang.time.*;

import de.quinscape.intrexx.report.*;

/**
 * Enthält alle Informationen zum Ergebnis eines Exports für Velocity.
 * 
 * @author Jörg Gottschling
 */
public class VelocityExportResult
    implements ExportTarget
{

  private static final FastDateFormat dateFormat =
      FastDateFormat.getInstance("yyyyMMdd-HHmmss");

  private String externalFileUrl;

  private Exporter exporter;

  /** Die Datei URL für den Zugriff von außen. */
  public String getExternalFileUrl()
  {
    return externalFileUrl;
  }

  /** Das ExportFormat. */
  public String getFormat()
  {
    return exporter.getFormatId();
  }

  /**
   * Siehe {@link Exporter#getCharacterEncoding()}.
   */
  public String getCharacterEncoding()
  {
    return exporter.getCharacterEncoding();
  }

  /**
   * Siehe {@link Exporter#getContentType()}.
   */
  public String getContentType()
  {
    return exporter.getContentType();
  }

  /**
   * Siehe {@link Exporter#getFileExtension()}.
   */
  public String getFileExtension()
  {
    return exporter.getFileExtension();
  }

  /**
   * Siehe {@link Exporter#getFormatId()}.
   */
  public String getFormatId()
  {
    return exporter.getFormatId();
  }

  @Override
  public void afterExport(ReportContext context, Exporter pExporter,
      OutputStream os)
      throws Exception
  {
    os.close();
  }

  @Override
  public OutputStream createOutputStream(ReportContext context,
      Exporter pExporter)
      throws Exception
  {
    this.exporter = pExporter;

    String sessionFolderName = "tmp/" + context.getSession().getId() + "/";

    String fileName =
        context.getReport().getMainJasperReport().getName() + '-'
            + dateFormat.format(currentTimeMillis()) + '.'
            + exporter.getFileExtension();

    externalFileUrl = sessionFolderName + "/" + fileName;

    File sessionFolder = new File("external/htmlroot/", sessionFolderName);
    if(!sessionFolder.exists()) sessionFolder.mkdirs();

    return new FileOutputStream(new File(sessionFolder, fileName));
  }
}
