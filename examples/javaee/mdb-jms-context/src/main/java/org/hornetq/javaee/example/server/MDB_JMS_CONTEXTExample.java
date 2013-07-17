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
package org.hornetq.javaee.example.server;

import static javax.ejb.TransactionAttributeType.REQUIRED;
import static javax.ejb.TransactionManagementType.CONTAINER;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionManagement;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
@MessageDriven(
        name = "MDB_JMS_CONTEXT",
        activationConfig = {
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
                @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/testQueue")
        }
)
@TransactionManagement(value = CONTAINER)
@TransactionAttribute(value = REQUIRED)
public class MDB_JMS_CONTEXTExample implements MessageListener
{

   @Inject
   JMSContext context;

   public void onMessage(final Message message)
   {
      try
      {
         // Step 7. We know the client is sending a text message so we cast
         TextMessage textMessage = (TextMessage)message;

         // Step 8. get the text from the message.
         String text = textMessage.getText();

         System.out.println("Received message: " + text);
         // Step 9. send a reply to the message
         context.createProducer()
                 .setJMSCorrelationID(message.getJMSMessageID())
                 .send(textMessage.getJMSReplyTo(), "this is a reply to " + text);

      }
      catch (JMSException e)
      {
         e.printStackTrace();
      }
   }
}
