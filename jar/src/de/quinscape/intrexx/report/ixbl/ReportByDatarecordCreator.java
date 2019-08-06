/*
 * (C) Copyright 2008 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.ixbl;

import static org.apache.commons.collections.MapUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.*;

import java.util.*;

import org.apache.commons.beanutils.*;
import org.apache.commons.beanutils.converters.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;
import org.springframework.util.*;

import de.uplanet.lucy.constants.*;
import de.uplanet.lucy.server.businesslogic.*;
import de.uplanet.lucy.server.businesslogic.rtappservices.*;
import de.uplanet.lucy.server.businesslogic.rtdata.*;
import de.uplanet.lucy.server.businesslogic.scripting.groovy.*;
import de.uplanet.lucy.server.businesslogic.util.*;
import de.uplanet.lucy.server.dataobjects.*;
import de.uplanet.lucy.server.dataobjects.impl.*;
import de.uplanet.lucy.server.rtcache.*;
import de.uplanet.lucy.server.spring.configuration.*;
import de.uplanet.lucy.server.workflow.timer.*;
import de.uplanet.util.filter.*;

import de.quinscape.intrexx.report.*;
import de.quinscape.intrexx.report.domain.*;

/**
 * Generiert aus einem {@link IDataRecord} oder {@link ReadOnlyGroovyRecord} einen Bericht. Die
 * Datengruppe
 * in der sich der Datensatz befindet muss als Sysident den Namen oder die GUID des Berichts haben.
 * <p>
 * Der Wert jedes Datenfeld des Datensatzes das einen Sysident besitzt wird als Parameter übergeben.
 * Dabei wir der Sysident als Parametername verwendet. Der Parametertyp entspricht dem Datentyp des
 * Feldes. Sind spezielle Parameter wie beispielsweise vom Typ {@link Locale} gefordert, so können
 * diese über die Reportverwaltung eingestellt werden. Die Werte der Datenfelder werden dann
 * entsprechend transformiert.
 * <p>
 * Von den Parametern ausgenommen sind Dateidatenfelder. Diese werden als Exportziel für den Bericht
 * genutzt. Für jedes gewünschte Format muss ein Dateidatenfeld in der Datengruppe vorhanden sein.
 * Der Sysident des Datenfeldes muss die Form <code>report&lt;Format&gt;</code> haben. Dabei ist die
 * "camelCase" schreibweise wichtig. Möglich sind beispielsweise <code>reportPdf</code> oder
 * <code>reportXls</code>. Werden keine Dateidatenfelder angegeben, so kann wird der Bericht nicht
 * exportiert und ein Warnmeldung in den Log geschrieben.
 * <p>
 * Wurde wegen der Testbarkeit und Modularität aus dem Datahandler ausgelagert.
 * <p>
 * TODO: Elterndatensätze berücksichtigen? Könnte einiges Vereinfachen.
 * <p>
 * TODO: Was ist mit Kinddatensätzen? Sinnvoll?
 *
 * @author Jörg Gottschling
 */
public class ReportByDatarecordCreator
{

  /** Logger for this class. */
  private static final Log log =
      LogFactory.getLog(ReportByDatarecordCreator.class);

  private ReportService reportService;

  /**
   * Der {@link ReportService} der zur Erzeugung des Reports genutzt werden* soll.
   */
  public void setReportService(ReportService reportService)
  {
    this.reportService = reportService;
  }

