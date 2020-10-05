/*
 * (C) Copyright 2020 QuinScape GmbH
 * All Rights Reserved.
 *
 * http://www.quinscape.de
 *
 * No part of this source code may be distributed in any form, be it altered
 * or unaltered, without the explicit written permission of QuinScape.
 */

package de.quinscape.intrexx.reports.fill;

import java.util.Iterator;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;

import de.uplanet.lucy.server.odata.consumer.cfg.IODataConsumerCfg;
import de.uplanet.lucy.server.odata.consumer.cfg.ODataConsumerRegistry;
import de.uplanet.lucy.server.odata.consumer.jersey.ODataConsumerFactory;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

/**
 * Eine {@link JRDataSource}, die ihre Daten aus einem Intrexx OData-Dienst zieht.
 *
 * @author Jasper Lauterbach
 */
public class ODataDataSource
    implements JRDataSource
{

  /**
   * Erzeugt eine {@link ODataDataSource} (als {@link JRDataSource}) anhand einer
   * OData-Servicebeschreibung und einem EntitySet.
   * 
   * @param serviceDescription
   *          Die Beschreibung des OData-Dienstes in Intrexx, in der Form
   *          <code>"Konfigurations-GUID/Service-GUID"</code>.
   * @param entitySet
   *          Der Name des EntitySets, das gelesen werden soll, um die Daten für den Bericht zu
   *          beschaffen.
   * @return Die erzeugte {@link ODataDataSource} als {@link JRDataSource}.
   */
  public static JRDataSource create(String serviceDescription, String entitySet)
  {
    String connectorGuid = serviceDescription.substring(0, serviceDescription.indexOf('/'));
    String serviceGuid = serviceDescription.substring(serviceDescription.indexOf('/') + 1);

    ODataDataSource source = new ODataDataSource();
    source.connect(connectorGuid, serviceGuid, entitySet);
    return source;
  }

  private Iterator<OEntity> entities;

  private OEntity currentEntity;

  /**
   * Verbindet sich zum konfigurierten OData-Anbieter als Administrator-Benutzer und liest dessen
   * EntitySet komplett ein.
   * 
   * @param configGuid
   *          Die GUID der OData-Anbieter-Konfiguration in Intrexx.
   * @param serviceGuid
   *          Die GUID des Dienstes des o. g. OData-Anbieters in Intrexx.
   * @param entitySet
   *          Das EntitySet (entspricht Tabellen-Name in Intrexx), das gelesen werden soll.
   */
  private void connect(String configGuid, String serviceGuid, String entitySet)
  {
    IODataConsumerCfg cfg =
        ODataConsumerRegistry.getInstance().getConsumerConfiguration(configGuid);
    ODataConsumer consumer = ODataConsumerFactory.INSTANCE.createConsumer(cfg, serviceGuid,
        "7312F993D0DA4CECCA9AE5A9D865BE142DE413EA");
    entities = consumer.getEntities(entitySet).execute().toList().iterator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.jasperreports.engine.JRDataSource#next()
   */
  @Override
  public boolean next()
      throws JRException
  {
    if(entities.hasNext())
    {
      currentEntity = entities.next();
      return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
   */
  @Override
  public Object getFieldValue(JRField jrField)
      throws JRException
  {
    return currentEntity.getProperty(jrField.getName()).getValue();
  }

}
