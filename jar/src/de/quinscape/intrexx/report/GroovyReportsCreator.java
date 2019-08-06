/*
 * (C) Copyright 2011 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report;

import static org.springframework.util.StringUtils.arrayToDelimitedString;
import static org.springframework.util.StringUtils.hasText;

import java.util.*;
import java.util.Map.*;

import de.uplanet.lucy.server.businesslogic.*;

import de.quinscape.intrexx.report.domain.*;
import de.quinscape.intrexx.report.ixbl.*;

/**
 * EXPERIMENTAL - UNDER DEVELOPMENT!
 * <p>
 * Erzeugt sehr flexibel und dynamisch Reports aus Groovy.
 * 
 * TODO Ist diese Klasse noch notwendig /sinnvoll dank der Methode
 * {@link ReportByDatarecordCreator#createReport(Map)}?
 * 
 * @author Jörg Gottschling
 */
public class GroovyReportsCreator
{

  /** Diese Werte in der Definition können den Report-Identifizieren. */
  static final String[] identifierKeys = {"name", "guid", "identifier"};

  private ReportService reportService;

  /**
   * <p>
   * Doku fehlt.
   * </p>
   * 
   * DOKU: GroovyReportsCreator.createReport()
   */
  @SuppressWarnings({"unchecked"})
  public void createReport(IBusinessLogicProcessingContext blCtx, Map<String, Object> definition)
      throws Exception
  {
    ReportContext context = new DefaultReportContext(blCtx);

    Object parameters = definition.get("parameters");
    if(parameters != null && parameters instanceof Map)
    {
      for(Entry<String, Object> parameter : ((Map<String, Object>)parameters).entrySet())
      {
        context.setParameterValue(parameter.getKey(), parameter.getValue());
      }
    }

    Map<String, ExportTarget> exportTargets = resolveExportTargets(definition);

    reportService.createReport(context, resolveIdentifier(definition), exportTargets);
  }

  /**
   * Erzeugt aus der flexiblen Definition eine Liste mit Exportzielen.
   */
  @SuppressWarnings("unchecked")
  Map<String, ExportTarget> resolveExportTargets(Map<String, Object> definition)
  {
    Map<String, ExportTarget> exportTargets = new HashMap<String, ExportTarget>();

    List<Object> export = null;
    Object value = definition.get("export");
    if(value != null)
    {
      if(value instanceof Map) value = Collections.singletonList(value);
      if(value instanceof List) export = (List<Object>)value;
    }

    if(export == null || export.isEmpty())
      throw new RuntimeException("export is empty");

    for(Object entry : export)
    {
      if(!(entry instanceof Map))
        throw new RuntimeException("Illegal export entry.");

      Map<String, Object> target = (Map<String, Object>)entry;

      // FIXME: "The value of the local variable formatId is not used" -> Fehler!?
      value = target.get("format");
      if(value != null && !(value instanceof CharSequence))
        throw new RuntimeException("'format' must be a String.");

      value = target.get("datafield");
      if(value != null && !(value instanceof CharSequence))
        throw new RuntimeException("'datafield' must be a String.");
    }

    return exportTargets;
  }

  /**
   * Identifiziert den Identifier des Reports in der flexiblen Definition.
   */
  String resolveIdentifier(Map<String, Object> definition)
  {
    for(String key : identifierKeys)
    {
      Object value = definition.get(key);
      if(value != null)
      {
        if(!(value instanceof CharSequence))
          throw new RuntimeException("'" + key + "' must be a String.");
        String identifier = value.toString();
        if(hasText(identifier)) return identifier;
      }
    }

    throw new IllegalArgumentException("None of these found: '"
                                       + arrayToDelimitedString(identifierKeys,
                                           "', '")
                                       + "'.");
  }

}
