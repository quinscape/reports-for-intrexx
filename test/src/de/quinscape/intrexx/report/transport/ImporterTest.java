/*
 * (C) Copyright 2018 QuinScape GmbH
 * All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report.transport;

import java.io.*;

import org.easymock.classextension.*;
import org.hamcrest.core.*;
import org.junit.*;

import de.quinscape.intrexx.report.transport.jaxb.v1.*;

/**
 * <p>
 * ImporterTest.
 * </p>
 * 
 * DOKU: ImporterTest
 *
 * @author jlauterbach
 */
public class ImporterTest
{

  /**
   * Test method for
   * {@link de.quinscape.intrexx.report.transport.Importer#fileFromResource(java.io.File, de.quinscape.intrexx.report.transport.jaxb.v1.Resource)}
   * .
   */
  @Test
  public void fileFromResource()
    throws Exception
  {
    File result = fileFromResourceGetResultFile("internal\\files\\abc\\test1.pdf");
    Assert.assertThat(result.getCanonicalPath(), Is.is(System.getProperty("user.home") + File.separator + "test1.pdf"));
    File result2 = fileFromResourceGetResultFile("internal/files/abc/test2.pdf");
    Assert.assertThat(result2.getCanonicalPath(), Is.is(System.getProperty("user.home") + File.separator + "test2.pdf"));
    File result3 = fileFromResourceGetResultFile("/opt/intrexx/org/myportal/internal/files/abc/test3.pdf");
    Assert.assertThat(result3.getCanonicalPath(), Is.is(System.getProperty("user.home") + File.separator + "test3.pdf"));
    File result4 = fileFromResourceGetResultFile("test4.pdf");
    Assert.assertThat(result4.getCanonicalPath(), Is.is(System.getProperty("user.home") + File.separator + "test4.pdf"));
  }

  private File fileFromResourceGetResultFile(String url)
  {
    Resource resource = EasyMock.createMock(Resource.class);
    EasyMock.expect(resource.getUrl()).andReturn(url).anyTimes();
    EasyMock.replay(resource);
    
    File unzipTmp = new File(System.getProperty("user.home"));
    File result = Importer.fileFromResource(unzipTmp, resource);
    return result;
  }

}
