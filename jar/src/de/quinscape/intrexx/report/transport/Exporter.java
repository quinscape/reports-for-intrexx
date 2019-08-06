/*
 * (C) Copyright 2013 QuinScape GmbH
 * All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.transport;

import java.io.*;
import java.nio.file.Path;
import java.text.*;
import java.util.*;

import javax.xml.bind.*;

import org.apache.commons.codec.digest.*;

import de.uplanet.io.*;
import de.uplanet.lucy.server.businesslogic.*;
import de.uplanet.lucy.server.businesslogic.util.*;
import de.uplanet.lucy.server.portalserver.*;
import de.uplanet.lucy.server.rtcache.*;
import de.uplanet.lucy.server.util.*;

import de.quinscape.intrexx.report.transport.jaxb.v1.*;

import static de.quinscape.intrexx.report.ixbl.IxblHelper.*;

/**
 * Exportiert die ausgewählten Reports und ergänzt diese Datei zum Datensatz.
 * 
 * @author Markus Vollendorf
 */
public class Exporter
    extends AbstractTransportPageActionHandler
{

  private static final float formatVersion = 1.0f;

  private static final SimpleDateFormat exportFilenameFormatter = new SimpleDateFormat(
      "yyyy-MM-dd HH-mm 'Reportsexport'");

  /**
   * {@inheritDoc}
   * 
   * @see de.uplanet.lucy.server.businesslogic.handler.IPageActionHandler#processAfter(de.uplanet.lucy.server.businesslogic.IBusinessLogicProcessingContext,
   *      de.uplanet.lucy.server.businesslogic.util.IDataRecord,
   *      de.uplanet.lucy.server.rtcache.PageInfo, java.lang.String)
   */
  @Override
  public void processAfter(IBusinessLogicProcessingContext intrexxContext, IDataRecord dataRecord,
      PageInfo pageInfo,
      String actionType)
      throws BlException
  {

    // Hinterlegt den Kontext für weitere Abfragen
    Export export = new Export();
    export.setVersion(formatVersion);

    // Selektierte Reports
    for(String s : getIncludedReports(dataRecord.getRecId()))
      export.getReports().add(createReport(s));

    // Ressourcen
    HashSet<String> resourceGuids = new HashSet<String>();
    for(Report r : export.getReports())
      for(ReportResource reportResource : r.getResources())
        resourceGuids.add(reportResource.getRefResource());
    for(String resourceGuid : resourceGuids)
      export.getResources().add(createResource(resourceGuid));

    // Dateiname
    String exportDateiname = getTypedValue(dataRecord, "filename", String.class);
    exportDateiname = IOHelper.cleanupFileName(exportDateiname);
    if(exportDateiname == null || "".equals(exportDateiname))
    {
      exportFilenameFormatter.setTimeZone(intrexxContext.getUserTimeZone());
      exportDateiname = exportFilenameFormatter.format(new Date());
    }
    if(!exportDateiname.toLowerCase().endsWith(".zip"))
      exportDateiname += ".zip";

    // Kommentar zum Export
    export.setComment(getTypedValue(dataRecord, "comment", String.class));

    // Eigentlicher Export
    try
    {
      File internalTmp = PortalPath.get(PortalPath.TMP_DIR).toFile();
      Path zipTmp = IOHelper.createTempDirectory(internalTmp, "reportsExport", null).toPath();
      Marshaller m = getMarshaller();
      m.marshal(export, new File(zipTmp.toFile(), "reports.xml"));
      for(Resource r : export.getResources())
      {
        File srcFile = new File(r.getUrl());
        IOHelper.copyFile(new File(zipTmp.toFile(), srcFile.getName()), srcFile);
      }
      Path zipFile = IOHelper.getUniqueFileName(internalTmp, exportDateiname).toPath();
      ZipHelper.zipDir(zipFile, zipTmp);
      setFileValueHolder("qsrep_packages", "file_package", dataRecord.getRecId(), zipFile.toFile(), exportDateiname);
      IOHelper.deleteFileRecursively(zipTmp);
      IOHelper.deleteFile(zipFile.toFile());
    }
    catch(Exception exc)
    {
      throw new BlException("Error in processAfter.", exc);
    }

  }

  /**
   * Exportiert einen Bericht mit zugeordneten Ressourcen.
   * 
   * @param reportGuid Die GUID des Berichts
   * @return Den Bericht als {@link Report}
   */
  private Report createReport(String reportGuid)
  {

    // Zusammenstellen des Reports
    IRow matchingReport = getRow("qsrep_reports", "guid", reportGuid);
    Report jaxbReport = new Report();
    jaxbReport.setBeschreibung(getTypedValue(matchingReport, "beschreibung", String.class));
    jaxbReport.setDefaultConnection(getTypedValue(matchingReport, "default_connection",
        String.class));
    jaxbReport.setGuid(getTypedValue(matchingReport, "guid", String.class));
    jaxbReport.setName(getTypedValue(matchingReport, "name", String.class));
    jaxbReport.setPermissions(getTypedValue(matchingReport, "permissions", String.class));
    jaxbReport.setRefMainReport(getTypedValue(matchingReport, "ref_main_report", String.class));

    // Ressourcenzuordnungen des Berichts
    List<IRow> dependantResources =
        getRows(
            "qsrep_resource_alloc", "fkguid", reportGuid);
    for(IRow r : dependantResources)
    {
      ReportResource jaxbReportResource = new ReportResource();
      jaxbReportResource.setGuid(getTypedValue(r, "guid", String.class));
      jaxbReportResource.setRefResource(getTypedValue(r, "ref_resource", String.class));
      jaxbReport.getResources().add(jaxbReportResource);
    }

    // Rückgabe des gesamten Reports
    return jaxbReport;

  }

  /**
   * Exportiert eine Resource mit Informationen über die entsprechende Datei.
   * 
   * @param resourceGuid
   * @return Resource
   * @throws BlException
   */
  private Resource createResource(String resourceGuid)
      throws BlException
  {
    Resource jaxbResource = new Resource();
    IRow resourceRow = getRow("qsrep_resources", "guid", resourceGuid);
    jaxbResource.setGuid(getTypedValue(resourceRow, "guid", String.class));
    jaxbResource.setName(getTypedValue(resourceRow, "name", String.class));
    IVHFileDescriptor resourceFile =
        getFileValueHolder("qsrep_resources", "file_resource", resourceGuid).getFirstFile();
    jaxbResource.setFilename(resourceFile.getFileName());
    jaxbResource.setUrl(resourceFile.getPath());
    File file = new File(resourceFile.getPath());
    jaxbResource.setSize(file.length());
    try
    {
      jaxbResource.setMd5(DigestUtils.md5Hex(new FileInputStream(file)));
    }
    catch(FileNotFoundException exc)
    {
      throw new BlException("File '" + resourceFile.getFileName() + "' is missing.", exc);
    }
    catch(IOException exc)
    {
      throw new BlException("Error adding file '" + resourceFile.getFileName() + "'.", exc);
    }
    return jaxbResource;
  }

}