  /**
   * Konvertiert einen Parameter gemäß den angegebenen Parametern und wirft im Fehlerfall eine
   * entsprechende, aussagekräftige Exception.
   * 
   * @param argumentsMap
   *          Die Map die die Paramater enthält (mit Typ-Check auf Map)
   * @param parameterName
   *          Der Name des gesuchten Parameters
   * @param T
   *          Der erwartete Typ des Parameters
   * @return Der gecastete Parameter
   * @throws Exception
   *           Beim Fehlschlagen der Konvertierung mit Hinweis auf Parameternamen und erwarteten Typ
   * 
   * @author Markus Vollendorf
   */
  @SuppressWarnings("unchecked")
  private static <T> T getCastedParameter(Object argumentsMap,
      String parameterName, Class<T> T)
      throws Exception
  {

    // Konvertiert die angegebenen Parameter
    ConvertUtilsBean cub = new ConvertUtilsBean();

    // Konverter mit Exceptions (statt Defaultwerten)
    cub.register(new IntegerConverter(), Integer.class);
    cub.register(new StringConverter(), String.class);

    // Ausführen der Konvertierung
    Object cObject = null;
    try
    {

      // Mappen und Eintrag
      Map<String, Object> cArgs =
          (Map<String, Object>)cub.convert(argumentsMap, Map.class);
      cObject = cArgs.get(parameterName);

      // Keine Angabe
      if(cObject == null)
        return null;

      // Kein Konverter
      else if(cub.lookup(T) == null)
      {
        if(T.isInstance(cObject)) return (T)cObject;
        throw new Exception("Failed to cast parameter '" + parameterName
                            + "' in groovy api call to required type '"
                            + T.getName() + "'.");
      }

      // Vorhandener Konverter
      else
        cObject = cub.convert(cObject, T);

    }

    // Abfangen von Konvertierungsfehlern
    catch(Exception e)
    {
      throw new Exception("Failed to cast parameter '" + parameterName
                          + "' in groovy api call to required type '"
                          + T.getName() + "'.",
          e);
    }

    // Rückgabe
    return (T)cObject;

  }

  /**
   * Wählt wie die SQL-Function COALESCE() den ersten Parameter aus, der nicht <i>null</i> ist.
   * 
   * @param args
   *          Die Parameterliste (Varargs)
   * 
   * @return Den ersten Parameter der nicht <i>null</i> ist oder <i>null</i>.
   * 
   * @author Markus Vollendorf
   */
  @SafeVarargs
  private static <T> T coalesceParameter(T... args)
  {
    T resultParameter = null;
    for(int i = 0; i < args.length; i++)
    {
      if(args[i] != null)
      {
        resultParameter = args[i];
        break;
      }
    }
    return resultParameter;
  }

