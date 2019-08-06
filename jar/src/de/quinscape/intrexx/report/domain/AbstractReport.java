/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain;

import java.util.*;

/**
 * Basisimplementierung von {@link Report}.
 * 
 * @author Jörg Gottschling
 */
public abstract class AbstractReport
    implements Report
{

  private String displayName;

  private String guid;

  private Set<String> permittedOrgGuids;

  @Override
  public String getDisplayName()
  {
    return displayName;
  }

  @Override
  public String getGuid()
  {
    return guid;
  }

  @Override
  public Set<String> getPermittedOrgGuids()
  {
    return permittedOrgGuids;
  }

  /**
   * Siehe {@link Report#getDisplayName()}.
   */
  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  /**
   * Siehe {@link Report#getGuid()}.
   */
  public void setGuid(String guid)
  {
    this.guid = guid;
  }

  /**
   * Siehe {@link Report#getPermittedOrgGuids()}.
   */
  public void setPermittedOrgGuids(Set<String> permittedOrgGuids)
  {
    this.permittedOrgGuids = permittedOrgGuids;
  }
}
