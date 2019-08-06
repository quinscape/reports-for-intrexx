/*
 * (C) Copyright 2010 QuinScape GmbH All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.domain.fill;

/**
 * Ein {@link ParameterValueCreator} der aus dem aktuellen Datensatz (mit
 * Hilfe der RecId) den aktuellen Wert für das Datenfeld aus der Datenbank
 * holt. Der Reportparameter muss benannt sein wie die Datenbankspalte,
 * der SysIdent oder die Guid.
 * 
 * @author Markus Vollendorf
 * 
 * @deprecated seit 6.0.1
 *             ersetzt durch {@link DatabaseParameterValueCreator}
 */
@Deprecated
public class DatabaseQxfcParameterValueCreator
    extends
    DatabaseParameterValueCreator
{
  /* alles extrahiert nach DatabaseParameterValueCreator */
}
