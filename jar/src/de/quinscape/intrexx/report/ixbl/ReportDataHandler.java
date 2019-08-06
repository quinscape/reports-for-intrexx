/*
 * (C) Copyright 2008 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.ixbl;

import static de.uplanet.lucy.constants.ACTION.MERGE;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import de.uplanet.lucy.server.IProcessingContext;
import de.uplanet.lucy.server.binding.IAppBinding;
import de.uplanet.lucy.server.businesslogic.BlException;
import de.uplanet.lucy.server.businesslogic.IBusinessLogicProcessingContext;
import de.uplanet.lucy.server.businesslogic.rtdata.IRtDataGroup;
import de.uplanet.lucy.server.businesslogic.rtdata.IRtDataUpd;
import de.uplanet.lucy.server.businesslogic.rtdata.jdbc.RtDataGroupTable;
import de.uplanet.lucy.server.businesslogic.rtdata.jdbc.RtDataUpdTable;
import de.uplanet.lucy.server.businesslogic.util.IDataRecord;
import de.uplanet.lucy.server.rtcache.DataGroupInfo;
import de.uplanet.lucy.server.spring.configuration.IntrexxApplicationContext;

/**
 * Eine {@link IRtDataGroup} (UP) welche beim Anlegen von Datensätzen aus
 * den übergebenen Daten einen Report erzeugt und diesen in der Datengruppe
 * selber speichert.
 * <p>
 * Zur Berichterzeugung siehe {@link ReportByDatarecordCreator}.
 * <p>
 * Dieser "datagroup datahandler" kann auch mittels Prozessmanager oder WebServices angestoßen
 * werden! Die erzeugten Berichte können mittels Prozessmanager oder auch einer normalen
 * Schaltfläche per E-Mail verschickt werden.
 * 
 * @see ReportByDatarecordCreator
 * 
 * @author Jörg Gottschling
 */
public class ReportDataHandler
    extends RtDataGroupTable
{

  /**
   * DOKU: DE_QUINSCAPE_INTREXX_REPORTS_DATAHANDLER_FILEHELPER_INVOKED in ReportDataHandler.java.
   */
  static final String FILEHELPER_INVOKED =
      "de.quinscape.intrexx.reports.datahandler.filehelper.invoked";

  /** Logger for this class. */
  private static final Log log = LogFactory.getLog(ReportDataHandler.class);

  /**
   * Erzeugt eine Instanz von <code>ReportDataHandler</code>.
   */
  protected ReportDataHandler()
  {
    super(null, null, null);
    log.debug("ReportDataHandler()");
  }

  /**
   * Erzeugt eine Instanz von <code>ReportDataHandler</code>.
   */
  public ReportDataHandler(IProcessingContext context,
      IAppBinding binding, DataGroupInfo dataGroupInfo)
  {
    super(context, binding, dataGroupInfo);
    if(log.isDebugEnabled()) log.debug("ReportDataHandler(guid=" + dataGroupInfo.getGuid() + ")");
  }

  @Override
  public IRtDataUpd createDataUpdateHandler()
  {

    return new RtDataUpdTable(this)
    {

      // wird noch vor dem Anstoßen der Workflows aufgerufen
      @Override
      public void updateAction(IDataRecord record)
          throws BlException
      {
        super.updateAction(record);
        // TODO Fix um nicht durch FileUCHelper.moveFileToIntrexx in DatarecordExportTarget eine
        // Endlosschleife zu bilden.
        createReport(record);
      }

    };
  }

  void createReport(IDataRecord record)
      throws BlException
  {
    try
    {
      log.debug("createReport() actionId = " + record.getActionMode());
      if(MERGE.equals(record.getActionMode()))
      {
        log.debug("createReport() calling report creator");
        getReportCreator().createReport((IBusinessLogicProcessingContext) m_ctx, record);
      }
    }
    catch(Exception exc)
    {
      log.error(exc.getMessage(), exc);
      throw new BlException(exc.getMessage(), exc);
    }
  }

  ReportByDatarecordCreator getReportCreator()
  {
    ReportByDatarecordCreator reportCreator =
        getApplicationContext().getBean(
            "quinscape.reports.datarecordReportCreator",
            ReportByDatarecordCreator.class);
    Assert.notNull(
        reportCreator,
        "Can not get the bean 'quinscape.reportsByDatarecordCreator' from the Spring application context of Intrexx!");
    return reportCreator;
  }

  ApplicationContext getApplicationContext()
  {
    Assert.notNull(IntrexxApplicationContext.getInstance(),
        "Can not get the default Spring application context of Intrexx!");
    return IntrexxApplicationContext.getInstance();
  }

}