  /**
   * Generiert anhand einer Map einen Report. Die Map erwartet gewisse Parameter und hat auch
   * optionale Parameter, siehe dazu die Groovy-Beispiel-Library. Diese Funktion wird im Regelfall
   * aus einem Groovy-Element heraus in einem Prozess verwendet. Der gewöhnliche Datensatz-Kontext
   * ist hierbei niedriger priorisiert als die durch die Groovy-API eingegebenen Informationen -
   * deshalb ist dieser nicht zwingend notwendig für die Erzeugung.
   * 
   * Parameter innerhalb der Methode die mit <i>g</i> beginnen sind aus der Groovy-API übergebene
   * Parameter. Parameter die mit <i>t</i> beginnen sind die, mit denen letztendlich der Report
   * erzeugt werden wird.
   * 
   * TODO Ist allgemein etwas umständlich, da beispielsweise
   * {@link #resolveExportTargets(DataGroupInfo, String)} mehrfach aufgerufen wird. Macht aber dafür
   * die Handhabung einfacher, da nicht nachgestellt werden muss was dort passiert. Dafür werden
   * unter Umständen einige {@link DatarecordExportTarget}s unnötig erstellt.
   * 
   * TODO Parameter zur Deaktivierung des Standard-Verhaltens einer Report-Datengruppe
   * 
   * @param args
   *          Die Parameter-Map
   * @throws Exception
   *           Bei fehlenden Parametern oder bei Fehlschlägen bei der
   *           Konvertierung
   * 
   * @author Markus Vollendorf
   */
  @SuppressWarnings("unchecked")
  public void createReport(Map<String, Object> args)
      throws Exception
  {

    // Debugcheck
    Boolean gDebugMode = getCastedParameter(args, "debug", Boolean.class);
    if(gDebugMode == null) gDebugMode = false;

    // Die vier Hauptparameter
    IBusinessLogicProcessingContext gContext;
    try
    {
      gContext = getCastedParameter(args, "context", IBusinessLogicProcessingContext.class);
    }
    catch(Exception e)
    {
      FreeTimerProcessingContext f =
          getCastedParameter(args, "context", FreeTimerProcessingContext.class);
      gContext =
          new BusinessLogicProcessingContext(IntrexxApplicationContext.getInstance(),
              f.getConnection(), null, null, f.getSession(),
              f.getSharedState(), null);
    }
    Assert.notNull(gContext, "gContext is null.");
    String gReportIdentifier = getCastedParameter(args, "identifier", String.class);
    Map<String, Object> gDatarecord = getCastedParameter(args, "datarecord", Map.class);
    Collection<Map<String, String>> gFiles = getCastedParameter(args, "files", Collection.class);
    Map<String, Object> gParameters = getCastedParameter(args, "parameters", Map.class);

    // Stringbuilder zur Logausgabe für weitere Feldwerte (recordValues)
    StringBuilder additionalFieldsLogger = new StringBuilder();

    // Die endgültigen Parameter
    DefaultReportContext reportsContext = new DefaultReportContext(gContext);
    String tDatagroupGuid = null;
    String tRecordId = null;
    String tReportIdentifier = null;
    Map<String, ExportTarget> exportTargets =
        new HashMap<String, ExportTarget>();

    // Defaults sind - wenn möglich - erstmal aus dem Kontext
    if(gContext.internalPeekRecord() != null)
    {
      IDataRecord peek = gContext.internalPeekRecord();
      tRecordId = peek.getRecId();
      DataGroupInfo tDatagroup = peek.getDataGroupInfo();
      tDatagroupGuid = tDatagroup.getGuid();
      tReportIdentifier = tDatagroup.getSysIdent();
      exportTargets = resolveExportTargets(tDatagroup, tRecordId);
    }

    // Auszuführender Report
    tReportIdentifier = coalesceParameter(gReportIdentifier, tReportIdentifier);

    // Verarbeitung des Ziels "Datarecord"
    if(gDatarecord != null)
    {

      // Datengruppe
      String gDatagroupGuid = getCastedParameter(gDatarecord, "datagroupGuid", String.class);
      tDatagroupGuid = coalesceParameter(gDatagroupGuid, tDatagroupGuid);
      DataGroupInfo dataGroup = null;
      try
      {
        dataGroup = RtCache.getDataGroup(tDatagroupGuid);
      }
      catch(RtCacheException e)
      {
        throw new Exception("Invalid datagroup '" + tDatagroupGuid + "'.", e);
      }
      if(dataGroup == null)
        throw new Exception("Invalid datagroup '" + tDatagroupGuid + "'.");

      // Datensatz
      String gRecordId = getCastedParameter(gDatarecord, "recordId", String.class);
      Map<String, Object> gRecordValues =
          getCastedParameter(gDatarecord, "recordValues", Map.class);
      if(gDebugMode)
        if(gRecordValues != null && gRecordValues.size() > 0)
      {
        for(String s : gRecordValues.keySet())
        {
          additionalFieldsLogger.append("\n        -> ");
          additionalFieldsLogger.append(s);
          additionalFieldsLogger.append(" = ");
          additionalFieldsLogger.append(gRecordValues.get(s));
        }
      }
        else
        additionalFieldsLogger.append("\n        -> Keine");
      Boolean gNewRecord = getCastedParameter(gDatarecord, "newRecord", Boolean.class);
      if(Boolean.FALSE.equals(gNewRecord) && "-1".equals(gRecordId))
        throw new Exception(
            "Parameter 'newRecord' is false, but parameter 'recordId' is -1 (what implies a new record).");
      if(Boolean.TRUE.equals(gNewRecord) || "-1".equals(gRecordId))
        gRecordId = insertEmptyRecord(gContext, dataGroup, gRecordId, gRecordValues);
      else if(gRecordValues != null && gRecordValues.size() > 0)
        updateRecord(gContext, dataGroup, coalesceParameter(gRecordId, tRecordId), gRecordValues);
      tRecordId = coalesceParameter(gRecordId, tRecordId);

      // Evtl. andere Export-Targets
      exportTargets = resolveExportTargets(dataGroup, tRecordId);

      // Assertions
      Assert.notNull(tDatagroupGuid, "tDatagroupGuid is null.");
      Assert.notNull(tRecordId, "tRecordId is null.");

      // Überprüfung der evtl. angegebenen Exportziele
      Collection<Map<String, Object>> datafields =
          getCastedParameter(gDatarecord, "datafields", Collection.class);
      if(datafields != null)
      {
        for(Object dfEntry : datafields)
        {

          // Angaben des Exportziels aus der Groovy-API
          String gFormat = getCastedParameter(dfEntry, "format", String.class);
          String gFilename =
              getCastedParameter(dfEntry, "filename", String.class);
          String gIdentifier =
              getCastedParameter(dfEntry, "identifier", String.class);

          // Angaben des Exportziels
          String tFormat = null, tFilename = null, tIdentifier = null;

          // Suche nach ähnlichem Export-Target via Format
          if(gFormat != null)
          {
            gFormat = gFormat.toUpperCase();
            if(exportTargets.containsKey(gFormat))
            {
              ExportTarget contextTarget = exportTargets.get(gFormat);
              tFilename = ((DatarecordExportTarget)contextTarget).getFilename();
              tIdentifier = ((DatarecordExportTarget)contextTarget).getField().getGuid();
              tFormat = gFormat;
            }
          }

          // ... via Guid / Sysident / Spaltenname
          if(gIdentifier != null)
          {
            for(Map.Entry<String, ExportTarget> etEntry : exportTargets.entrySet())
            {
              ExportTarget exportTarget = etEntry.getValue();
              FieldInfo exportField = ((DatarecordExportTarget)exportTarget).getField();
              if(gIdentifier.equals(exportField.getGuid())
                 || gIdentifier.equals(exportField.getColumnName())
                 || gIdentifier.equals(exportField.getSysIdent()))
              {
                tFormat = etEntry.getKey();
                tFilename = ((DatarecordExportTarget)exportTarget).getFilename();
                tIdentifier = exportField.getGuid();
                exportTargets.remove(etEntry.getKey());
              }
            }
          }

          // Bestimmung der endgültigen Parameter - dabei hat Groovy die höhere Priorität
          tFormat = coalesceParameter(gFormat, tFormat);
          tFilename = coalesceParameter(gFilename, tFilename);
          tIdentifier = coalesceParameter(gIdentifier, tIdentifier);

          // Assertions
          Assert.notNull(tFormat, "tFormat is null.");
          Assert.notNull(tIdentifier, "tIdentifier is null.");

          // Datenfeld
          FieldInfo tField = getDatafieldByAnyIdentifier(dataGroup, tIdentifier);
          if(tField == null)
            throw new Exception("Invalid datafield '" + tIdentifier + "'.");

          // Neues Target
          tFormat = tFormat.toUpperCase();
          exportTargets.put(tFormat, new DatarecordExportTarget(dataGroup, tRecordId,
              tField, tFilename));

        }
      }

    }

    // Verarbeitung des Ziels "Files"
    if(gFiles != null)
    {
      for(Map<String, String> fileEntry : gFiles)
      {

        // Angaben des Exportziels aus der Groovy-API
        String gFormat = getCastedParameter(fileEntry, "format", String.class);
        String gPath = getCastedParameter(fileEntry, "path", String.class);
        String gFilename = getCastedParameter(fileEntry, "filename", String.class);

        // Assertions
        Assert.notNull(gFormat, "gFormat is null.");
        Assert.notNull(gPath, "gPath is null.");

        // Neues Target
        gFormat = gFormat.toUpperCase();
        exportTargets.put(gFormat, new FileSystemExportTarget(gPath, gFilename));

      }
    }

    // Einfügen der Parameter
    if(gParameters != null)
    {
      for(Map.Entry<String, Object> e : gParameters.entrySet())
      {
        Object o = e.getValue();
        if(o instanceof IValueHolder)
          reportsContext.setParameterValue(e.getKey(),
              ((IValueHolder<?>)o).getValue());
        else
          reportsContext.setParameterValue(e.getKey(), o);
      }
    }

    // Debug
    if(gDebugMode)
    {
      StringBuilder l = new StringBuilder();

      // Allgemeine Informationen
      l.append("Groovy-API-Call (Debug)");
      l.append("\n      * Report: ");
      l.append(tReportIdentifier);

      // Werte des Datensatzes
      l.append("\n      * Weitere neue Werte des Datensatzes");
      l.append(additionalFieldsLogger.toString());

      // Exportziele
      l.append("\n      * Export-Targets");
      if(exportTargets != null && exportTargets.size() > 0)
      {
        for(Map.Entry<String, ExportTarget> etEntry : exportTargets.entrySet())
        {
          ExportTarget exportTarget = etEntry.getValue();
          if(exportTarget instanceof DatarecordExportTarget)
          {
            String reportFilename = ((DatarecordExportTarget)exportTarget).getFilename();
            if(reportFilename == null) reportFilename = "<Report Default>";
            reportFilename += "." + etEntry.getKey();
            l.append("\n      * Datengruppe: ");
            l.append(tDatagroupGuid);
            l.append("\n      * Datensatz: ");
            l.append(tRecordId);
            l.append("\n        -> Format: ");
            l.append(etEntry.getKey());
            l.append(", Datafield: ");
            l.append(((DatarecordExportTarget)exportTarget).getField().getGuid());
            l.append(", Filename: ");
            l.append(reportFilename);
          }
          else if(exportTarget instanceof FileSystemExportTarget)
          {
            String reportFilename = ((FileSystemExportTarget)exportTarget).getFilename();
            if(reportFilename == null) reportFilename = "<Report Default>";
            reportFilename += "." + etEntry.getKey();
            l.append("\n        -> Format: ");
            l.append(etEntry.getKey());
            l.append(", Path: ");
            l.append(((FileSystemExportTarget)exportTarget).getPath());
            l.append(", Filename: ");
            l.append(reportFilename);
          }
        }
      }
      else
      {
        l.append("\n      -> Keine");
      }

      // Parameter des Berichts
      l.append("\n      * Parameters");
      if(gParameters != null && gParameters.size() > 0)
        for(Map.Entry<String, Object> e : gParameters.entrySet())
        {
          Object o = e.getValue();
          if(o instanceof IValueHolder) o = ((IValueHolder<?>)o).getValue();
          if(o == null)
          {
            l.append("\n        -> Name: ");
            l.append(e.getKey());
            l.append(", Class: -, Value: null");
          }
          else
          {
            l.append("\n        -> Name: ");
            l.append(e.getKey());
            l.append(", Class: ");
            l.append(o.getClass().getName());
            l.append(", Value: ");
            l.append(o);
          }
        }
      else
        l.append("\n        -> Keine");

      // Ausgabe der Informationen
      log.info(l.toString());
    }

    // Erzeugen des Reports
    createReport(reportsContext, tReportIdentifier, exportTargets);

  }

