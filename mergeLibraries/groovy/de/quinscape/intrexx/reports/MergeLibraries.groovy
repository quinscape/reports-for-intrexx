/*
 * (C) Copyright 2014 QuinScape GmbH
 * All Rights Reserved.
 *
 * http://www.quinscape.de
 *
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.io.FileUtils
import de.uplanet.io.ZipHelper

import de.uplanet.lucy.property.*;

import groovy.util.XmlSlurper

/**
 * Fügt die Libraries aus einem Jasper-Reports-Release, Intrexx und Reports
 * selbst zusammen, damit dies nicht jedes Mal von Hand gemacht werden muss.
 *
 * @author Markus Vollendorf
 */
class MergeLibraries
{

  static File mergedFolder, licenseFolder, sourceFolder
  static File ixFolder = getIntrexxFolder()
  static File reportsFolder = new File("../portal/lib")
  static List unnecessaryLibraries = [
    /bsh.+/,
    /castor.+/,
    /cglib.+/,
    /cincom.+/,
    /commons-beanutils-1.8.0.jar/,
    /commons-dbcp.+/,
    /commons-pool.+/,
    /commons-vfs.+/,
    /ehcache.+/,
    /eigenbase.+/,
    /ejb3.+/,
    /hadoop.+/,
    /hibernate.+/,
    /hive.+/,
    /jackson.+/,
    /jakarta-bcel-20050813.jar/,
    /jasperreports-(applet|javaflow).+/,
    /jasperreports-json.jar/,
    /javacup.+/,
    /jpa.+/,
    /js_activation.+/,
    /js_axis.+/,
    /js_commons.+/,
    /js_commons-discovery.+/,
    /js_jasperserver.+/,
    /js_jaxrpc.+/,
    /js_mail.+/,
    /js_saaj-api.+/,
    /js_wsdl4j.+/,
    /js_wsdl4j.+/,
    /js-hive.+/,
    /libfb303.+/,
    /mondrian.+/,
    /olap4j.+/,
    /org.springframework.+-3.+/,
    /sqleonardo.+/,
    /* Xalan darf nicht mit installiert werden, da es Probleme in Intrexx verursacht (z.B. können
     * Applikationen nicht mehr veröffentlicht werden). Es kann aber ausgelassen werden, da die
     * relvanten Teile bereits sowohl in Xerces von Intrexx und in der serializer.jar enthalten sind.
     */
    /xalan-2.7.+/
  ]

  static List ignoredFiles = [
    "org/w3c/dom/UserDataHandler.class",
    "bcel-5.2.jar",
  ]

  private static File getIntrexxFolder()
  {
    def rootNode = new XmlSlurper().parse(new File("../.classpath"))
    def pathNodes = rootNode.children().@path*.text()
    def ixNode = pathNodes.findAll { it.contains("ixlib") }
    assert ixNode.size() == 1
    def ixFile = new File("../.." + ixNode.first() + "/jars/lib");
    if(!ixFile.isDirectory())
      throw new FileNotFoundException(ixFile.canonicalPath + " ist kein Verzeichnis.")
    return ixFile
  }

