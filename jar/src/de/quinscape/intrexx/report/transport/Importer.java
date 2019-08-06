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

import static de.quinscape.intrexx.report.ixbl.IxblHelper.*;

import java.io.*;
import java.util.*;

import org.apache.commons.codec.digest.*;
import org.apache.commons.logging.*;

import de.uplanet.io.*;
import de.uplanet.lucy.server.businesslogic.*;
import de.uplanet.lucy.server.businesslogic.util.*;
import de.uplanet.lucy.server.dataobjects.*;
import de.uplanet.lucy.server.rtcache.*;
import de.uplanet.util.*;

import de.quinscape.intrexx.report.transport.jaxb.v1.*;

/**
 * Stellt die Liste der beinhalteten Reports bereit und importiert diese in einem zweiten Schritt
 * auf Aufforderung.
 * 
 * @author Markus Vollendorf, Jasper Lauterbach
 */
public class Importer
    extends AbstractTransportPageActionHandler
{

  /**
   * Logger for this class
   */
  private static final Log log = LogFactory.getLog(Importer.class);

  @Override
  public void processAfter(IBusinessLogicProcessingContext intrexxContext, IDataRecord dataRecord,
      PageInfo pageInfo, String actionType)
      throws BlException
  {
    // TODO: Besser in zwei unterschiedliche Handler auslagern, statt hier Fallunterscheidung
    String qsAction = intrexxContext.getRequest().get("rq_qsAction");

    IFileValueHolder uploadFile =
        getFileValueHolder("qsrep_packages", "file_package", dataRecord.getRecId());
    File unzipTmpFolder = unzipFileToTempFolder(uploadFile.getFirstFile().getPath());
    validateXmlFiles(unzipTmpFolder);
    Export export = unmarshalPackage(unzipTmpFolder);

    // Aktualisierung der Pfade auf den aktuellen Import
    for(Resource resource : export.getResources())
    {
      FileInputStream fileInputStream = null;
      try
      {
        File file = fileFromResource(unzipTmpFolder, resource);
        resource.setUrl(file.getPath());
        fileInputStream = new FileInputStream(file);
        if(!resource.getSize().equals(file.length()))
          throw new RuntimeException(
              "Resource " + resource.getFilename()
                  + " corrupt: declared size is not equal to actual size (" + resource.getSize()
                  + " <> " + file.length() + ")");
        else if(!resource.getMd5().equals(DigestUtils.md5Hex(fileInputStream)))
          throw new RuntimeException("Resource " + resource.getFilename()
                                     + " corrupt: declared MD5 and actual MD5 do not match.");
      }
      catch(Exception e)
      {
        log.error(e.getMessage(), e);
      }
      finally
      {
        Safely.close(fileInputStream);
      }
    }

    // Schritt 1: Auslesen der Datei und Bereitstellung zur Auswahl
    if("READ".equals(qsAction))
    {
      for(Report report : export.getReports())
      {
        IDataRecord containedReportRecord = getDataRecord("qsrep_packages_reports", "-1");
        setValue(containedReportRecord, "fklid", dataRecord.getRecId());
        setValue(containedReportRecord, "name", report.getName());
        setValue(containedReportRecord, "guid", report.getGuid());
        parseDataRecord(containedReportRecord);
      }
    }

    // Schritt 2: Importieren der ausgewählten Berichte
    if("IMPORT".equals(qsAction))
    {

      // Sammeln der Ressourcen zu den Reports
      HashSet<String> resourceGuids = new HashSet<String>();
      for(String s : getSplitValues(intrexxContext.getRequest().get("rq_qsSelection")))
      {
        Report report = findReportBySelectionId(s, export.getReports());
        for(ReportResource reportResource : report.getResources())
          resourceGuids.add(reportResource.getRefResource());
      }

      // Erstellen dieser Ressourcen
      for(Resource resource : export.getResources())
        if(resourceGuids.contains(resource.getGuid()))
          insertResource(resource);

      // Erstellen der Reports
      StringBuilder reportList = new StringBuilder();
      for(String s : getSplitValues(intrexxContext.getRequest().get("rq_qsSelection")))
      {
        Report report = findReportBySelectionId(s, export.getReports());
        insertReport(report);
        deleteRows("qsrep_resource_alloc", "fkguid", report.getGuid());
        for(ReportResource reportResource : report.getResources())
          insertReportResource(report, reportResource);
        reportList.append(report.getName());
        reportList.append("; ");
      }

      IDataRecord importRecord = getDataRecord("qsrep_packages", dataRecord.getRecId());
      setValue(importRecord, "imported_reports", reportList.toString().replaceAll(", $", ""));
      setValue(importRecord, "comment", export.getComment());
      parseDataRecord(importRecord);
    }

    // Entfernen des Imports
    IOHelper.deleteFileRecursively(unzipTmpFolder);

  }

  /**
   * Liefert ein {@link File} anhand des temporären Verzeichnisses und der Ressourcen-URL.
   * 
   * @param unzipTmp Das temporäre Verzeichnis, in das zuvor das Paket entpackt wurde.
   * @param resource Die {@link Resource}, deren URL genutzt werden soll.
   * @return Die Datei als {@link File}
   */
  protected static File fileFromResource(File unzipTmp, Resource resource)
  {
    String url = resource.getUrl();
    int pos = url.indexOf("internal");
    String filename = null;
    if(pos >= 0)
    {
      String separator = url.substring(pos + "internal".length(), pos + "internal".length() + 1);
      filename = url.substring(url.lastIndexOf(separator) + 1);
    }
    else
      filename = url;
    return new File(unzipTmp, filename);
  }

  /**
   * Sucht einen Report anhand der ID in der Unterdatengruppe (Auflösen einer Selektion).
   * 
   * @param reportId
   * @param reports
   * @return Der entsprechende Report oder <i>null</i>
   */
  private Report findReportBySelectionId(String reportId, List<Report> reports)
  {
    IRow r = getRow("qsrep_packages_reports", "id", reportId);
    Report search = null;
    for(Report report : reports)
      if(report.getGuid().equals(getTypedValue(r, "guid", String.class)))
        search = report;
    return search;
  }

  private void insertReport(Report report)
  {
    IDataRecord reportRecord = getDataRecord("qsrep_reports", report.getGuid());
    setValue(reportRecord, "beschreibung", report.getBeschreibung());
    setValue(reportRecord, "permissions", report.getPermissions());
    setValue(reportRecord, "name", report.getName());
    setValue(reportRecord, "default_connection", report.getDefaultConnection());
    setValue(reportRecord, "ref_main_report", report.getRefMainReport());
    parseDataRecord(reportRecord);
  }

  private void insertReportResource(Report report, ReportResource reportResource)
  {
    IDataRecord reportResourceRecord =
        getDataRecord("qsrep_resource_alloc", reportResource.getGuid());
    reportResourceRecord.setParentId(report.getGuid());
    setValue(reportResourceRecord, "ref_resource", reportResource.getRefResource());
    parseDataRecord(reportResourceRecord);
  }

  private void insertResource(Resource resource)
  {
    IDataRecord resourceRecord = getDataRecord("qsrep_resources", resource.getGuid());
    setValue(resourceRecord, "name", resource.getName());
    File f = new File(resource.getUrl());
    parseDataRecord(resourceRecord);
    setFileValueHolder("qsrep_resources", "file_resource", resourceRecord.getRecId(), f,
        resource.getFilename());
  }

}
