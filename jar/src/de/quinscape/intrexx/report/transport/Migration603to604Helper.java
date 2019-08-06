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

import java.io.*;

import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import de.uplanet.io.*;
import de.uplanet.lucy.server.businesslogic.*;
import de.uplanet.lucy.server.businesslogic.util.*;
import de.uplanet.lucy.server.dataobjects.*;

import de.quinscape.intrexx.report.transport.jaxb.v1.*;

import static de.quinscape.intrexx.report.ixbl.IxblHelper.*;

/**
 * Aus dem Migrationprozess heraus aufgerufene Hilfsklasse für die Migration von exakt <i>Reports
 * für Intrexx</i> <code>6.0.3</code> zu exakt <code>6.0.4</code>. Hintergrund ist eine
 * Inkompatiblität von Reports 6.0.3 zu ORACLE, welches über die neue Datengruppe Pakete hinzu kam.
 * Die Datenfelder "Kommentar" und "Typ" hatten als Spaltennamen die in ORACLE reservierten Wörter:
 * <code>COMMENT</code> und <code>TYPE</code>.
 * <p>
 * Das Feld Typ könnten wir einfach weglassen. Das Feld Kommentar müssen wir aber migrieren. Es
 * wurde gelöscht (wichtig) und als neues Feld wieder angelegt. Nach der Migration sind damit aber
 * alle bereits in der Datenbank vorhandenen Kommentare weg. Sie sind aber in den Paketen noch
 * vorhanden. Deshalb wurde ein Intrexx-Prozess entwickelt, welcher über alle in der Datengruppe
 * vorhandenen Pakete läuft und jeweils aus der Datei den Kommentar lädt und wieder in die Datenbank
 * schreibt. Dies übernimmt diese Hilfsklasse, welche auf das bisherige Package
 * <code>transport</code> zurück greift. Der Prozess erzeugt nur eine Instanz und ruft
 * <code>reloadPackageFromFile</code> auf.
 * <p>
 * Diese Hilfsklasse gehört eigentlich auch in der Architektur an einer andere Stelle, aber es ist
 * eh geplant diese nach Version 6.0.4 (bzw. spätestens zu 7.0.0) wieder zu entfernen.
 * <p>
 * Die Klasse und die Methode ist in der Form leider faktisch nicht mit Unit-Tests testbar.
 * 
 * @author Jörg Gottschling
 */
public class Migration603to604Helper
    extends Transport
{

  /**
   * Logger for this class.
   */
  private static final Log log = LogFactory.getLog(Migration603to604Helper.class);

  /**
   * TODO Doku
   * Erzeugt eine Instanz mit dem Übergebenen <code>IBusinessLogicProcessingContext</code>.
   */
  public Migration603to604Helper(@SuppressWarnings("unused") IBusinessLogicProcessingContext context)
  {
    ;
  }

  /**
   * Lädt die Meta-Daten des Export aus ZIP-Datei des Paket und speichert den Kommentar erneut in
   * der Datenbank ab.
   * 
   * @param recId
   *          Datensatz-ID des Pakets.
   */
  public void reloadPackageInfoFromFile(String recId)
      throws BlException
  {
    if(log.isDebugEnabled())
      log.debug("loadCommentFromPackageFile(String recId=" + recId + ") - start");
    Validate.notEmpty(recId, "Parameter recId was null or empty.");

    IFileValueHolder zipFileVH =
        getFileValueHolder("qsrep_packages", "file_package", recId);

    File unzippedPackageFolder = unzipFileToTempFolder(zipFileVH.getFirstFile().getPath());
    validateXmlFiles(unzippedPackageFolder);
    Export export = unmarshalPackage(unzippedPackageFolder);

    // Kommentar direkt in die Datenbank speichern
    IDataRecord blRecord = getDataRecord("qsrep_packages", recId);
    setValue(blRecord, "comment", export.getComment());
    parseDataRecord(blRecord);

    // Entfernen des Imports
    IOHelper.deleteFileRecursively(unzippedPackageFolder);

    if(log.isDebugEnabled())
      log.debug("loadCommentFromPackageFile(String recId=" + recId + ") - end");
  }
}
