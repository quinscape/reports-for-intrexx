/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain.fill;

import java.util.*;

import net.sf.jasperreports.engine.*;

import org.springframework.core.convert.*;

import de.quinscape.intrexx.report.ReportContext;
import de.quinscape.intrexx.report.domain.*;

/**
 * Ein {@link Parametriser} der eine {@link List}e von {@link ParameterValueCreator}s sequentiell
 * abläuft und den Wert des ersten zurückgegeben {@link ReportParameterValueHolder} als
 * Parameterwert nutzt. Versucht für jeden Parameter des Hauptbericht einen Wert zu erzeugen.
 * Erlaubt die Nutzung eines {@link ConversionService} um die Werte automatisch zu konvertieren.
 * 
 * @author Jörg Gottschling
 */
public class SequentialParametriser
    implements Parametriser
{

  private ConversionService conversionService;

  private ParameterValueCreator creators;

  @Override
  public final void parametrise(ReportContext context, Report report)
  {
    for(JRParameter parameter : report.getMainJasperReport().getParameters())
    {
      Object value = null;
      String parameterName = parameter.getName();

      // bereits gesetzte Parameter (z.B. in Groovy) haben Vorrang
      if(context.containsParameter(parameterName))
      {
        value = context.getParameterValue(parameterName);
      }

      // Nur wenn kein Parameter explizit gesetzt wurde, wird einer gesucht
      if(value == null)
      {
        value = creators.createParameterValue(context, report, parameter);
      }

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
                    + " can not be converted to an instance of " + targetType.getName()
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
   * {@link List} mit {@link ParameterValueCreator} die in der übergebenen
   * Reihenfolge abgelaufen werden.
   */
  public void setParameterValueCreators(List<ParameterValueCreator> creators)
  {
    this.creators = new SequentialParameterValueCreator(creators);
  }
}
