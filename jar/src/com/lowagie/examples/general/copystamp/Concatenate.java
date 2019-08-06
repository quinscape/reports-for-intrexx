/*
 * This code is free software. It may only be copied or modified if you
 * include the following copyright notice:
 * 
 * This class by Mark Thompson. Copyright (c) 2002 Mark Thompson.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * itext@lowagie.com
 */

/**
 * This class demonstrates copying a PDF file using iText.
 * 
 * @author Mark Thompson, Phil Moston, Jörg Gottschling
 */

package com.lowagie.examples.general.copystamp;

import java.io.*;
import java.util.*;
import java.util.List;

import org.apache.commons.logging.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

/**
 * Tool that can be used to concatenate existing PDF files.
 */
public class Concatenate
{

  /** Logger dieser Klasse. */
  private static Log log = LogFactory.getLog(Concatenate.class);

  /**
   * Default Constructor to make it callable compatible.
   */
  public Concatenate()
  {
    log.debug("Concatenate instantiated");
  }

  /**
   * Concatinates PDF files.
   * 
   * @param fileNames
   *          List with paths of the files to concatenate. Must be in the
   *          right order. The last file is the destination.
   */
  public void concatenate(List<String> fileNames)
  {
    log.debug("concatenate(ArrayList) - start");

    concatenate(fileNames.toArray(new String[fileNames.size()]));

    log.debug("concatenate(ArrayList) - end");
  }

  /**
   * Concatinates PDF files.
   * 
   * @param fileNames
   *          Array with paths of the files to concatenate. Must be in the
   *          right order. The last file is the destination.
   */
  public void concatenate(String[] fileNames)
  {
    log.debug("concatenate(Object[]) - start");

    if(fileNames == null || fileNames.length < 2)
    {
      log.error("Too few arguments");
    }
    else
    {
      if(log.isDebugEnabled()) log.debug(Arrays.asList(fileNames));
      try
      {
        doConcatenate(fileNames);
      }
      catch(IOException exc)
      {
        log.error(exc.getMessage(), exc);
      }
      catch(DocumentException exc)
      {
        log.error(exc.getMessage(), exc);
      }
    }

    log.debug("concatenate(Object[]) - end");
  }

  @SuppressWarnings("unchecked")
  private void doConcatenate(String[] fileNames)
      throws IOException, DocumentException
  {
    int pageOffset = 0;
    List<Object> master = new ArrayList<Object>();
    final int argLen = fileNames.length - 1;
    String outFile = fileNames[argLen];
    Document document = null;
    PdfCopy writer = null;
    
    for(int fileNumber = 0; fileNumber < argLen; fileNumber++)
    {
      // we create a reader for a certain document
      log.debug(fileNames[fileNumber]);
      PdfReader reader = new PdfReader(fileNames[fileNumber]);
      reader.consolidateNamedDestinations();
      // we retrieve the total number of pages
      int numberOfPages = reader.getNumberOfPages();
      
      List<Object> bookmarks = SimpleBookmark.getBookmark(reader);
      if(bookmarks != null)
      {
        if(pageOffset != 0)
        {
          SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
        }
        master.addAll(bookmarks);
      }
      pageOffset += numberOfPages;

      if(fileNumber == 0)
      {
        // step 1: creation of a document-object
        document = new Document(reader.getPageSizeWithRotation(1));
        // step 2: we create a writer that listens to the
        // document
        writer = new PdfCopy(document, new FileOutputStream(outFile));
        // step 3: we open the document
        document.open();
      }
      
      // step 4: we add content
      for(int pageNumber = 1; pageNumber <= numberOfPages; pageNumber++)
        writer.addPage(writer.getImportedPage(reader, pageNumber));
      if(reader.getAcroForm() != null) writer.copyAcroForm(reader);
    }
    
    if(!master.isEmpty()) writer.setOutlines(master);
    
    // step 5: we close the document
    document.close();
  }
}
