/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain.export;

// import static net.sf.jasperreports.engine.export.JRXmlExporterParameter.DTD_LOCATION;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.export.*;

/**
 * Exportiert einen Bericht in XML.
 * 
 * TODO Seit Jasper Reports 5.6 gibt es DTD_LOCATION nicht mehr und vieles ist als deprecated
 * markiert.
 * 
 * @author Jörg Gottschling
 */
public class XmlExporter
    extends
    JRXmlExporter
{

  private String dtdLocation;

  private boolean embeddingImages = true;

  /**
   * Siehe {@link #setDtdLocation(String)}.
   */
  public String getDtdLocation()
  {
    return dtdLocation;
  }

  /**
   * Siehe {@link #setEmbeddingImages(boolean)}.
   */
  public boolean isEmbeddingImages()
  {
    return embeddingImages;
  }

  /**
   * TODO: DTD-Location hat keine Funktion mehr.
   * 
   * @deprecated seit 6.0.1
   */
  @Deprecated
  public void setDtdLocation(String dtdLocation)
  {
    this.dtdLocation = dtdLocation;
  }

  /**
   * Siehe {@link SimpleXmlExporterOutput#setEmbeddingImages(Boolean)}.
   */
  public void setEmbeddingImages(boolean flag)
  {
    this.embeddingImages = flag;
  }

}
