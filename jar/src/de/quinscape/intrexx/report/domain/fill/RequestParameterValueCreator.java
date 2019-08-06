/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain.fill;

import de.uplanet.lucy.server.engine.http.*;

import de.quinscape.intrexx.report.ReportContext;
import de.quinscape.intrexx.report.domain.*;

import net.sf.jasperreports.engine.*;

/**
 * Ein {@link ParameterValueCreator} der die Werte aus den
 * {@link IViewData} des übergebenen {@link ReportContext} holt. Prüft ob
 * ein Request-Parameter vorhanden ist, der exakt den gleichen Namen hat,
 * wie der Report-Parameter. Prüft zusätzlich ob ein Request-Parameter
 * vorhanden ist der genauso heißt wie der Report-Parameter, nur mit dem
 * zusätzlichen Prefix "rq_".
 * 
 * @author Jörg Gottschling
 */
public class RequestParameterValueCreator
    implements ParameterValueCreator
{

  @Override
  public ReportParameterValueHolder createParameterValue(ReportContext context, Report report,
      JRParameter parameter)
  {
    ReportParameterValueHolder result = new ReportParameterValueHolder();

    if(context.getRequest() != null)
    {
      String value = context.getRequest().get(parameter.getName());
      if(value == null)
        value = context.getRequest().get("rq_" + parameter.getName());
      result.setValue(value);
    }

    return (result.hasValue() ? result : null);
  }
}
