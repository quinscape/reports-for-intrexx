/*
 * (C) Copyright 2008 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain;

/**
 * 
 * 
 * @author Jörg Gottschling
 */
public class ReportResult
{

  private final String filename;

  private final Exception error;
  
  private final String userMessage;

  /**
   * Erzeugt ein reguläres {@link ReportResult}.
   */
  public ReportResult(String filename)
  {
    this.filename = filename;
    this.error = null;
    this.userMessage = null;
  }

  /**
   * Erzeugt ein {@link ReportResult} für einen Fehler.
   */
  public ReportResult(Exception error, String userMessage)
  {
    this.filename = null;
    this.error = error;
    this.userMessage = userMessage;
  }
  
  /**
   * Liefert den Dateinamen des PDF-Export.
   */
  public String getPdfFileName()
  {
    return filename + ".pdf";
  }

  /**
   * Liefert den Dateinamen ohne Endung.
   */
  public String getBareFileName()
  {
    return filename;
  }
  
  /**
   * Prüft ob das Ergebnisfehlerhaft war.
   */
  public boolean hasError()
  {
    return error != null;
  }

  /**
   * Liefert die Fehlermeldung oder <code>null</code> wenn kein Fehler
   * aufgetreten ist.
   */
  public Exception getError()
  {
    return error;
  }

  /**
   * Liefert eine Meldung für den Benutzer. Kann eine Fehlermeldung sein.
   */
  public String getUserMessage()
  {
    return userMessage;
  }
  
}
