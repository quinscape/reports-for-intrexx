/*
 * (C) Copyright 2008 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import de.uplanet.jdbc.JdbcConnection;
import de.uplanet.lucy.server.ContextConnection;
import de.uplanet.lucy.server.ContextLanguage;
import de.uplanet.lucy.server.DefaultLanguage;
import de.uplanet.lucy.server.DefaultTimeZone;
import de.uplanet.lucy.server.SharedState;
import de.uplanet.lucy.server.businesslogic.IBusinessLogicProcessingContext;
import de.uplanet.lucy.server.connector.IServerBridgeRequest;
import de.uplanet.lucy.server.engine.http.IViewData;
import de.uplanet.lucy.server.layout.ContextLayout;
import de.uplanet.lucy.server.session.ISession;
import de.uplanet.lucy.server.usermanager.IUser;
import de.uplanet.util.Guid;

import de.quinscape.intrexx.reports.domain.IntrexxApplicationReport;

/**
 * Standard-Implementierung von {@link ReportContext}.
 * 
 * @author Jörg Gottschling
 */
public class DefaultReportContext
    implements ReportContext
{

  private final String id;

  private IntrexxApplicationReport report;

  private Map<String, Object> reportParameters = new HashMap<String, Object>();

  final IBusinessLogicProcessingContext ctx;

  /**
   * Erzeugt eine Instanz von <code>DefaultReportContext</code>.
   */
  public DefaultReportContext(IBusinessLogicProcessingContext ctx)
  {
    this.id = Guid.createInstance().toString();
    this.ctx = ctx;
    setParameterValue("REPORT_CONTEXT", this);
  }

  @Override
  public Map<String, Object> getReportParameters()
  {
    return reportParameters;
  }

  @Override
  public IntrexxApplicationReport getReport()
  {
    return report;
  }

  @Override
  public void setReport(IntrexxApplicationReport report)
  {
    this.report = report;
  }

  @Override
  public JdbcConnection getDbConnection()
  {
    return ctx.getConnection();
  }

  @Override
  public JdbcConnection getDbConnection(String name)
  {
    return ContextConnection.get(name);
  }

  @Override
  public IServerBridgeRequest getRequest()
  {
    return ctx.getRequest();
  }

  @Override
  public ISession getSession()
  {
    return ctx.getSession();
  }

  @Override
  public IUser getUser()
  {
    return getSession().getUser();
  }

  @Override
  public String getDefaultLanguage()
  {
    return DefaultLanguage.get();
  }

  @Override
  public TimeZone getDefaultTimeZone()
  {
    return DefaultTimeZone.get();
  }

  @Override
  public String getLanguage()
  {
    return ContextLanguage.get(getRequest(), getSession());
  }

  @Override
  public String getLayout()
  {
    return ContextLayout.get(ctx);
  }

  @Override
  public IBusinessLogicProcessingContext getBusinessLogicProcessingContext()
  {
    return ctx;
  }

  @Override
  public SharedState getSharedState()
  {
    return ctx.getSharedState();
  }

  @Override
  public IViewData getSourceViewData()
  {
    return ctx.getViewData();
  }

  @Override
  public IBusinessLogicProcessingContext ix()
  {
    return ctx;
  }

  @Override
  public net.sf.jasperreports.engine.ReportContext jr()
  {
    return this;
  }

  @Override
  public boolean containsParameter(String parameterName)
  {
    return reportParameters.containsKey(parameterName);
  }

  @Override
  public String getId()
  {
    return id;
  }

  @Override
  public Object getParameterValue(String parameterName)
  {
    return reportParameters.get(parameterName);
  }

  @Override
  public void setParameterValue(String parameterName, Object parameterValue)
  {
    reportParameters.put(parameterName, parameterValue);
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.jasperreports.engine.ReportContext#getParameterValues()
   */
  @Override
  public Map<String, Object> getParameterValues()
  {
    // TODO: Ist das richtig?
    return reportParameters;
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.jasperreports.engine.ReportContext#clearParameterValues()
   */
  @Override
  public void clearParameterValues()
  {
    reportParameters.clear();
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.jasperreports.engine.ReportContext#removeParameterValue(java.lang.String)
   */
  @Override
  public Object removeParameterValue(String key)
  {
    return reportParameters.remove(key);
  }
}