  static main(args)
  {
    println "Intrexx-Ordner: ${ixFolder.canonicalPath}"
    println "Suche nach jasperreports-*.zip in ${new File('.').absolutePath} ..."

    // Auswahl des zu mergenden Releases
    List jasperReleases = []
    new File(".").eachFileMatch(~/jasperreports-.+-project.zip/, { jasperReleases << it })
    if (!jasperReleases)
    {
      println "In diesem Verzeichnis gibt es keine Datei namens 'jasperreports-...-project.zip'."
      System.exit(1)
    }
    int selectedRelease = 0
    if (jasperReleases.size() > 1)
    {
      int i = 0
      println "Bitte wähle das zu mergende Release:"
      jasperReleases.each { println "[${i++}] ${it.name}" }
      print "Auswahl: "
      selectedRelease = System.in.newReader().readLine().toInteger()
    }

    // Verzeichnis zum Merge
    def releaseName = jasperReleases[selectedRelease].name.find(/jasperreports-[\d.]{2,}/)
    println "Jasper-Reports $releaseName wird genutzt."
    mergedFolder = new File(releaseName + "-libs")
    sourceFolder = new File(releaseName + "-src")
    licenseFolder = new File(mergedFolder, "license")

    // Entpacken des Releases
    if (mergedFolder.exists())
    FileUtils.deleteQuietly(mergedFolder)
    def zipFile = new ZipFile(jasperReleases[selectedRelease])
    zipFile.entries.each { ZipArchiveEntry zae ->
      List folderName = zae.name.split("/")
      if (folderName.size() == 3 && ["lib", "dist"].contains(folderName[1]) && folderName[2] != "docs")
      {
        String targetName = zae.name.replaceAll(/^.+\/(.+)$/, "\$1")
        if (!targetName.toLowerCase().endsWith(".jar"))
        targetName = targetName.replaceAll("^", "license/")
        FileUtils.copyInputStreamToFile(zipFile.getInputStream(zae), new File(mergedFolder, targetName))
      }
      if (folderName.size() > 2 && folderName[1] == "src" && !zae.name.endsWith("/"))
      {
        FileUtils.copyInputStreamToFile(zipFile.getInputStream(zae), new File(sourceFolder, folderName[2..-1].join(File.separator)))
      }
    }

    // Zippen der Quellen
    ZipHelper.zipDir(new File(releaseName + "-src.zip"), sourceFolder)
    FileUtils.deleteQuietly(sourceFolder)

    // Erstmal alles aus dem neuen Release
    List allLibs = []
    Map jasperLibs = mergedFolder.listFiles().findAll { !it.isDirectory() }.collectEntries {
      def libraryName = getLibraryName(it.name)
      if (!unnecessaryLibraries.any { u -> it.name ==~ u })
      {
        if (allLibs.contains(libraryName))
        println "ACHTUNG: $libraryName ist bereits im Release mehrfach inkludiert."
        allLibs << libraryName
        [(libraryName): it]
      }
      else
      {
        println "HINWEIS: ${it.name} aus dem Release wurde entfernt, weil diese in unnecessaryLibraries gelistet ist."
        it.delete()
        return [:]
      }
    }

    // Intrexx hat Vorrang
    ixFolder.eachFile {
      if (!it.isDirectory())
      {
        def libraryName = getLibraryName(it.name)
        if (jasperLibs.containsKey(libraryName))
        {
          jasperLibs[libraryName].delete()
          licenseFolder.eachFile {
            if (it.name.startsWith(libraryName + "-"))
            {
              it.delete()
            }
          }
          println "HINWEIS: ${it.name} aus dem Release wurde entfernt, weil Intrexx diese selbst mitliefert."
        }
      }
    }

    // Eigene Reportslibraries
    reportsFolder.eachFile {
      if (!it.isDirectory())
      {
        def libraryName = getLibraryName(it.name)
        if (!allLibs.contains(libraryName) && !unnecessaryLibraries.any { u -> it.name ==~ u })
        {
          allLibs << libraryName
          FileUtils.copyFileToDirectory(it, mergedFolder)
        }
        else if (!unnecessaryLibraries.any { u -> it.name ==~ u })
        {
          println "HINWEIS: ${it.name} wurde aus Reports nicht übernommen, da diese in Intrexx und/oder Jasper Reports dabei ist."
        }
      }
    }
    FileUtils.copyDirectory(new File(reportsFolder, "license"), licenseFolder)

    // Löschen der überflüssigen Lizenzdateien
    licenseFolder.eachFile {
      def libraryName = getLibraryName(it.name)
      if (!allLibs.contains(libraryName))
      it.delete()
    }

    // Überprüfen zur Sicherheit
    def allClasses = [:]
    mergedFolder.eachFile { File f ->
      if (!f.isDirectory())
      {
        ZipFile z = new ZipFile(f)
        z.getEntries().each {
          def fileName = it.name
          if (fileName.endsWith(".class"))
          {
            if (allClasses.containsKey(fileName))
            allClasses[fileName] << f.name
            else
            allClasses[fileName] = [f.name]
          }
        }
        z.close()
      }
    }
    allClasses.keySet().each {
      if (allClasses[it].size() > 1 && !ignoredFiles.any { i -> it == i || allClasses[it].contains(i) })
      println "ACHTUNG: Klasse $it ist mehrfach im zusammengeführten Ordner: ${allClasses[it]}."
    }

    // Abschluss
    println ""
    println "---"
    println ""
    println "Zusammenstellung ist im Ordner '$mergedFolder' erfolgt!"
    println "Nun muss die STS beendet, das Lib-Verzeichnis im Portal durch das neue ersetzt werden und anschließend der Classpath angepasst (Beanutils, iText, Jasperreports, Spring-Jdbc)."

  }

  // Zusammenstellen der Libraries
  private static String getLibraryName(String it) {
    def libraryName = it.split(/[\\/]/).last()
    libraryName = libraryName =~ /(.+)\.[\w]+/
    libraryName = libraryName.matches() ? libraryName[0][1] : null
    if (libraryName =~ /[\d.]{2,}/) {
      libraryName = libraryName[0..libraryName.lastIndexOf(libraryName.find(/([\d.]{2,})/))-2]
    }
    return libraryName
  }

}
