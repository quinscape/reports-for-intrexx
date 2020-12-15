/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.fill.params;

import de.uplanet.lucy.server.connector.IServerBridgeRequest;
import de.uplanet.lucy.server.engine.http.IViewData;

import de.quinscape.intrexx.reports.ReportContext;
import de.quinscape.intrexx.reports.domain.IntrexxApplicationReport;

import net.sf.jasperreports.engine.JRParameter;

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
  public ReportParameterValueHolder createParameterValue(ReportContext context,
      IntrexxApplicationReport report,
      JRParameter parameter)
  {
    ReportParameterValueHolder result = new ReportParameterValueHolder();
    if(context.getRequest() != null)
    {
      IServerBridgeRequest request = context.getRequest();
      String name = parameter.getName();
      String rqName = "rq_" + name;
      if(request.containsKey(name))
        result.setValue(request.get(name));
      else if(request.containsKey(rqName))
        result.setValue(request.get(rqName));
    }
    return (result.hasValue() ? result : null);
  }
}