  /**
   * Generiert aus einem {@link ReadOnlyGroovyRecord} einen Bericht. Die Datengruppe in der sich der
   * Datensatz befindet muss als Sysident den Namen des Berichts haben. Erlaubt das Ausführen dieser
   * Logik aus dem GroovyScripting des Prozess Managers.
   * 
   * @see ReportByDatarecordCreator
   */
  public void createReport(IBusinessLogicProcessingContext context)
      throws Exception
  {
    createReport(context, context.internalPeekRecord());
  }

  /**
   * Generiert aus einem {@link ReadOnlyGroovyRecord} einen Bericht. Die Datengruppe in der sich der
   * Datensatz befindet muss als Sysident den Namen des Berichts haben. Erlaubt das Ausführen dieser
   * Logik aus dem GroovyScripting des Prozess Managers.
   * 
   * @see ReportByDatarecordCreator
   */
  public void createReport(IBusinessLogicProcessingContext context, ReadOnlyGroovyRecord record)
      throws Exception
  {
    createReport(new DefaultReportContext(context), record,
        resolveExportTargets(record.getDataGroupInfo(), record.getRecId()));
  }

  /**
   * Generiert aus einem {@link IDataRecord} einen Bericht. Die Datengruppe in der sich der
   * Datensatz befindet muss als Sysident den Namen des Berichts haben.
   * 
   * @see ReportByDatarecordCreator
   */
  public void createReport(IBusinessLogicProcessingContext context, IDataRecord record)
      throws Exception
  {
    createReport(new DefaultReportContext(context), record,
        resolveExportTargets(record.getDataGroupInfo(), record.getRecId()));
  }

