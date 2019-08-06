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

import de.uplanet.lucy.server.businesslogic.*;
import de.uplanet.lucy.server.businesslogic.rtdata.*;
import de.uplanet.lucy.server.businesslogic.util.*;
import de.uplanet.lucy.server.dataobjects.*;
import de.uplanet.lucy.server.rtcache.*;
import de.uplanet.lucy.server.rtcache.filter.*;
import de.uplanet.util.filter.*;

import de.quinscape.intrexx.report.ReportContext;
import de.quinscape.intrexx.report.domain.*;

import net.sf.jasperreports.engine.*;

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
      Report report, JRParameter parameter)
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
