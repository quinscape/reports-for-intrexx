
package de.quinscape.intrexx.reports.domain;

import static java.lang.System.currentTimeMillis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.lang.time.FastDateFormat;

import de.quinscape.intrexx.reports.ReportContext;
import de.quinscape.intrexx.reports.export.ExportTarget;
import de.quinscape.intrexx.reports.export.Exporter;

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