  /**
   * Generiert aus einem einem {@link ReadOnlyGroovyRecord} einen Bericht. Die Datengruppe in der
   * sich der
   * Datensatz befindet muss als Sysident den Namen des Berichts haben.
   * 
   * @see ReportByDatarecordCreator
   */
  public void createReport(ReportContext context, ReadOnlyGroovyRecord record,
      Map<String, ExportTarget> exportTargets)
      throws Exception
  {
    createReport(context, resolveIdentifier(record.getDataGroupInfo()), exportTargets);
  }

  /**
   * Generiert aus einem einem {@link IDataRecord} einen Bericht. Die Datengruppe in der sich der
   * Datensatz befindet muss als Sysident den Namen des Berichts haben.
   * 
   * @see ReportByDatarecordCreator
   */
  public void createReport(ReportContext context, IDataRecord record,
      Map<String, ExportTarget> exportTargets)
      throws Exception
  {
    createReport(context, resolveIdentifier(record.getDataGroupInfo()), exportTargets);
  }

  /**
   * Generiert aus einem einem {@link ReadOnlyGroovyRecord} einen Bericht.
   * 
   * @see ReportByDatarecordCreator
   */
  public void createReport(ReportContext context, ReadOnlyGroovyRecord record, String identifier)
      throws Exception
  {
    createReport(context, record.getDataGroupInfo(), record.getRecId(), identifier);
  }

