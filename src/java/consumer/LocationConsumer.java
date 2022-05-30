package consumer;

import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.ejb.MessageDrivenContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

@MessageDriven(activationConfig =
{
   @ActivationConfigProperty(propertyName = "clientId",
      propertyValue = "jms/locationQueue"),
   @ActivationConfigProperty(propertyName = "destinationLookup",
      propertyValue = "jms/locationQueue"),
   @ActivationConfigProperty(propertyName = "subscriptionDurability",
      propertyValue = "Durable"),
   @ActivationConfigProperty(propertyName = "subscriptionName",
      propertyValue = "jms/locationQueue"),
   @ActivationConfigProperty(propertyName = "destinationType",
      propertyValue = "jakarta.jms.Queue")
})
public class LocationConsumer implements MessageListener {
   // field obtained via dependency injection (not used here)
   @Resource
   private MessageDrivenContext mdc;
   private Logger logger;
   Socket socket = null;
   

   public LocationConsumer() {
      logger = Logger.getLogger(getClass().getName());
      init();
   }
   
   public void init() {
       try {
            ServerSocket serverSocket = new ServerSocket(6543);
            logger.info("MessageSendingServer start, waiting for all client open connection...");
            socket = serverSocket.accept();
            String clientIp = socket.getLocalAddress().getHostAddress();
            int clientPort = socket.getPort();
            logger.info("Server accept, client_ip: " + clientIp + ", port: " + clientPort);  
       } catch (IOException e) {
           
       }
   }

   @Override
   public void onMessage(Message message){
        BufferedReader bufferReader = null;
        BufferedWriter bufferWriter = null;
        try {
            if (message instanceof TextMessage) {
               String mess = ((TextMessage) message).getText();
               logger.info("Consumer received text message: " + mess);

               bufferWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
               logger.info("Send all clients, message: " + mess);

               bufferWriter.write(mess);
               bufferWriter.newLine();
               bufferWriter.flush();
            }
        } catch (JMSException e) {
           logger.warning("Exception with incoming message: " + e);
        } catch (Exception e) {
            logger.warning("Exception " + e);
        } finally {
            try {
                bufferReader.close();
                bufferWriter.close();
            } catch (IOException ex) {

            }
        }
   }
}