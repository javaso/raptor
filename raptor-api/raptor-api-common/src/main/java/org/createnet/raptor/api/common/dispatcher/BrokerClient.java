/*
 * The MIT License
 *
 * Copyright 2017 FBK/CREATE-NET
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.createnet.raptor.api.common.dispatcher;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.models.configuration.DispatcherConfiguration;
/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class BrokerClient {
    
  Logger logger = LoggerFactory.getLogger(BrokerClient.class);
  
  private static MqttClient connection = null;
  private DispatcherConfiguration configuration;
    
  private final String clientName = "raptor";
  private final int connectionTimeout = 10;
  private final MemoryPersistence clientPersistence = new MemoryPersistence();
  
  private final int qos = 2;
  private final boolean retain = false;

  public void initialize(DispatcherConfiguration config) {
    configuration = config;  
  }

  protected synchronized void connect() throws MqttException {
    
    if(connection != null && connection.isConnected()) {
//      logger.debug("Mqtt connection available");
      return;
    }
    
//    logger.debug("Connecting to mqtt broker");
    
    try {

      String uri = configuration.getUri();
      String username = configuration.getUsername();
      String password = configuration.getPassword();
      
      logger.debug("Connecting to broker {}", uri);
      
      connection = new MqttClient(uri, clientName + System.currentTimeMillis(), clientPersistence);
      
      MqttConnectOptions connOpts = new MqttConnectOptions();
      
      connOpts.setCleanSession(true);
      connOpts.setConnectionTimeout(connectionTimeout);
      
      if(username != null && password != null) {
        connOpts.setUserName(username);
        connOpts.setPassword(password.toCharArray());
      }
      
      connection.connect(connOpts);
      
    } catch (MqttException me) {
      logger.error("Failed to connect to broker: {}", me.getMessage());
      throw me;
    }
  }
  
  public MqttClient getConnection() throws MqttException {
    connect();
    return connection;
  }

  public void sendMessage(String topic, String message)  {
    try {
      
      MqttClient conn = getConnection();
      
      if(conn == null || !conn.isConnected() ) {
        throw new DispatchException("Connection is not available");
      }
      
      getConnection().publish(topic, message.getBytes(), qos, retain);

    }
    catch(Exception e) {
      logger.error("Failed to send message. MQTT exception: {}", e.getMessage());
      throw new DispatchException(e);
    }
  }
  
  public void disconnect() {
    if(connection != null && connection.isConnected()) {
      try {
        connection.disconnect();
      } catch (MqttException ex) {
        logger.error("Cannot close connection properly: {}", ex.getMessage());
      }
    }
  }

  boolean isConnected() {
    return connection == null ? false : connection.isConnected();
  }
  
}
