/*
 * Copyright 2009 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.hornetq.javaee.example;

import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
public class MDBJMSContextClientExample
{
   public static void main(final String[] args) throws Exception
   {
      JMSContext context = null;
      InitialContext initialContext = null;
      try
      {
         // Step 1. Create an initial context to perform the JNDI lookup.
         final Properties env = new Properties();
         env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
         env.put(Context.PROVIDER_URL, "remote://localhost:4447");
         env.put(Context.SECURITY_PRINCIPAL, "guest");
         env.put(Context.SECURITY_CREDENTIALS, "password");
         initialContext = new InitialContext(env);

         // Step 2. Perfom a lookup on the queue
         Queue queue = (Queue)initialContext.lookup("jms/queues/testQueue");

         // Step 3. Perform a lookup on the Connection Factory
         ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("jms/RemoteConnectionFactory");

         // Step 4.Create a JMSContext
         context = cf.createContext("guest", "password", Session.AUTO_ACKNOWLEDGE);

         // Step 5. Create a temporary queue to receive the replies
         TemporaryQueue replyTo = context.createTemporaryQueue();

         // Step 6. Send 10 messages
         for (int i = 0; i < 10; i++)
         {
            String text = "This is a text message " + i;
            context.createProducer()
                  .setJMSReplyTo(replyTo)
                  .send(queue, text);
            System.out.println("Sent message: " + text);
         }

         // Step 7, 8 and 9 in MDB_JMS_CONTEXTExample

         // Step 10. Create a JMSConsumer to receive the replies
         JMSConsumer consumer = context.createConsumer(replyTo);

         // Step 11. Receive all the replies
         for (int i = 0; i < 10; i++)
         {
            String text = consumer.receiveBody(String.class, 1000);
            System.out.println("Received reply: " + text);
         }
      }
      finally
      {
         // Step 12. Be sure to close our JMS resources!
         if (initialContext != null)
         {
            initialContext.close();
         }
         if (context != null)
         {
            context.close();
         }
      }
   }
}
