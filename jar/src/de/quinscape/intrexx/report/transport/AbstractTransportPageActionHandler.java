/*
 * (C) Copyright 2014 QuinScape GmbH
 * All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.transport;

import de.uplanet.lucy.server.businesslogic.*;
import de.uplanet.lucy.server.businesslogic.handler.*;
import de.uplanet.lucy.server.businesslogic.util.*;
import de.uplanet.lucy.server.rtcache.*;

/**
 * Leere Teilimplementiert von {@link AbstractTransportPageActionHandler} und
 * <code>IPageActionHandler</code>. Package-private. Reine Implementationsvererbung.
 * 
 * @author Jörg Gottschling
 */
abstract class AbstractTransportPageActionHandler
    extends Transport
    implements IPageActionHandler
{

  /**
   * Empty Implementation.
   */
  @Override
  public void processBefore(IBusinessLogicProcessingContext intrexxContext, IDataRecord datarecord,
      PageInfo pageInfo, String actionType)
      throws BlException
  {
    // Leer. Tut nichts.
    ;
  }

  
  /**
   * Empty Implementation.
   */
  @Override
  public void processAfter(IBusinessLogicProcessingContext intrexxContext, IDataRecord dataRecord,
      PageInfo pageInfo,
      String actionType)
      throws BlException
  {
    // Leer. Tut nichts.
    ;
  }

}
