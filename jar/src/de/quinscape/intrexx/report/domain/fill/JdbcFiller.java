/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain.fill;

import java.io.*;
import java.sql.*;
import java.util.*;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.repo.*;

import org.springframework.util.*;

import de.uplanet.jdbc.*;

import de.quinscape.intrexx.report.*;
import de.quinscape.intrexx.report.ReportContext;
import de.quinscape.intrexx.report.domain.*;

/**
 * Ein {@link Filler} welcher den Report an Hand einer oder mehrerer JDBC-Datenbankverbindungen
 * befüllt.
 * 
 * @author Jörg Gottschling, Jasper Lauterbach
 */
public class JdbcFiller
    implements Filler
{

  @Override
  public JasperPrint fill(ReportContext context, Report report)
  {
    Assert.notNull(context, "Context was null.");
    Assert.notNull(report, "Report was null.");

    try
    {
      SimpleJasperReportsContext jasperContext =
          new SimpleJasperReportsContext(DefaultJasperReportsContext.getInstance());
      Map<String, File> resources = report instanceof IntrexxApplicationReport ?
          ((IntrexxApplicationReport)report).getResources() : new HashMap<String, File>();
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
              return "internal/files/000EEE6F0AB4F5CD1BE98AA2B2958BB3C5764501/" +
                     resources.get(resourceName).getName();
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

      return fillManager.fill(jasperReport, reportParameters, retrieveConnection(context, report));
    }
    catch(Throwable exc)
    {
      throw new IntrexxReportException(exc);
    }
  }

  private Connection retrieveConnection(ReportContext context, Report report)
  {
    Connection connection;
    if(report.knowsConnectionName())
      connection = context.getDbConnection(report.getDefaultConnection());
    else
      connection = context.getDbConnection();
    if(connection instanceof JdbcConnection)
      connection = ((JdbcConnection)connection).getNativeConnection();
    Assert.notNull(connection, "connection could not be retrieved.");
    return connection;
  }

}
