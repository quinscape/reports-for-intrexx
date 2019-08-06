/*
 * (C) Copyright 2014 QuinScape GmbH
 * All Rights Reserved.
 * 
 * http://www.quinscape.de
 * 
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.report;

import static de.quinscape.intrexx.report.ReportsServlet.*;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.*;

import org.junit.*;

import de.uplanet.lucy.server.connector.*;
import de.uplanet.lucy.server.connector.web.*;
import de.uplanet.lucy.server.engine.http.*;

import de.quinscape.intrexx.report.domain.*;

/**
 * Unittests für <code>ReportsServlet</code>. Keine Functiontests.
 * 
 * @author Jörg Gottschling
 */
public class ReportsServletTest
{

  /**
   * DOKU: XYZ in ReportsServletTest.java.
   */
  private static final String TEST_TEXT = "xyz";

  private ReportsServlet servlet;

  private ReportService service;

  private ReportContext context;

  private Exporter exporter;

  private IServerBridgeRequest request;

  /**
   * Erzeugt die Test-Objekte und Mocks.
   */
  @Before
  public void setUp()
      throws Exception
  {
    servlet = new ReportsServlet();
    service = createMock(ReportService.class);
    context = createMock(ReportContext.class);
    exporter = createMock(Exporter.class);
    // Ist schlecht zu mocken durch get(String, Object). :-(
    request = new ServerBridgeRequest();

    servlet.setReportService(service);
    servlet.init();

  }

  /**
   * Prüft ob {@link ReportsServlet#init()} eine Exception wirft (korrekt), wenn kein Service
   * konfiguriert wurde.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testInitThrowsExceptionWhenNoServiceConfigured()
  {
    servlet.setReportService(null);
    servlet.init();
  }

  /**
   * Test method for
   * {@link ReportsServlet#service(IWebProcessingContext, IServerBridgeRequest, IServerBridgeResponse)}
   * .
   */
  @Test
  public void testService()
  {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ReportsServlet#createOutputStream(ReportContext, Exporter)}
   * .
   */
  @Test
  public void testCreateOutputStream()
  {
    fail("Not yet implemented");
    // TODO Ist es überhaupt sinnvoll diese Methode zu testen?
  }

  /**
   * Test method for {@link ReportsServlet#contentType(Exporter)} .
   */
  @Test
  public void testContentType()
  {
    expect(exporter.getCharacterEncoding()).andReturn("UTF-64").anyTimes();
    expect(exporter.getContentType()).andReturn("application/xyz").anyTimes();
    replay(exporter);

    assertThat(servlet.contentType(exporter), is("application/xyz; charset=UTF-64"));

    verify(exporter);
  }

  /**
   * Test method for {@link ReportsServlet#disposition(ReportContext, Exporter, boolean)} .
   */
  @Test
  public void testDispositionWithRq_qsFileName()
  {
    expect(context.getRequest()).andReturn(request).anyTimes();
    expect(exporter.getFileExtension()).andReturn("rep").anyTimes();
    request.put(rq_qsFileName, "mein-bericht-123");

    replay(context, service, exporter);

    assertThat(servlet.disposition(context, exporter, false), is("inline; filename=mein-bericht-123.rep"));

    verify(context, service, exporter);
  }

  /**
   * Test method for {@link ReportsServlet#disposition(ReportContext, Exporter, boolean)} .
   */
  @Test
  public void testDispositionWithQsFileName()
  {
    expect(context.getRequest()).andReturn(request).anyTimes();
    expect(exporter.getFileExtension()).andReturn("ext").anyTimes();
    request.put(qsFileName, "dein-bericht-456");

    replay(context, service, exporter);

    assertThat(servlet.disposition(context, exporter, false), is("inline; filename=dein-bericht-456.ext"));

    verify(context, service, exporter);
  }

  /**
   * Test method for {@link ReportsServlet#disposition(ReportContext, Exporter, boolean)} .
   */
  @Test
  public void testDispositionWithDisplayName()
  {
    Report report = createMock(Report.class);
    
    expect(context.getRequest()).andReturn(request).anyTimes();
    expect(context.getReport()).andReturn(report).anyTimes();
    expect(exporter.getFileExtension()).andReturn("pdf").anyTimes();
    expect(report.getDisplayName()).andReturn("Der Bericht").anyTimes();

    replay(context, service, exporter, report);

    // Withespace wird durch Unterstriche ersetzt
    assertThat(servlet.disposition(context, exporter, false), is("inline; filename=Der_Bericht.pdf"));

    verify(context, service, exporter, report);
  }

  /**
   * Test method for {@link ReportsServlet#disposition(ReportContext, Exporter, boolean)} .
   */
  @Test
  public void testDispositionWithNothing()
  {
    Report report = createNiceMock(Report.class);
    
    expect(context.getRequest()).andReturn(request).anyTimes();
    expect(context.getReport()).andReturn(report).anyTimes();
    expect(exporter.getFileExtension()).andReturn("pdf").anyTimes();

    replay(context, service, exporter, report);

    assertThat(servlet.disposition(context, exporter, false), is("inline; filename=report.pdf"));

    verify(context, service, exporter, report);
  }

  /**
   * Test method for {@link ReportsServlet#exportFormat(ReportContext)}.
   */
  @Test
  public void testExportFormatWithRq_qsExportFormat()
  {
    expect(context.getRequest()).andReturn(request).anyTimes();
    request.put(rq_qsExportFormat, TEST_TEXT);
    replay(context);

    assertThat(servlet.exportFormat(context), is(TEST_TEXT));

    verify(context);
  }

  /**
   * Test method for {@link ReportsServlet#exportFormat(ReportContext)}.
   */
  @Test
  public void testExportFormatWithQsExportFormat()
  {
    expect(context.getRequest()).andReturn(request).anyTimes();
    request.put(qsExportFormat, TEST_TEXT);
    replay(context);

    assertThat(servlet.exportFormat(context), is(TEST_TEXT));

    verify(context);
  }

  /**
   * Test method for {@link ReportsServlet#reportIdentifier(ReportContext)}.
   */
  @Test
  public void testReportIdentifierWithRq_qsReport()
  {
    expect(context.getRequest()).andReturn(request).anyTimes();
    replay(context);

    request.put(rq_qsReport, TEST_TEXT);
    assertThat(servlet.reportIdentifier(context), is(TEST_TEXT));

    verify(context);
  }

  /**
   * Test method for {@link ReportsServlet#reportIdentifier(ReportContext)}.
   */
  @Test
  public void testReportIdentifierWithQsReport()
  {
    expect(context.getRequest()).andReturn(request).anyTimes();
    replay(context);

    request.put(qsReport, TEST_TEXT);
    assertThat(servlet.reportIdentifier(context), is(TEST_TEXT));

    verify(context);
  }

  /**
   * Test für {@link ReportsServlet#afterExport(ReportContext, Exporter, OutputStream)}, welche
   * nichts tut.
   */
  @Test
  public void testAfterExport()
      throws Exception

  {
    OutputStream outputStream = createMock(OutputStream.class);
    replay(context, service, exporter, outputStream);

    // Es sollte nichts äußerlich messbares passieren.
    servlet.afterExport(context, exporter, null);

    verify(context, service, exporter, outputStream);
  }

}
