/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain;

import static java.util.Collections.emptyList;

import java.util.*;

import org.springframework.beans.factory.*;
import org.springframework.util.*;

/**
 * Ein {@link ReportsRepository} als Spring-Bean mit statischem, aber
 * konfigurierbaren Inhalt. Die Inhalten werden In-Memory gehalten, dadurch
 * sehr schnell. Gut für Test-Zwecke.
 * 
 * @see StaticReport
 * 
 * @author Jörg Gottschling
 */
public class StaticReportsRepository
    implements ReportsRepository, InitializingBean
{

  private Map<String, Report> reportsByIdentifier =
      new TreeMap<String, Report>();

  private List<Report> allReports = emptyList();

  @Override
  public Report findReport(String reportIdentifier)
  {
    return reportsByIdentifier.get(reportIdentifier);
  }

  @Override
  public void afterPropertiesSet()
      throws Exception
  {
    for(Report report : allReports)
    {
      if(StringUtils.hasText(report.getGuid()))
        reportsByIdentifier.put(report.getGuid(), report);
      if(StringUtils.hasText(report.getDisplayName()))
        reportsByIdentifier.put(report.getDisplayName(), report);
    }
  }

  /**
   * Liefert alle diesem {@link StaticReportsRepository} bekannten Berichte.
   */
  public List<Report> getAllReports()
  {
    return allReports;
  }

  /**
   * Setzt die diesem {@link StaticReportsRepository} statisch bekannten
   * Berichte.
   * <p>
   * Da als Spring-Bean gedacht, muss danach {@link #afterPropertiesSet()}
   * aufgerufen werden. Dies geschieht automatisch durch Spring.
   */
  public void setReports(List<Report> reports)
  {
    this.allReports = reports;
  }

}
