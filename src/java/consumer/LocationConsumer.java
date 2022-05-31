package consumer;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
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
   private Logger logger;

   public LocationConsumer() {
      logger = Logger.getLogger(getClass().getName());
   }

   @Override
   public void onMessage(Message message){
       System.out.println("Consume message: " + message);
        BufferedWriter bufferWriter = null;
        try {
            if ((message instanceof TextMessage)) {
                try {
                    ServerSocket serverSocket = new ServerSocket(6543);
                    Socket socket = serverSocket.accept();
                    String clientIp = socket.getLocalAddress().getHostAddress();
                    int clientPort = socket.getPort();
                    logger.info("Server accept, client_ip: " + clientIp + ", port: " + clientPort); 
                     
                    TextMessage textMessage = (TextMessage) message;
                    logger.info("Consumer received text message: " + textMessage.getText());

                    bufferWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    logger.info("Send all clients, message: " + textMessage.getText());

                    bufferWriter.write(textMessage.getText());
                    bufferWriter.newLine();
                    bufferWriter.flush();
                     
                } catch (IOException e) {
                    logger.warning("IOException: " + e);
                }
            }
        } catch (Exception e) {
           logger.warning("Exception with incoming message: " + e);
        } finally {
            if (bufferWriter != null) {
                try {
                    bufferWriter.close();
                } catch (IOException ex) {
                    Logger.getLogger(LocationConsumer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
   }
}