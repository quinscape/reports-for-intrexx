/*
 * (C) Copyright 2011 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report;

import java.text.*;
import java.util.*;

import org.springframework.core.convert.converter.*;
import org.apache.commons.lang.time.DateUtils;

/**
 * Konvertiert ein Datum von einem String in ein Datum, hierbei insbesondere
 * deutsche Daten aus einem dynamischen Datumsfilter, da diese Datumswerte
 * in dem View leider nicht vorhanden sind. :-(
 * 
 * @author Markus Vollendorf
 */
public class StringToDateConverter
    implements Converter<String, Date>
{

  private static final String[] dateFormats =
      {"yy-MM-dd", "yyyy-MM-dd", "dd.MM.yy", "dd.MM.yyyy"};

  @Override
  public Date convert(String source)
  {
    Date d = null;
    if(source != null && !"".equals(source))
    {
      try
      {
        d = DateUtils.parseDate(source, dateFormats);
      }
      catch(ParseException e)
      {
        throw new IllegalArgumentException(e);
      }
    }
    return d;
  }

}
