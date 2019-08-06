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

import static de.quinscape.intrexx.report.ixbl.IxblHelper.getRows;
import static de.quinscape.intrexx.report.ixbl.IxblHelper.getTypedValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import de.quinscape.intrexx.report.transport.jaxb.v1.Export;
import de.uplanet.io.IOHelper;
import de.uplanet.io.ZipHelper;
import de.uplanet.lucy.server.businesslogic.BlException;
import de.uplanet.lucy.server.businesslogic.util.IRow;
import de.uplanet.lucy.server.portalserver.PortalPath;

/**
 * Dient als Wrapper / Helper für den Im- und Exporter und agiert der Lesbarkeit halber fast
 * ausschließlich mit Sysidents. Implementationsvererbung.
 * <p>
 * TODO: Architekturänderung hin zu Service-Klasse <br>
 * Die Architektur scheint ungünstig gewachsen. Diese Klasse müsste ein fachlicher Service sein. Die
 * Persistenz-Logik müsste in ein PackageDAO wandern inkl. XML-Marshalling. Aus den
 * PageActionHandler heraus müssten nur noch die richtigen Services Kontext-Bezogen aufgerufen
 * werden. Die Services für über den Spring-Context gemanaged.<br>
 * Dann würden aber auch die Domain-Klassen unter <code>de.quinscape.intrexx.report.domain</code>
 * gehören. JG, 21.07.2014
 * 
 * @author Markus Vollendorf
 */
public abstract class Transport
{

  /**
   * Logger for this class
   */
  private static final Log log = LogFactory.getLog(Transport.class);

  private static JAXBContext jaxbContext;

  JAXBContext getJaxbContext()
      throws JAXBException
  {
    if(jaxbContext == null)
      jaxbContext = JAXBContext.newInstance("de.quinscape.intrexx.report.transport.jaxb.v1");
    return jaxbContext;
  }

  Marshaller getMarshaller()
      throws JAXBException
  {
    Marshaller m = getJaxbContext().createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    return m;
  }

  Unmarshaller getUnmarshaller()
      throws JAXBException
  {
    Unmarshaller u = getJaxbContext().createUnmarshaller();
    return u;
  }

  String[] getSplitValues(String s)
  {
    return s.split("[|\\s]+");
  }

  List<String> getIncludedReports(String packageId)
  {
    List<String> reportList = new ArrayList<String>();
    List<IRow> rowList = getRows("qsrep_packages_reports", "fklid", packageId);
    for(IRow r : rowList)
      reportList.add(getTypedValue(r, "guid", String.class));
    return reportList;
  }

  /**
   * Lädt die Meta-Daten mittels JAXB aus dem XML und stellt sie als Java-Objekt zur Verfügung.
   * 
   * @param unzippedPackageFolder
   *          Das temporäre Verzeichnis in welches das Paket entpackt wurde als {@link File}.
   * 
   * @return Die Meta-DAten als {@link Export}.
   */
  Export unmarshalPackage(File unzippedPackageFolder)
  {
    Export export = null;
    try
    {
      export = (Export)getUnmarshaller().unmarshal(new File(unzippedPackageFolder, "reports.xml"));
    }
    catch(JAXBException exc)
    {
      // TODO Fehlerbehandlung - catch(JAXBException)
      log.error(exc);
    }
    return export;
  }

  /**
   * Validiert die im Packet enthaltenen XML-Dateien an Hand des Schemas und loggt dann eine
   * Warnung, aber wirft <i>keine</i> Ausnahme.
   * 
   * @param unzippedPackageFolder
   *          Das temporäre Verzeichnis in welches das Paket entpackt wurde als {@link File}.
   */
  void validateXmlFiles(File unzippedPackageFolder)
  {
    try
    {
      String schemaLang = "http://www.w3.org/2001/XMLSchema";
      SchemaFactory factory = SchemaFactory.newInstance(schemaLang);
      Schema schema = factory.newSchema(new StreamSource("export.xsd"));
      Validator validator = schema.newValidator();
      validator.validate(new StreamSource(new File(unzippedPackageFolder, "reports.xml")));
    }
    catch(SAXException e)
    {
      System.out.println(" sax exception :" + e.getMessage());
    }
    catch(Exception ex)
    {
      System.out.println("excep :" + ex.getMessage());
    }
  }

  /**
   * Entpackt den Import in ein temporäres Verzeichnis.
   * 
   * @param filePath
   *          Direkter Pfad zum ZIP-Archiv des Pakets.
   * 
   * @return Das temporäre Verzeichnis in welches das Paket entpackt wurde als {@link File}.
   * 
   */
  File unzipFileToTempFolder(String filePath)
      throws BlException
  {
    Path unzipTmp = null;
    try
    {
      File internalTmp = PortalPath.get(PortalPath.TMP_DIR).toFile();
      unzipTmp = IOHelper.createTempDirectory(internalTmp, "reportsPackageMigration", null).toPath();
      ZipHelper.unzipDir(new File(filePath).toPath(), unzipTmp);
    }
    catch(IOException exc)
    {
      throw new BlException("Error extracting given package.", exc);
    }
    return unzipTmp.toFile();
  }

}
