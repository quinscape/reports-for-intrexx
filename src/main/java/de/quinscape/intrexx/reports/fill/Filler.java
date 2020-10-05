/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.fill;

import java.io.File;
import java.sql.Connection;
import java.util.Collections;
import java.util.Map;

import org.springframework.util.Assert;

import de.uplanet.jdbc.JdbcConnection;

import de.quinscape.intrexx.reports.CONSTANTS;
import de.quinscape.intrexx.reports.IntrexxReportException;
import de.quinscape.intrexx.reports.ReportContext;
import de.quinscape.intrexx.reports.dao.IntrexxDataSource;
import de.quinscape.intrexx.reports.domain.IntrexxApplicationReport;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.SimpleJasperReportsContext;
import net.sf.jasperreports.repo.FileRepositoryService;
import net.sf.jasperreports.repo.RepositoryContext;
import net.sf.jasperreports.repo.RepositoryService;

/**
 * Befüllt den Bericht.
 * 
 * @author Jasper Lauterbach
 */
public class Filler
{

  /**
   * Füllt einen Bericht mit Daten. Dazu werden die verknüpften Ressourcen über einen
   * {@link FileRepositoryService} bereitgestellt, und die Datenquelle für den Bericht wird
   * ermittelt (JDBC oder OData).
   * 
   * @param context
   *          Der {@link ReportContext}.
   * @param report
   *          Der zu füllende Bericht.
   * @return Ein {@link JasperPrint}, das dann exportiert werden kann.
   */
  public JasperPrint fill(ReportContext context, IntrexxApplicationReport report)
  {
    Assert.notNull(context, "Context was null.");
    Assert.notNull(report, "Report was null.");

    try
    {
      SimpleJasperReportsContext jasperContext =
          new SimpleJasperReportsContext(DefaultJasperReportsContext.getInstance());
      Map<String, File> resources = report.getResources();
      /*
       * Ab JasperReports 6.6.0 oder so soll der FileResolver nicht mehr genutzt werden. Er war
       * bisher dafür zuständig, von einem Report angeforderte Ressourcen (etwa Bilder oder
       * Subreport-Dateien) zu liefern.
       * 
       * Ab sofort wird das über einen RepositoryService erledigt, der als Extension im Context
       * von Jaspersoft liegen muss, bevor der Filler seine Arbeit macht.
       * 
       * Man kann den FileRepositoryService von Jaspersoft schon fast nutzen, wenn da nicht die
       * Besonderheit von Intrexx wäre, eine Ressource wie 'Bild.jpg' als 'Bild_2.jpg' abzulegen.
       * Diese Transformation in den echten Dateienamen, den Intrexx beim Hochladen vergeben hat,
       * übernimmt diese anonyme Unterklasse vom FileRepositoryService.
       */
      String portalDirectory = new File(".").getAbsolutePath();
      RepositoryService service =
          new FileRepositoryService(jasperContext, portalDirectory, false)
          {

            private String transformFilename(String resourceName)
            {
              // Ressourcen, die nicht von der Reports-Applikation bereitgestellt, sondern per URL
              // direkt im Report eingebunden werden, haben bereits ihren physikalisch korrekten
              // Namen; diesen einfach zurückgeben.
              if(isUrl(resourceName) || new File(resourceName).exists())
                return resourceName;

              // Von der Reports-Applikation verwaltete Ressourcen müssen um den Pfadnamen (relativ
              // zum Portal) ergänzt werden sowie durch ihren korrekten Dateinamen (mit „_0“ am Ende
              // zum Beispiel).
              File transformedResource = resources.get(resourceName);
              if(transformedResource == null)
                throw new RuntimeException("Die vom Bericht geforderte Ressource '" + resourceName
                                           + "' existiert nicht in Reports für Intrexx oder ist dem Bericht nicht zugeordnet. Zugeordnet sind: "
                                           + resources.keySet());
              String transformedPath = "internal/files/" + CONSTANTS.REPORTS_APPGUID + "/"
                                       + transformedResource.getName();
              File transformedFile = new File(transformedPath);
              if(!transformedFile.exists())
                throw new RuntimeException("Die Datei '" + transformedFile.getAbsolutePath()
                                           + "' existiert nicht. Sie sollte eigentlich die Ressource '"
                                           + resourceName + "' darstellen.");
              return transformedPath;
            }

            @Override
            public File getFile(RepositoryContext repoContext, String name)
            {
              return super.getFile(repoContext, transformFilename(name));
            }

          };
      jasperContext.setExtensions(RepositoryService.class, Collections.singletonList(service));

      JasperFillManager fillManager = JasperFillManager.getInstance(jasperContext);
      JasperReport jasperReport = report.getMainJasperReport();
      Map<String, Object> reportParameters = context.getReportParameters();

      IntrexxDataSource dataSource = report.getDataSource();
      if("jdbc".equals(dataSource.getType()))
      {
        Connection jdbcConnection =
            retrieveConnection(context, dataSource.getJdbcDataSourceName());
        return fillManager.fill(jasperReport, reportParameters, jdbcConnection);
      }
      else if("odata".equals(dataSource.getType()))
      {
        String entitySet = dataSource.getOdataEntitySet();
        String odataService = dataSource.getOdataServiceDescription();
        JRDataSource odataConnection = ODataDataSource.create(odataService, entitySet);
        return fillManager.fill(jasperReport, reportParameters, odataConnection);
      }
      else
        throw new RuntimeException(
            "Unbekannter Datenquellen-Typ: '" + dataSource.getType() + "'.");
    }
    catch(Throwable exc)
    {
      throw new IntrexxReportException(exc);
    }
  }

  /**
   * Gibt an, ob es sich bei einer Ressource um eine URL handelt. Diese wäre dann untransformiert
   * vom Bericht zu verwenden.
   * 
   * @param resourceName
   *          Der zu prüfende Ressourcen-Name
   * @return <code>true</code>, falls <code>resourceName</code> nicht <code>null</code> ist und
   *         startet mit <code>http(s)://</code> oder <code>ftp(s)://</code>.
   */
  protected static boolean isUrl(String resourceName)
  {
    return resourceName != null && (resourceName.toLowerCase().startsWith("http://") ||
                                    resourceName.toLowerCase().startsWith("https://") ||
                                    resourceName.toLowerCase().startsWith("ftp://") ||
                                    resourceName.toLowerCase().startsWith("ftps://"));
  }

  private Connection retrieveConnection(ReportContext context, String connectionName)
  {
    Connection connection;
    connection = context.getDbConnection(connectionName);
    if(connection instanceof JdbcConnection)
      connection = ((JdbcConnection)connection).getNativeConnection();
    Assert.notNull(connection, "connection could not be retrieved.");
    return connection;
  }

}
