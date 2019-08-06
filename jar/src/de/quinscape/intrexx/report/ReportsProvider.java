/*
 * (C) Copyright 2003-2011 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report;

import static org.springframework.util.StringUtils.startsWithIgnoreCase;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import de.quinscape.intrexx.report.domain.Report;
import de.quinscape.intrexx.report.domain.ReportCreationResult;
import de.quinscape.intrexx.report.domain.ReportsRepository;
import de.quinscape.intrexx.report.domain.VelocityReportCreator;
import de.uplanet.lucy.server.annotation.VelocityCallable;
import de.uplanet.lucy.server.businesslogic.IBusinessLogicProcessingContext;
import de.uplanet.lucy.server.poolmanager.PoolManager;
import de.uplanet.lucy.server.spring.configuration.IntrexxApplicationContext;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;

/**
 * Das Velocity-Callable <code>$ReportsProvider</code> welches zur
 * Integration der Java-Logik mit Velocity dient. Spielt gleichzeitig die
 * Rolle des Service-/Bean-Provider.
 * 
 * @author Jörg Gottschling (joerg.gottschling@quinscape.de)
 * @author Philip Moston (phil.moston@quinscape.de)
 */
@VelocityCallable("singleton")
public final class ReportsProvider
{

  /** Logger for this class. */
  private static final Log log = LogFactory.getLog(ReportsProvider.class);

  /**
   * Liefert einen {@link String} mit einer rein informativen Versionsnummer
   * von "Reports für Intrexx".
   */
  public static final String getVersion()
  {
    return "@reports-version@";
  }

  /**
   * Öffentlicher Konstruktor für Callable.
   */
  public ReportsProvider()
  {
    log.info("Callable ReportsProvider loaded. Version: " + getVersion());
  }

  /**
   * Holt einen Bericht an Hand seiner GUID oder seines Namen.
   */
  public Report findReport(
      @SuppressWarnings("unused") IBusinessLogicProcessingContext context,
      String reportIdentifier)
  {
    ReportsRepository repository =
        getApplicationContext().getBean("quinscape.reports.repository",
            ReportsRepository.class);
    return repository.findReport(reportIdentifier);
  }

  /**
   * Liefert die Parameter zu dem aktuellen Bericht.
   */
  public Collection<JRParameter> getParameters(
      IBusinessLogicProcessingContext context, String reportIdentifier)
  {
    Collection<JRParameter> inputParameters = new ArrayList<JRParameter>();
    Report report = findReport(context, reportIdentifier);
    if(report != null)
    {
      JasperReport mainReport = report.getMainJasperReport();
      if(mainReport != null)
      {
        for(JRParameter parameter : mainReport.getParameters())
        {
          // TODO: Diese Liste von Präfixen konfigurierbar machen
          // TODO: ParameterValueCreator.filterForInput(JRParameter) ?
          if(!parameter.isSystemDefined()
             && !startsWithIgnoreCase(parameter.getName(), "CONNECTION_")
             && !startsWithIgnoreCase(parameter.getName(), "REPORT_")
             && !startsWithIgnoreCase(parameter.getName(), "INTREXX_")
             && !startsWithIgnoreCase(parameter.getName(), "JASPER_"))
            inputParameters.add(parameter);
        }
      }
    }

    return inputParameters;
  }

  /**
   * Erzeugt einen Bericht an Hand dessen Namen oder GUID. Die Erzeugung
   * geschieht durch die ServiceBean
   * <code>quinscape.reports.velocityReportCreator</code> vom Typ
   * {@link VelocityReportCreator}.
   */
  @Deprecated
  public ReportCreationResult createReport(
      IBusinessLogicProcessingContext context)
  {
    VelocityReportCreator service =
        getApplicationContext().getBean(
            "quinscape.reports.velocityReportCreator",
            VelocityReportCreator.class);
    return service.createReport(new DefaultReportContext(context));
  }

  /**
   * Alle Namen der in Intrexx konfigurierten Datenbankverbindungen.
   */
  public Collection<String> getConnectionNames()
  {
    return PoolManager.getInstance().getDataSourceNames();
  }

  ApplicationContext getApplicationContext()
  {
    Assert.notNull(IntrexxApplicationContext.getInstance(),
        "Can not get the default Spring application context of Intrexx!");
    return IntrexxApplicationContext.getInstance();
  }

}