  /**
   * Generiert aus einem einem {@link IDataRecord} einen Bericht.
   * 
   * @see ReportByDatarecordCreator
   */
  public void createReport(ReportContext context, IDataRecord record, String identifier)
      throws Exception
  {
    createReport(context, record.getDataGroupInfo(), record.getRecId(), identifier);
  }

  private void createReport(ReportContext context, DataGroupInfo dataGroup, String recordId,
      String identifier)
      throws Exception
  {
    Map<String, ExportTarget> exportTargets = resolveExportTargets(dataGroup, recordId);
    if(isEmpty(exportTargets))
    {
      log.warn("Die Datengruppe enthält keine Datenfelder in den der Bericht "
               + "exportiert werden kann. Der Bericht wird deshalb nicht generiert.");
    }

    createReport(context, identifier, exportTargets);
  }

  /**
   * Versucht den Identifier des Reports aus dem Datensatz zu ermitteln.
   */
  String resolveIdentifier(DataGroupInfo datagroup)
  {
    String reportIdentifier = datagroup.getSysIdent();
    Assert.hasText(reportIdentifier,
        "Could not resolve the report identifier from the sysident of the datagroup!");
    return reportIdentifier;
  }

  /**
   * Generiert einen Bericht. Der Identifier des Reports muss explizit übergeben werden.
   * 
   * @see ReportByDatarecordCreator
   */
  public void createReport(ReportContext context, String reportIdentifier,
      Map<String, ExportTarget> exportTargets)
      throws Exception
  {
    Assert.hasText(reportIdentifier, "No report identifier!");
    Assert.notEmpty(exportTargets, "No export targets!");

    reportService.createReport(context, reportIdentifier, exportTargets);
  }

  /**
   * Liefert zu einer Datengruppe eine {@link Map} mit allen Format-IDs und
   * dem {@link FieldInfo} in dem der Report in diesem Format gespeichert
   * werden soll.
   */
  Map<String, ExportTarget> resolveExportTargets(DataGroupInfo datagroup, String recId)
  {
    Map<String, ExportTarget> exportTargets =
        new HashMap<String, ExportTarget>();

    final String dgGuid = datagroup.getGuid();
    for(FieldInfo field : RtCache.getFields(new IFilter<FieldInfo>()
    {

      @Override
      public boolean accept(FieldInfo fieldInfo)
      {
        return fieldInfo.getDataGroupGuid().equals(dgGuid) && fieldInfo.isFileField();
      }
    }))
    {
      if(startsWithIgnoreCase(field.getSysIdent(), "report"))
      {
        String format = substringAfter(field.getSysIdent(), "report");
        if(org.apache.commons.lang.StringUtils.isNotEmpty(format)
           && format.startsWith("_"))
          format = format.substring(1);
        if(org.apache.commons.lang.StringUtils.isNotEmpty(format))
        {
          format = format.toUpperCase();
          exportTargets.put(format, new DatarecordExportTarget(datagroup, recId, field));
        }
      }
    }

    if(log.isDebugEnabled()) log.debug("exportTargets: " + exportTargets);

    return exportTargets;
  }

