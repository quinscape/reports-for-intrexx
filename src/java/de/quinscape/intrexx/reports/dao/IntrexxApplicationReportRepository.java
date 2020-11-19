/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.dao;

import static de.quinscape.intrexx.reports.ixbl.IxblHelper.getFileValueHolder;
import static de.quinscape.intrexx.reports.ixbl.IxblHelper.getRows;
import static de.quinscape.intrexx.reports.ixbl.IxblHelper.getTypedValue;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.split;
import static org.springframework.util.StringUtils.hasText;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import de.quinscape.intrexx.reports.IntrexxReportException;
import de.quinscape.intrexx.reports.domain.IntrexxApplicationReport;
import de.quinscape.intrexx.reports.domain.ReportsRepository;

import de.uplanet.lucy.server.businesslogic.util.IRow;
import de.uplanet.lucy.server.dataobjects.IFileValueHolder;
import de.uplanet.lucy.server.util.IVHFileDescriptor;

/**
 * Ein {@link ReportsRepository} welches auf die Datenbanktabellen der
 * Intrexx-Applikation "Reports für Intrexx" zugreift.
 * 
 * TODO Datenbanktest
 * 
 * @author Jörg Gottschling
 */
public class IntrexxApplicationReportRepository
    implements ReportsRepository
{

  /**
   * Erzeugt eine Instanz von <code>IntrexxApplicationReportRepository</code>.
   */
  public IntrexxApplicationReportRepository()
  {}

  @Override
  public IntrexxApplicationReport findReport(String reportIdentifier)
  {
    Assert.hasText(reportIdentifier, "The report identifier was null or empty.");

    List<IRow> allReports = new ArrayList<IRow>();
    allReports.addAll(getRows("qsrep_reports", "guid", reportIdentifier));
    allReports.addAll(getRows("qsrep_reports", "name", reportIdentifier));

    if(allReports.size() == 0)
    {
      String msg = "No report with the identifier \"" + reportIdentifier + "\" was found.";
      throw new IntrexxReportException(msg, "NOT_FOUND");
    }
    if(allReports.size() > 1)
    {
      String msg = "Found " + allReports.size() + " reports with the identifier \""
                   + reportIdentifier + "\". The identifier must be unambiguous. "
                   + "The name and the GUID of the report could be used.";
      throw new IntrexxReportException(msg, "AMBIGUOUS_IDENTIFIER");
    }

    IRow selectedReport = allReports.get(0);
    IntrexxApplicationReport report = new IntrexxApplicationReport();
    report.setDisplayName(getTypedValue(selectedReport, "name", String.class));
    report.setGuid(getTypedValue(selectedReport, "guid", String.class));
    setMainReport(selectedReport, report);

    // Setze Datenquelle
    String dataSourceId = getTypedValue(selectedReport, "ref_datasource_id", String.class);
    IntrexxDataSource datasource = readDataSource(dataSourceId);
    report.setDataSource(datasource);

    String strPermissions = getTypedValue(selectedReport, "permissions", String.class);
    if(hasText(strPermissions))
    {
      report.setPermittedOrgGuids(new HashSet<String>(asList(split(
          strPermissions, '|'))));
    }
    if(!report.hasMainReport())
    {
      String message =
          "The report \"" + report.getDisplayName() + "\" has no main resource assigned.";
      throw new IntrexxReportException(message, "MISSING_MAIN_RESOURCE");
    }

    String reportGuid = report.getGuid();
    final List<IRow> resourceRows = getRows("qsrep_resource_alloc", "fkguid", reportGuid);
    Assert.notEmpty(resourceRows,
        "Report hat keine Ressourcen-Zuordnungen: " + report.getDisplayName());
    Map<String, File> allResources = new HashMap<String, File>(resourceRows.size());
    for(IRow resourceRow : resourceRows)
    {
      String resourceGuid = getTypedValue(resourceRow, "ref_resource", String.class);
      Assert.hasText(resourceGuid, "Ressourcen-Zuordnung verweist nicht auf eine Ressource.");
      IFileValueHolder fileValueHolder =
          getFileValueHolder("qsrep_resources", "file_resource", resourceGuid);
      Assert.notNull(fileValueHolder,
          "FileValueHolder zu Ressource konnte nicht gefunden werden: " + resourceGuid);
      Assert.isTrue(fileValueHolder.hasValue(),
          "An dieser Ressource h�ngt keine Datei: " + resourceGuid);
      IVHFileDescriptor fileDescriptor = fileValueHolder.getFirstFile();
      Assert.notNull(fileDescriptor,
          "Obwohl der FileValueHolder einen Wert hat, ist der FileDescriptor null.");
      String fileName = fileDescriptor.getFileName();
      Assert.hasText(fileName, "Ressourcen-Datei hat keinen Namen.");
      String filePath = fileDescriptor.getPath();
      Assert.hasText(filePath, "Ressourcen-Datei hat keinen Pfad.");
      File file = new File(filePath);
      Assert.isTrue(file.exists(), "Ressourcen-Datei existiert nicht: " + file.getAbsolutePath());
      allResources.put(fileName, file);
    }
    report.setResources(allResources);

    return report;
  }

  /**
   * Erzeugt eine {@link IntrexxDataSource} anhand der ID, die diese in der Applikation <em>Reports
   * für Intrexx</em> hat.
   * 
   * @param dataSourceId
   *          Die ID der Datenquelle.
   * @return Die erzeugte {@link IntrexxDataSource}.
   */
  private IntrexxDataSource readDataSource(String dataSourceId)
  {
    List<IRow> allSources = getRows("qsrep_datasources", "id", dataSourceId);
    if(allSources.size() == 0)
      throw new RuntimeException(
          "Es konnte keine Datenquelle mit der ID '" + dataSourceId + "' gefunden werden.");
    else if(allSources.size() > 1)
      throw new RuntimeException("Es wurden " + allSources.size() + " Datenquellen mit der ID '"
                                 + dataSourceId + "' gefunden. Die ID muss eindeutig sein.");
    IRow selectedDatasource = allSources.get(0);
    IntrexxDataSource intrexxDataSource = new IntrexxDataSource();
    intrexxDataSource.setType(getTypedValue(selectedDatasource, "typ", String.class));
    if("jdbc".equals(intrexxDataSource.getType()))
    {
      String jdbcDataSourceName = getTypedValue(selectedDatasource, "jdbcDataSource", String.class);
      intrexxDataSource.setJdbcDataSourceName(jdbcDataSourceName);
    }
    else if("odata".equals(intrexxDataSource.getType()))
    {
      String odataServiceName = getTypedValue(selectedDatasource, "odataServiceName", String.class);
      String odataEntitySet = getTypedValue(selectedDatasource, "odataEntitySet", String.class);
      intrexxDataSource.setOdataServiceDescription(odataServiceName);
      intrexxDataSource.setOdataEntitySet(odataEntitySet);
    }
    else
      throw new RuntimeException(
          "Unbekannter Datenquellen-Typ: '" + intrexxDataSource.getType() + "'");
    return intrexxDataSource;
  }

  /**
   * Setzt die Haupt-Report-Ressource eines Berichts.
   * 
   * @param selectedReport
   *          Die Datensatz-Zeile, die den Report repr�sentiert.
   * @param report
   *          Der Report, dessen Hauptberichtsressource gesetzt werden soll.
   */
  private void setMainReport(IRow selectedReport, IntrexxApplicationReport report)
  {
    // Wert 'ref_main_report' bestimmen
    String refMainReportValue = getTypedValue(selectedReport, "ref_main_report", String.class);
    Assert.hasText(refMainReportValue, "Value 'ref_main_report' not set!");

    // Den FileValueHolder-Wert bestimmen
    IFileValueHolder fileValueHolder =
        getFileValueHolder("qsrep_resources", "file_resource", refMainReportValue);
    String message = "FileValueHolder aus qsrep_resources konnte nicht gefunden werden f�r Wert '"
                     + refMainReportValue + "'";
    Assert.notNull(fileValueHolder, message);

    // Erste Datei des Valueholders holen
    IVHFileDescriptor firstFile = fileValueHolder.getFirstFile();
    Assert.notNull(firstFile, "FileValueHolder enth�lt keine Datei.");

    // Pfad der Datei holen
    String path = firstFile.getPath();
    Assert.hasText(path, "Pfad ist leer.");

    // Datei aus dem Pfad erzeugen
    File file = new File(path);
    Assert.isTrue(file.exists(), "Ressourcen-Datei existiert nicht: '" + file.getAbsolutePath());

    // Datei als Hauptberichts-Ressource setzen
    report.setMainReport(file);
  }
}
