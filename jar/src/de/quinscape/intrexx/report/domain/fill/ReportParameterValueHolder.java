/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain.fill;

import javax.xml.namespace.QName;

import de.uplanet.lucy.server.dataobjects.*;

/**
 * Ist ein {@link IValueHolder}, der ein Objekt für Reports kapselt.
 * {@code hasValue()} ist hier <b>nur</b> wahr, wenn wirklich ein Wert mit
 * {@code setValue()} gesetzt wird (das heißt insbesondere es findet kein
 * Null-Vergleich zur Feststellung statt).
 * 
 * @author Markus Vollendorf
 */
public class ReportParameterValueHolder
    implements IValueHolder<Object>
{

  Object o;

  boolean hasValue;

  /**
   * Erstellt einen leeren ReportParameterValueHolder (das heißt
   * insbesondere {@code hasValue()} ist nicht wahr).
   */
  public ReportParameterValueHolder()
  {
    this.o = null;
    this.hasValue = false;
  }

  @Override
  public String getCanonicalLexicalRepresentation()
  {
    return o.toString();
  }

  @Override
  public Object getRawValue()
  {
    return o;
  }

  @Override
  public QName getType()
  {
    return null;
  }

  @Override
  public Object getValue()
  {
    return o;
  }

  @Override
  public boolean hasValue()
  {
    return hasValue;
  }

  /**
   * Setzt den Wert des ValueHolders auf das angegebene Objekt und somit
   * {@code hasValue()} auf {@code true}.
   */
  public void setValue(Object o)
  {
    this.o = o;
    this.hasValue = true;
  }

}