  FieldInfo getDatafieldByAnyIdentifier(final DataGroupInfo dataGroup, final String identifier)
  {
    Validate.notEmpty(identifier, "identifier was null or empty");

    final String dgGuid = dataGroup.getGuid();
    return RtCache.getFirstField(new IFilter<FieldInfo>()
    {

      @Override
      public boolean accept(FieldInfo field)
      {
        boolean result = false;
        if(field.getDataGroupGuid().equals(dgGuid))
        {
          result |= identifier.equalsIgnoreCase(field.getSysIdent());
          result |= identifier.equalsIgnoreCase(field.getName());
          result |= identifier.equalsIgnoreCase(field.getColumnName());
          result |= identifier.equalsIgnoreCase(field.getGuid());
        }
        return result;
      }
    });
  }

  FieldInfo getDatafieldByColumnName(final DataGroupInfo dataGroup, final String identifier)
  {
    Validate.notEmpty(identifier, "identifier was null or empty");

    final String dgGuid = dataGroup.getGuid();
    return RtCache.getFirstField(new IFilter<FieldInfo>()
    {

      @Override
      public boolean accept(FieldInfo field)
      {
        return field.getDataGroupGuid().equals(dgGuid)
               && identifier.equalsIgnoreCase(field.getColumnName());
      }
    });
  }

  /**
   * Fügt einen neuen, bis auf die angegebenen Daten leeren Datensatz in eine Tabelle ein und gibt
   * den (möglicherweise zusammengesetzten) Primärschlüssel des neuen Datensatzes zurück.
   * 
   * TODO {@link IxblHelper}
   * 
   * @param ixContext
   *          Der Kontext von Intrexx
   * @param dataGroup
   *          Die Datengruppe für den neuen Datensatz
   * @param recordId
   *          Bei zusammengesetztem Primärschlüssel eine entsprechende Kodierung der neuen
   *          gewünschte Werte (KEY1=VALUE1, KEY2=VALUE2, ...). Ist an den meisten Stellen
   *          tolerant gegenüber Leerzeichen.
   * @param recordValues
   *          Weitere Werte im neuen Datensatz (zum Beispiel FKLID, ...) als Map mit beliebigem
   *          Identifier (Sysident, GUID, Spaltenname) als Key.
   * @return Primärschlüssel des neuen Datensatzes
   * @throws BlException
   * 
   * @author Markus Vollendorf
   */
  private String insertEmptyRecord(final IBusinessLogicProcessingContext ixContext,
      final DataGroupInfo dataGroup, final String recordId, final Map<String, Object> recordValues)
      throws BlException
  {

    // Der Primärschlüssel des neuen Datensatzes
    String resultRecId = null;

    try
    {

      // Sammelt die erforderliche Intrexx-Logik
      IRtDataGroup rtDataGroup = RtCreator.createDataGroup(ixContext, dataGroup);
      IDataRecord rtDataRecord = new DataRecord(dataGroup);
      rtDataRecord.setActionMode(ACTION.MERGE);

      // Übernimmt ggf. angegebene Werte
      if(recordValues != null)
      {
        for(String k : recordValues.keySet())
        {
          FieldInfo f = getDatafieldByAnyIdentifier(dataGroup, k);
          if(f == null)
            throw new BlException("Invalid identifier '" + k + "'.");
          rtDataRecord.setValue(f.getGuid(),
              ValueHolderFactory.getValueHolder(recordValues.get(k)));
        }
      }

      // Bei angegebenem, zusammengesetztem Primärschlüssel
      if(contains(recordId, ","))
      {
        Map<String, String> recIdParts =
            RecIdCodec.decodeRecId(formatDatarecordInformation(recordId));
        for(String k : recIdParts.keySet())
        {
          FieldInfo f = getDatafieldByColumnName(dataGroup, k);
          if(f == null)
            throw new BlException("Invalid column '" + k + "'.");
          rtDataRecord.setValue(f.getGuid(), ValueHolderFactory.getValueHolder(recIdParts.get(k)));
        }
      }

      // Ansonsten hoffentlich erzeugbar von Intrexx
      else
      {
        List<FieldInfo> primaryKeyFields = RtCache.getPrimaryKeyFields(new IFilter<FieldInfo>()
        {

          @Override
          public boolean accept(FieldInfo f)
          {
            return f.getDataGroupGuid().equals(dataGroup.getGuid());
          }
        });
        if(primaryKeyFields.size() != 1)
          throw new BlException(
              "Only single primary key values can be generated. You need to specify the wanted values!");
        for(FieldInfo primaryKey : primaryKeyFields)
          rtDataRecord.setValue(primaryKey.getGuid(),
              ValueHolderFactory.getValueHolder(rtDataGroup.generateNewPkValue(null, null)));
      }

      // Erzeugung des Datensatzes
      /*
       * TODO
       * 
       * Ist verantwortlich für Lock der Zieltabelle. Siehe
       * https://quinscape-de.socialcast.com/messages/21445298?ref=stream
       * 
       * http://msdn.microsoft.com/en-us/library/ms188929.aspx: BEGIN TRANSACTION starts a local
       * transaction for the connection issuing the statement. Depending on the current transaction
       * isolation level settings, many resources acquired to support the Transact-SQL statements
       * issued by the connection are locked by the transaction until it is completed with either a
       * COMMIT TRANSACTION or ROLLBACK TRANSACTION statement. Transactions left outstanding for
       * long periods of time can prevent other users from accessing these locked resources, and
       * also can prevent log truncation.
       * 
       * => Wohl nicht zu ändern, wenn es bei der Reihenfolge bleibt.
       */
      rtDataGroup.createDataUpdateHandler().createRecord(rtDataRecord);
      resultRecId = rtDataRecord.getRecId();

    }
    catch(BlException e)
    {
      throw e;
    }
    catch(Exception e)
    {
      throw new BlException(e.getMessage(), e);
    }

    // Rückgabe des Primärschlüssels des neuen Datensatzes
    return resultRecId;

  }

