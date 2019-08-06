/*
 * (C) Copyright 2008 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report;

/**
 * <p>
 * IntrexxReportException.
 * </p>
 * 
 * @author Jörg Gottschling
 */
@SuppressWarnings("serial")
public class IntrexxReportException
    extends RuntimeException
{

  // Wird verwendet für die Behandlung in Error-Handlern.
  private final String errorCode;
  
  
  /**
   * Liefert {@link #errorCode}. 
   *
   * @return Liefert <code>IntrexxReportException.errorCode</code>
   *         als <code>String</code>.
   */
  public String getErrorCode()
  {
    return errorCode;
  }

  /**
   * Erzeugt eine Instanz von <code>IntrexxReportException</code>.
   * 
   * @param message
   */
  public IntrexxReportException(String message)
  {
    this(message, (String)null);
  }

  /**
   * Erzeugt eine Instanz von <code>IntrexxReportException</code>.
   * 
   * @param message
   * @param errorCode
   */
  public IntrexxReportException(String message, String errorCode)
  {
    super(message);
    this.errorCode = errorCode;
  }

  /**
   * Erzeugt eine Instanz von <code>IntrexxReportException</code>.
   * 
   * @param message
   * @param cause
   */
  public IntrexxReportException(String message, Throwable cause)
  {
    super(message, cause);
    this.errorCode = null;
  }

  /**
   * Erzeugt eine Instanz von <code>IntrexxReportException</code>.
   * 
   * @param cause
   */
  public IntrexxReportException(Throwable cause)
  {
    this(cause.getMessage(), cause);
  }
}
