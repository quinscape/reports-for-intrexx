/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.fill.params;

import static java.util.Collections.EMPTY_LIST;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.jfree.util.Log;

import de.quinscape.intrexx.reports.ReportContext;
import de.quinscape.intrexx.reports.domain.IntrexxApplicationReport;

import net.sf.jasperreports.engine.JRParameter;

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

  private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(SequentialParameterValueCreator.class);

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
  public Object createParameterValue(ReportContext context, IntrexxApplicationReport report,
      JRParameter parameter)
  {
    ReportParameterValueHolder value = null;
    Iterator<ParameterValueCreator> creators = sequence.iterator();

    while(value == null && creators.hasNext())
    {
      ParameterValueCreator creator = creators.next();
      LOG.debug("Trying to create a value for '" + parameter.getName() + "' with " + creator.getClass().getName());
      value = (ReportParameterValueHolder)creator.createParameterValue(context, report,
          parameter);
    }
    LOG.debug("Value for '" + parameter.getName() + "' finally set to " +
      String.valueOf(value == null ? null : value.getValue()));
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
