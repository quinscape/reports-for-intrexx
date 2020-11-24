/*
 * (C) Copyright 2020 QuinScape GmbH
 * All Rights Reserved.
 *
 * http://www.quinscape.de
 *
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.dao;

/**
 * Eine Datenquelle, die in der Applikation Reports für Intrexx definiert wurde.
 *
 * @author Jasper Lauterbach
 */
public class IntrexxDataSource
{

  private String jdbcDataSourceName;

  private String odataEntitySet;

  private String odataServiceDescription;

  private String type;

  /**
   * Liefert den JDBC-Datenquellennamen.
   * 
   * @return Den Namen, oder <code>null</code>, falls nicht gesetzt (das ist der Fall, wenn eine
   *         OData-Datenqelle definiert ist)
   */
  public String getJdbcDataSourceName()
  {
    return this.jdbcDataSourceName;
  }

  /**
   * Liefert den Namen des OData-EntitySets, das gelesen werden soll.
   * 
   * @return Den Namen, oder <code>null</code>, falls nicht gesetzt (das ist der Fall, wenn eine
   *         JDBC-Datenqelle definiert ist)
   */
  public String getOdataEntitySet()
  {
    return this.odataEntitySet;
  }

  /**
   * Liefert die OData-Servicebeschreibung.
   * 
   * @return Die Beschreibung in der Form <code>"Konfigurations-GUID/Service-GUID"</code>, oder
   *         <code>null</code>, falls nicht gesetzt (das ist der Fall, wenn eine
   *         JDBC-Datenqelle definiert ist).
   */
  public String getOdataServiceDescription()
  {
    return this.odataServiceDescription;
  }

  /**
   * Liefert den Typ (<code>&quot;jdbc&quot;</code> oder <code>&quot;odata&quot;</code>).
   * 
   * @return Der Typ.
   */
  public String getType()
  {
    return this.type;
  }

  /**
   * Setzt den Namen der JDBC-Datenquelle (nur sinnvoll in Verbindung mit
   * <code>setType("jdbc")</code>).
   * 
   * @param jdbcDataSourceName
   *          Der Name der Datenquelle, zum Beispiel <code>"IxSysDb"</code> für die Intrexx-eigene
   *          Datenbank.
   */
  public void setJdbcDataSourceName(String jdbcDataSourceName)
  {
    if(jdbcDataSourceName == null || jdbcDataSourceName.trim().length() == 0)
      throw new IllegalArgumentException(
          "Ungültiger jdbcDataSourceName: " + jdbcDataSourceName + "; darf nicht leer sein.");
    this.jdbcDataSourceName = jdbcDataSourceName;
  }

  /**
   * Legt den Namen des EntitySets fest, das aus der OData-Verbindung gelesen werden soll (nur
   * sinnvoll in Verbindung mit <code>setType("odata")</code>).
   * 
   * @param odataEntitySet
   *          Der Name des EntitySets.
   */
  public void setOdataEntitySet(String odataEntitySet)
  {
    if(odataEntitySet == null || odataEntitySet.trim().length() == 0)
      throw new IllegalArgumentException(
          "Ungültiger EntitySet-Name: " + odataEntitySet + "; darf nicht leer sein.");
    this.odataEntitySet = odataEntitySet;
  }

  /**
   * Setzt die OData-Dienstbeschreibung, im Format <code>"Konfigurations-GUID/Service-GUID"</code>
   * (nur sinnvoll in Verbindung mit <code>setType("odata")</code>).
   * 
   * @param odataServiceDescription
   *          Die Beschreibung.
   */
  public void setOdataServiceDescription(String odataServiceDescription)
  {
    if(odataServiceDescription == null || odataServiceDescription.trim().length() < 3
       || odataServiceDescription.indexOf('/') < 0)
      throw new IllegalArgumentException(
          "Ungültiger OData-Servicedeskriptor: " + odataServiceDescription
                                         + "; er muss das Format \"Konfigurations-GUID/Service-GUID\" haben.");
    this.odataServiceDescription = odataServiceDescription;
  }

  /**
   * Setzt den Typ (<code>&quot;jdbc&quot;</code> oder <code>&quot;odata&quot;</code>).
   * 
   * @param type
   *          Der Typ.
   */
  public void setType(String type)
  {
    if(type == null || !("odata".equals(type) || "jdbc".equals(type)))
      throw new IllegalArgumentException(
          "Ungültiger Typ: " + type + "; muss \"odata\" oder \"jdbc\" sein.");
    this.type = type;
  }

}
