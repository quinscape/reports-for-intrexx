/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.dao;

import static de.quinscape.intrexx.report.ixbl.IxblHelper.*;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.split;
import static org.springframework.util.StringUtils.hasText;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import org.springframework.util.*;

import de.uplanet.lucy.server.businesslogic.util.*;
import de.uplanet.lucy.server.util.*;

import de.quinscape.intrexx.report.*;
import de.quinscape.intrexx.report.domain.*;

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
  {
    ;
  }

  @Override
  public Report findReport(String reportIdentifier)
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
    File f =
        new File(
            getFileValueHolder("qsrep_resources", "file_resource",
                getTypedValue(selectedReport, "ref_main_report", String.class)).getFirstFile().getPath());
    report.setMainReport(f);
    report.setDefaultConnection(getTypedValue(selectedReport, "default_connection", String.class));
    String strPermissions = getTypedValue(selectedReport, "permissions", String.class);
    if(hasText(strPermissions))
    {
      report.setPermittedOrgGuids(new HashSet<String>(asList(split(
          strPermissions, '|'))));
    }
    if(!report.hasMainReport())
    {
      throw new IntrexxReportException("The report \"" + report.getDisplayName()
                                       + "\" has no main resource assigned.", "MISSING_MAIN_RESOURCE");
    }

    Map<String, File> allResources = getRows("qsrep_resource_alloc", "fkguid", report.getGuid()).stream().map(
        (IRow r) -> getTypedValue(r, "ref_resource", String.class)
        ).map(
            (String t) -> getFileValueHolder("qsrep_resources", "file_resource", t).getFirstFile()
        ).
        collect(
            Collectors.toMap((IVHFileDescriptor t) -> t.getFileName(),
                (IVHFileDescriptor t) -> new File(t.getPath()))
        );
    report.setResources(allResources);

    return report;
  }
}
