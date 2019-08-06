/*
 * ContextConnectionDataSource.java
 * 
 * (C) Copyright 2006-2007 QuinScape GmbH
 * All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.dao;

import java.sql.*;

import javax.sql.*;

import org.apache.commons.lang.*;
import org.springframework.jdbc.datasource.*;

import de.uplanet.lucy.server.*;

/**
 * DataSource mit der Intrexx-ContextConnection. Funktioniert nur im Context von Intrexx. Liefert
 * sonst <code>null</code>.
 * 
 * TODO: Quasi aus der QXFC kopiert. Gibt es eine Möglichkeit diese komplett zu ersetzen?
 * 
 * @author Jörg Gottschling
 */
class IntrexxContextDataSource
    extends AbstractDataSource
    implements SmartDataSource, DataSource
{

  /**
   * Standard-Verbindungsname, welcher genutzt wird wenn nichts angegeben
   * wird und der Systemdatenbank von Intrexx entspricht.
   */
  public static final String DEFAULT_CONNECTION_NAME = "IxSysDb";

  /** Name der Datenbankverbindung in der Poolmanager-Konfiguration. */
  private String connectionName;

  /**
   * Standard-Konstruktor. Setzt default-Werte.
   */
  protected IntrexxContextDataSource()
  {
    setConnectionName(DEFAULT_CONNECTION_NAME);
  }

  @Override
  public final Connection getConnection()
      throws SQLException
  {
    return ContextConnection.get(getConnectionName());
  }

  /**
   * Not implemented!
   * 
   * @throws NotImplementedException
   *           Weil nicht implementiert!
   * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
   */
  @Override
  public final Connection getConnection(String arg0, String arg1)
  {
    throw new NotImplementedException();
  }

  /**
   * Name der Datenbankverbindung in der Poolmanager-Konfiguration.
   * 
   * @see #DEFAULT_CONNECTION_NAME
   */
  public final String getConnectionName()
  {
    return connectionName;
  }

  /**
   * Name der Datenbankverbindung in der Poolmanager-Konfiguration.
   * 
   * @see #DEFAULT_CONNECTION_NAME
   */
  public final void setConnectionName(String connectionName)
  {
    this.connectionName = connectionName;
  }

  @Override
  public boolean shouldClose(Connection connection)
  {
    return false;
  }

}
