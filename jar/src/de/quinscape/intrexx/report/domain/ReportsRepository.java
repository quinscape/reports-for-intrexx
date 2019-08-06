/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain;


/**
 * Bietet den Zugriff auf Berichte.
 * 
 * @author Jörg Gottschling
 */
public interface ReportsRepository
{

  /**
   * Liefert einen bestimmten Bericht aus diesem Repository. Das
   * gelieferte Domänenobjekt enthält alle nötigen Unterobjekte.
   * 
   * @param reportIdentifier
   *          Entweder die GUID oder der Name des Berichts.
   * 
   * @return Das Domänenobjekt.
   */
  Report findReport(String reportIdentifier);


}