  /**
   * Aktualisiert einen bestehenden Datensatz mit den entsprechenden Werten.
   * 
   * TODO {@link IxblHelper}
   * 
   * @param ixContext
   *          Der Kontext von Intrexx
   * @param dataGroup
   *          Die Datengruppe des Datensatzes.
   * @param recordId
   *          Bei zusammengesetztem Primärschlüssel eine entsprechende Kodierung der neuen
   *          gewünschte Werte (KEY1=VALUE1, KEY2=VALUE2, ...). Ist an den meisten Stellen
   *          tolerant gegenüber Leerzeichen.
   * @param recordValues
   *          Die zu aktualisierenden Werte des Datensatzes als Map mit beliebigem
   *          Identifier (Sysident, GUID, Spaltenname) als Key.
   * @throws BlException
   * 
   * @author Markus Vollendorf
   */
  private void updateRecord(IBusinessLogicProcessingContext ixContext,
      DataGroupInfo dataGroup, String recordId, Map<String, Object> recordValues)
      throws BlException
  {
    try
    {

      // Sammelt die erforderliche Intrexx-Logik
      IRtDataGroup rtDataGroup = RtCreator.createDataGroup(ixContext, dataGroup);
      IDataRecord rtDataRecord = new DataRecord(dataGroup);
      rtDataRecord.setRecId(recordId);
      rtDataRecord.setActionMode("actMerge");

      // Übernimmt angegebene Werte
      for(String k : recordValues.keySet())
      {
        FieldInfo f = getDatafieldByAnyIdentifier(dataGroup, k);
        if(f == null)
          throw new BlException("Invalid identifier '" + k + "'.");
        rtDataRecord.setValue(f.getGuid(), ValueHolderFactory.getValueHolder(recordValues.get(k)));
      }

      // Aktualisieren des Datensatzes
      rtDataGroup.createDataUpdateHandler().updateRecord(rtDataRecord);

    }
    catch(BlException e)
    {
      throw e;
    }
    catch(Exception e)
    {
      throw new BlException(e.getMessage(), e);
    }
  }

  /**
   * Formatiert den zusammengesetzten Primärschlüssel gemäß dem erwarteten Format von Intrexx.
   * Toleriert dabei Leerzeichen am Anfang und Ende des Strings sowie um <i>,</i> und <i>=</i>.
   * 
   * @param s
   *          Der Primärschlüssel
   * @return Der neu formatierte Primärschlüssel
   */
  private String formatDatarecordInformation(String s)
  {
    if(s == null)
      return null;
    else
      return s.replaceAll("\\s*(,|=|^|$)\\s*", "$1").replaceAll("=", "= ").replaceAll(",", ", ");
  }

}
