package org.hornetq.tests.integration.aerogear;


import org.hornetq.api.core.Message;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.SendAcknowledgementHandler;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.ConnectorServiceConfiguration;
import org.hornetq.core.config.CoreQueueConfiguration;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.integration.aerogear.AeroGearConnectorServiceFactory;
import org.hornetq.integration.aerogear.AeroGearConstants;
import org.hornetq.tests.util.ServiceTestBase;
import org.hornetq.tests.util.UnitTestCase;
import org.hornetq.utils.json.JSONArray;
import org.hornetq.utils.json.JSONException;
import org.hornetq.utils.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AeroGearBasicServerTest extends ServiceTestBase
{

   private HornetQServer server;
   private ServerLocator locator;
   private Server jetty;

   @Override
   @Before
   public void setUp() throws Exception
   {
      super.setUp();
      /*
      * there will be a thread kept alive by the http connection, we could disable the thread check but this means that the tests
      * interfere with one another, we just have to wait for it to be killed
      * */
      jetty = new Server();
      SelectChannelConnector connector0 = new SelectChannelConnector();
      connector0.setPort(8080);
      connector0.setMaxIdleTime(30000);
      connector0.setHost("localhost");
      jetty.addConnector(connector0);
      jetty.start();
      Configuration configuration = createDefaultConfig();
      HashMap<String, Object> params = new HashMap();
      params.put(AeroGearConstants.QUEUE_NAME, "testQueue");
      params.put(AeroGearConstants.ENDPOINT_NAME, "http://localhost:8080");
      params.put(AeroGearConstants.APPLICATION_ID_NAME, "9d646a12-e601-4452-9e05-efb0fccdfd08") ;
      params.put(AeroGearConstants.APPLICATION_MASTER_SECRET_NAME, "ed75f17e-cf3c-4c9b-a503-865d91d60d40");
      params.put(AeroGearConstants.RETRY_ATTEMPTS_NAME, 2);
      params.put(AeroGearConstants.RETRY_INTERVAL_NAME, 1);
      params.put(AeroGearConstants.BADGE_NAME, "99");
      params.put(AeroGearConstants.ALIASES_NAME, "me,him,them");
      params.put(AeroGearConstants.DEVICE_TYPE_NAME, "android,ipad");
      params.put(AeroGearConstants.SOUND_NAME, "sound1");
      params.put(AeroGearConstants.VARIANTS_NAME, "variant1,variant2");
      configuration.getConnectorServiceConfigurations().add(
            new ConnectorServiceConfiguration(AeroGearConnectorServiceFactory.class.getName(), params, "TestAeroGearService"));

      configuration.getQueueConfigurations().add(new CoreQueueConfiguration("testQueue", "testQueue", null, true));
      server = createServer(configuration);
      server.start();

   }

   @Override
   @After
   public void tearDown() throws Exception
   {
      if(jetty != null)
      {
         jetty.stop();
      }
      if(locator != null)
      {
         locator.close();
      }
      if(server != null)
      {
         server.stop();
      }
      super.tearDown();
   }

   @Test
   public void aerogearSimpleReceiveTest() throws Exception
   {
      CountDownLatch latch = new CountDownLatch(1);
      AeroGearHandler aeroGearHandler = new AeroGearHandler(latch);
      jetty.addHandler(aeroGearHandler);
      TransportConfiguration tpconf = new TransportConfiguration(UnitTestCase.INVM_CONNECTOR_FACTORY);
      locator = HornetQClient.createServerLocatorWithoutHA(tpconf);
      ClientSessionFactory sf = createSessionFactory(locator);
      ClientSession session = sf.createSession(false, true, true);
      ClientProducer producer = session.createProducer("testQueue");
      ClientMessage m = session.createMessage(true);
      m.putStringProperty(AeroGearConstants.AEROGEAR_ALERT.toString(), "hello from HornetQ!");
      m.putStringProperty("AEROGEAR_PROP1", "prop1");
      m.putBooleanProperty("AEROGEAR_PROP2", true);

      producer.send(m);

      assertTrue(latch.await(5, TimeUnit.SECONDS));
      assertNotNull(aeroGearHandler.jsonObject);
      JSONObject body = (JSONObject) aeroGearHandler.jsonObject.get("message");
      assertNotNull(body);
      String prop1 =  body.getString("AEROGEAR_PROP1");
      assertNotNull(prop1);
      assertEquals(prop1, "prop1");
      prop1 =  body.getString("AEROGEAR_PROP2");
      assertNotNull(prop1);
      assertEquals(prop1, "true");
      String alert = body.getString("alert");
      assertNotNull(alert);
      assertEquals(alert, "hello from HornetQ!");
      String sound =  body.getString("sound");
      assertNotNull(sound);
      assertEquals(sound, "sound1");
      String badge = body.getString("badge");
      assertNotNull(badge);
      assertEquals(badge, "99");
      Integer ttl = body.getInt("ttl");
      assertNotNull(ttl);
      assertEquals(ttl.intValue(), 3600);
      JSONArray jsonArray = (JSONArray) aeroGearHandler.jsonObject.get("variants");
      assertNotNull(jsonArray);
      assertEquals(jsonArray.getString(0), "variant1");
      assertEquals(jsonArray.getString(1), "variant2");
      jsonArray = (JSONArray) aeroGearHandler.jsonObject.get("alias");
      assertNotNull(jsonArray);
      assertEquals(jsonArray.getString(0), "me");
      assertEquals(jsonArray.getString(1), "him");
      assertEquals(jsonArray.getString(2), "them");
      jsonArray = (JSONArray) aeroGearHandler.jsonObject.get("deviceType");
      assertNotNull(jsonArray);
      assertEquals(jsonArray.getString(0), "android");
      assertEquals(jsonArray.getString(1), "ipad");
      latch = new CountDownLatch(1);
      aeroGearHandler.resetLatch(latch);

      //now override the properties
      m = session.createMessage(true);
      m.putStringProperty(AeroGearConstants.AEROGEAR_ALERT.toString(), "another hello from HornetQ!");
      m.putStringProperty(AeroGearConstants.AEROGEAR_BADGE.toString(), "111");
      m.putStringProperty(AeroGearConstants.AEROGEAR_SOUND.toString(), "s1");
      m.putIntProperty(AeroGearConstants.AEROGEAR_TTL.toString(), 10000);
      m.putStringProperty(AeroGearConstants.AEROGEAR_ALIASES.toString(), "alias1,alias2");
      m.putStringProperty(AeroGearConstants.AEROGEAR_DEVICE_TYPES.toString(), "dev1,dev2");
      m.putStringProperty(AeroGearConstants.AEROGEAR_VARIANTS.toString(), "v1,v2");

      producer.send(m);
      assertTrue(latch.await(5, TimeUnit.SECONDS));
      assertNotNull(aeroGearHandler.jsonObject);
      body = (JSONObject) aeroGearHandler.jsonObject.get("message");
      assertNotNull(body);
      alert = body.getString("alert");
      assertNotNull(alert);
      assertEquals(alert, "another hello from HornetQ!");
      sound =  body.getString("sound");
      assertNotNull(sound);
      assertEquals(sound, "s1");
      badge = body.getString("badge");
      assertNotNull(badge);
      assertEquals(badge, "111");
      ttl = body.getInt("ttl");
      assertNotNull(ttl);
      assertEquals(ttl.intValue(), 10000);
      jsonArray = (JSONArray) aeroGearHandler.jsonObject.get("variants");
      assertNotNull(jsonArray);
      assertEquals(jsonArray.getString(0), "v1");
      assertEquals(jsonArray.getString(1), "v2");
      jsonArray = (JSONArray) aeroGearHandler.jsonObject.get("alias");
      assertNotNull(jsonArray);
      assertEquals(jsonArray.getString(0), "alias1");
      assertEquals(jsonArray.getString(1), "alias2");
      jsonArray = (JSONArray) aeroGearHandler.jsonObject.get("deviceType");
      assertNotNull(jsonArray);
      assertEquals(jsonArray.getString(0), "dev1");
      assertEquals(jsonArray.getString(1), "dev2");
      session.start();
      ClientMessage message = session.createConsumer("testQueue").receiveImmediate();
      assertNull(message);
   }

   class AeroGearHandler extends AbstractHandler
   {
      JSONObject jsonObject;
      private CountDownLatch latch;

      public AeroGearHandler(CountDownLatch latch)
      {
         this.latch = latch;
      }

      @Override
      public void handle(String target, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, int i) throws IOException, ServletException
      {
         Request request = (Request) httpServletRequest;
         httpServletResponse.setContentType("text/html");
         httpServletResponse.setStatus(HttpServletResponse.SC_OK);
         request.setHandled(true);
         byte[] bytes = new byte[httpServletRequest.getContentLength()];
         httpServletRequest.getInputStream().read(bytes);
         String json = new String(bytes);
         try
         {
            jsonObject = new JSONObject(json);
         }
         catch (JSONException e)
         {
            jsonObject = null;
         }
         latch.countDown();
      }

      public void resetLatch(CountDownLatch latch)
      {
         this.latch = latch;
      }
   }

   @Test
   public void aerogearReconnectTest() throws Exception
   {
      jetty.stop();
      final CountDownLatch reconnectLatch = new CountDownLatch(1);
      jetty.addHandler(new AbstractHandler()
      {
         @Override
         public void handle(String target, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, int i) throws IOException, ServletException
         {
            Request request = (Request) httpServletRequest;
            httpServletResponse.setContentType("text/html");
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            request.setHandled(true);
            reconnectLatch.countDown();
         }

      });
      TransportConfiguration tpconf = new TransportConfiguration(UnitTestCase.INVM_CONNECTOR_FACTORY);
      locator = HornetQClient.createServerLocatorWithoutHA(tpconf);
      ClientSessionFactory sf = createSessionFactory(locator);
      ClientSession session = sf.createSession(false, true, true);
      ClientProducer producer = session.createProducer("testQueue");
      final CountDownLatch latch = new CountDownLatch(2);
      ClientMessage m = session.createMessage(true);
      m.putStringProperty(AeroGearConstants.AEROGEAR_ALERT.toString(), "hello from HornetQ!");

      producer.send(m, new SendAcknowledgementHandler()
      {
         @Override
         public void sendAcknowledged(Message message)
         {
            latch.countDown();
         }
      });
      m = session.createMessage(true);
      m.putStringProperty(AeroGearConstants.AEROGEAR_ALERT.toString(), "another hello from HornetQ!");

      producer.send(m, new SendAcknowledgementHandler()
      {
         @Override
         public void sendAcknowledged(Message message)
         {
            latch.countDown();
         }
      });
      latch.await(5, TimeUnit.SECONDS);
      Thread.sleep(1000);
      jetty.start();
      reconnectLatch.await(5, TimeUnit.SECONDS);
      session.start();
      ClientMessage message = session.createConsumer("testQueue").receiveImmediate();
      assertNull(message);
   }

   @Test
   public void aerogear401() throws Exception
   {
      final CountDownLatch latch = new CountDownLatch(1);
      jetty.addHandler(new AbstractHandler()
      {
         @Override
         public void handle(String target, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, int i) throws IOException, ServletException
         {
            Request request = (Request) httpServletRequest;
            httpServletResponse.setContentType("text/html");
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            request.setHandled(true);
            latch.countDown();
         }

      });
      TransportConfiguration tpconf = new TransportConfiguration(UnitTestCase.INVM_CONNECTOR_FACTORY);
      locator = HornetQClient.createServerLocatorWithoutHA(tpconf);
      ClientSessionFactory sf = createSessionFactory(locator);
      ClientSession session = sf.createSession(false, true, true);
      ClientProducer producer = session.createProducer("testQueue");
      ClientMessage m = session.createMessage(true);
      m.putStringProperty(AeroGearConstants.AEROGEAR_ALERT.toString(), "hello from HornetQ!");

      producer.send(m);
      m = session.createMessage(true);
      m.putStringProperty(AeroGearConstants.AEROGEAR_ALERT.toString(), "another hello from HornetQ!");

      producer.send(m);
      assertTrue(latch.await(5, TimeUnit.SECONDS));
      session.start();
      ClientConsumer consumer = session.createConsumer("testQueue");
      ClientMessage message = consumer.receive(5000);
      assertNotNull(message);
      message = consumer.receive(5000);
      assertNotNull(message);
   }


   @Test
   public void aerogear404() throws Exception
   {
      jetty.addHandler(new AbstractHandler()
      {
         @Override
         public void handle(String target, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, int i) throws IOException, ServletException
         {
            Request request = (Request) httpServletRequest;
            httpServletResponse.setContentType("text/html");
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setHandled(true);
         }

      });
      TransportConfiguration tpconf = new TransportConfiguration(UnitTestCase.INVM_CONNECTOR_FACTORY);
      locator = HornetQClient.createServerLocatorWithoutHA(tpconf);
      ClientSessionFactory sf = createSessionFactory(locator);
      ClientSession session = sf.createSession(false, true, true);
      ClientProducer producer = session.createProducer("testQueue");
      ClientMessage m = session.createMessage(true);
      m.putStringProperty(AeroGearConstants.AEROGEAR_ALERT.toString(), "hello from HornetQ!");

      producer.send(m);
      m = session.createMessage(true);
      m.putStringProperty(AeroGearConstants.AEROGEAR_ALERT.toString(), "another hello from HornetQ!");

      producer.send(m);
      session.start();
      ClientConsumer consumer = session.createConsumer("testQueue");
      ClientMessage message = consumer.receive(5000);
      assertNotNull(message);
      message = consumer.receive(5000);
      assertNotNull(message);
   }
}
