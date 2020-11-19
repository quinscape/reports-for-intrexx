/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.fill.params;

import org.springframework.core.convert.ConversionService;

import de.quinscape.intrexx.reports.ReportContext;
import de.quinscape.intrexx.reports.domain.IntrexxApplicationReport;

import net.sf.jasperreports.engine.JRParameter;

/**
 * Ein {@link Parametriser} der jeden Parameter, der bereits im Kontext belegt ist, behält und für
 * alle übrigen einen {@link ParameterValueCreator} zum Ermitteln des Werts benutzt.
 * Erlaubt die Nutzung eines {@link ConversionService} um die Werte automatisch zu konvertieren.
 * 
 * @author Jörg Gottschling, Jasper Lauterbach
 */
public class ContextPreservingConvertingParametriser
    implements Parametriser
{

  private ConversionService conversionService;

  private ParameterValueCreator parameterValueCreator;

  @Override
  public final void parametrise(ReportContext context, IntrexxApplicationReport report)
  {
    for(JRParameter parameter : report.getMainJasperReport().getParameters())
    {
      Object value = null;
      String parameterName = parameter.getName();

      // bereits gesetzte Parameter (z.B. in Groovy) haben Vorrang
      if(context.containsParameter(parameterName))
        value = context.getParameterValue(parameterName);

      // Nur wenn kein Parameter explizit gesetzt wurde, wird einer gesucht
      if(value == null)
        value = parameterValueCreator.createParameterValue(context, report, parameter);

      if(value != null)
      {
        Class<?> sourceType = value.getClass();
        Class<?> targetType = parameter.getValueClass();

        if(!targetType.isAssignableFrom(sourceType))
        {
          if(conversionService != null
             && conversionService.canConvert(sourceType, targetType))
          {
            value = conversionService.convert(value, targetType);
          }
          else
          {
            throw new RuntimeException(
                "The value \"" + value + "\" of the type " + sourceType
                                       + " can not be converted to an instance of "
                                       + targetType.getName()
                                       + " for the parameter \"" + parameterName + "\".");
          }
        }

        // TODO: Properties abfragen > bei Datum Zeitzone ändern
        context.setParameterValue(parameterName, value);
      }
    }
  }

  /**
   * {@link ConversionService} der genutzt wird um die Parameterwerte in den
   * korrekte Typ zu überführen.
   */
  public void setConversionService(ConversionService conversionService)
  {
    this.conversionService = conversionService;
  }

  /**
   * Der {@link ParameterValueCreator}, der zum Ermitteln von Werten genutzt wird, wenn im Kontext
   * keiner definiert wurde.
   */
  public void setParameterValueCreator(ParameterValueCreator creator)
  {
    this.parameterValueCreator = creator;
  }
}
