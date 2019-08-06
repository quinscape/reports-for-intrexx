/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain;

import static net.sf.jasperreports.engine.util.JRLoader.loadObject;
import static org.springframework.util.StringUtils.hasText;

import java.io.*;

import net.sf.jasperreports.engine.*;

import org.springframework.beans.factory.*;
import org.springframework.util.*;

/**
 * Ein statischer {@link Report} als Spring-Bean.
 * 
 * @see StaticReportsRepository
 * 
 * @author Jörg Gottschling
 */
public class StaticReport
    extends AbstractReport
    implements Report, InitializingBean
{

  private JasperReport jasperReport;

  private String mainReport;

  private File resourcePath;

  @Override
  public void afterPropertiesSet()
      throws Exception
  {
    Assert.notNull(resourcePath, "No resourcePath configured.");
    Assert.notNull(mainReport, "No mainReport (file name) configured.");
    jasperReport = (JasperReport)loadObject(new File(resourcePath, mainReport));

    Assert.notNull(jasperReport, "No jasperReport loaded!");
  }

  @Override
  public JasperReport getMainJasperReport()
  {
    return jasperReport;
  }

  /**
   * Der Name des Hauptbericht relativ zu {@link #setResourcePath(File)} .
   */
  public void setMainReport(String recource)
  {
    this.mainReport = recource;
  }

  /**
   * Hier liegen die Ressourcen dieses {@link StaticReport}. Dateien werden
   * relativ zu diesem Pfad erzeugt.
   */
  public void setResourcePath(File resourcePath)
  {
    this.resourcePath = resourcePath;
  }

  @Override
  public String toString()
  {
    return getClass().getName() + "[guid=" + getGuid() + ", displayName=\""
           + getDisplayName() + "\", resourcePath=" + resourcePath
           + ", mainReport=" + mainReport + "]";
  }

  @Override
  public String getDefaultConnection()
  {
    return null;
  }

  @Override
  public boolean knowsConnectionName()
  {
    return false;
  }

  @Override
  public boolean hasMainReport()
  {
    return hasText(mainReport);
  }

}
