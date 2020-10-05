/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.fill.params;

import java.util.List;

import de.uplanet.lucy.server.businesslogic.IBusinessLogicProcessingContext;
import de.uplanet.lucy.server.businesslogic.rtdata.IRtDataGroup;
import de.uplanet.lucy.server.businesslogic.util.IDataRecord;
import de.uplanet.lucy.server.dataobjects.IValueHolder;
import de.uplanet.lucy.server.rtcache.FieldInfo;
import de.uplanet.lucy.server.rtcache.RtCache;
import de.uplanet.lucy.server.rtcache.filter.FieldInfoColumnNameFilter;
import de.uplanet.lucy.server.rtcache.filter.FieldInfoSysIdentFilter;
import de.uplanet.util.filter.IFilter;

import de.quinscape.intrexx.reports.ReportContext;
import de.quinscape.intrexx.reports.domain.IntrexxApplicationReport;

import net.sf.jasperreports.engine.JRParameter;

/**
 * Ein {@link ParameterValueCreator} der aus dem aktuellen Datensatz des
 * {@link IBusinessLogicProcessingContext} den aktuellen Wert für das
 * Datenfeld holt dessen Datenbankspalte (Name) oder Sysident wie der
 * Reportparameter heißt.
 * 
 * @author Jörg Gottschling
 */
public class DataRecordParameterValueCreator
    implements ParameterValueCreator
{

  @Override
  public ReportParameterValueHolder createParameterValue(ReportContext context,
      IntrexxApplicationReport report, JRParameter parameter)
  {
    ReportParameterValueHolder result = new ReportParameterValueHolder();

    IDataRecord dataRecord = context.ix().internalPeekRecord();
    if(dataRecord != null)
    {
      IRtDataGroup dataGroup = context.ix().internalPeekRtDataGroup();
      if(dataGroup != null)
      {
        IFilter<FieldInfo> fieldColumnFilter =
            new FieldInfoColumnNameFilter(dataGroup.getGuid(), parameter.getName());
        List<FieldInfo> fieldInfos = RtCache.getFields(fieldColumnFilter);

        if(fieldInfos.size() == 0)
        {
          IFilter<FieldInfo> fieldSysidentFilter =
              new FieldInfoSysIdentFilter(dataGroup.getGuid(), parameter.getName());
          fieldInfos = RtCache.getFields(fieldSysidentFilter);
        }

        if(fieldInfos.size() > 0)
        {
          FieldInfo fieldInfo = fieldInfos.get(0);

          if(fieldInfo != null)
          {
            IValueHolder<?> vh =
                dataRecord.getValueHolderByFieldGuid(fieldInfo.getGuid());
            if(vh != null) result.setValue(vh.getValue());
          }
        }
      }
    }
    return (result.hasValue() ? result : null);
  }
}
