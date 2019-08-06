/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain.fill;

import java.util.Optional;

import de.quinscape.intrexx.report.ReportContext;
import de.quinscape.intrexx.report.domain.Report;
import de.uplanet.lucy.server.engine.http.IViewData;
import de.uplanet.lucy.server.transform.IControlData;
import net.sf.jasperreports.engine.JRParameter;

/**
 * Ein {@link ParameterValueCreator} der die Werte aus den {@link IViewData} des übergebenen
 * {@link ReportContext} holt.
 * 
 * @author Jörg Gottschling
 */
public class ViewDataParameterValueCreator
    implements ParameterValueCreator
{

  @Override
  public ReportParameterValueHolder createParameterValue(ReportContext context,
      Report report, JRParameter parameter)
  {
    ReportParameterValueHolder result = new ReportParameterValueHolder();
    IViewData viewData = context.getSourceViewData();
    if(viewData != null)
    {
      Optional<IControlData> optional = viewData.getValueForControlName(parameter.getName());
      if(optional.isPresent())
        result.setValue(optional.get().getValue().asValueHolder());
    }
    return (result.hasValue() ? result : null);
  }
}
