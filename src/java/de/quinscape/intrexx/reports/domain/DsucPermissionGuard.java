/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.domain;

import static org.springframework.util.CollectionUtils.containsAny;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.quinscape.intrexx.reports.IntrexxReportException;
import de.quinscape.intrexx.reports.ReportContext;

import de.uplanet.lucy.server.usermanager.usecases.IDsucGetMembership;

/**
 * Ein {@link PermissionGuard} dessen Implementierung auf die die "directory
 * service use cases" (kurz DSUC) der Intrexx-API zurückgreift.
 * 
 * @author Jörg
 */
public class DsucPermissionGuard
    implements PermissionGuard
{

  /** Logger for this class. */
  private static final Log log = LogFactory.getLog(DsucPermissionGuard.class);

  private IDsucGetMembership dsucGetMembership;

  @Override
  public boolean isReportCreationPermitted(ReportContext context,
      IntrexxApplicationReport report)
  {
    Set<String> userOrgGuids;
    try
    {
      userOrgGuids =
          dsucGetMembership.fromUserId(context.ix().getConnection(),
              context.ix().getSession().getUser().getGuid());
    }
    catch(Exception exc)
    {
      String msg =
          "Error while trying to get memberships for user for report authorisation.";
      log.error(msg, exc);
      throw new IntrexxReportException(
          "Error while trying to get memberships for user for report authorisation.",
          exc);
    }

    return containsAny(userOrgGuids, report.getPermittedOrgGuids());
  }

  /**
   * Der "directory service use cases" (kurz DSUC) vom Typ {@link IDsucGetMembership} der genutzt
   * werden soll um die Mengen zu
   * ermitteln in denen der aktuelle Benutzer Mitglied ist.
   */
  public void setDsucGetMembership(IDsucGetMembership dsucGetMembership)
  {
    this.dsucGetMembership = dsucGetMembership;
  }

}
