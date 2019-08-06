/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain.fill;

import static java.util.Collections.EMPTY_LIST;

import java.util.*;

import net.sf.jasperreports.engine.*;

import de.quinscape.intrexx.report.ReportContext;
import de.quinscape.intrexx.report.domain.*;

/**
 * Ein {@link ParameterValueCreator} der eine {@link List}e von
 * anderen {@link ParameterValueCreator}s sequentiell abläuft und den
 * ersten Wert der nicht <code>null</code> ist zurück liefert. Quasi
 * COALESCE aus SQL. ;-)
 * 
 * @author Jörg Gottschling
 */
public class SequentialParameterValueCreator
    implements ParameterValueCreator
{

  private List<ParameterValueCreator> sequence;

  /**
   * Erzeugt eine Instanz mit einer {@link List} mit
   * {@link ParameterValueCreator} die in der übergebenen Reihenfolge
   * abgelaufen werden.
   */
  public SequentialParameterValueCreator(
      List<ParameterValueCreator> sequence)
  {
    this.sequence = sequence;
  }

  /**
   * Erzeugt eine leere Instanz.
   */
  @SuppressWarnings("unchecked")
  public SequentialParameterValueCreator()
  {
    this(EMPTY_LIST);
  }

  @Override
  public Object createParameterValue(ReportContext context, Report report,
      JRParameter parameter)
  {
    ReportParameterValueHolder value = null;
    Iterator<ParameterValueCreator> creators = sequence.iterator();

    while(value == null && creators.hasNext())
      value = (ReportParameterValueHolder) creators.next().createParameterValue(context, report, parameter);

    return (value == null ? null : value.getValue());
  }

  /**
   * {@link List} mit {@link ParameterValueCreator} die in der
   * übergebenen Reihenfolge abgelaufen werden.
   */
  public void setSequence(List<ParameterValueCreator> sequence)
  {
    this.sequence = sequence;
  }

}
