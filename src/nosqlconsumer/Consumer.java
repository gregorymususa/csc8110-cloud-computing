package nosqlconsumer;

import java.io.InputStream;
import java.util.Scanner;

import messaging.WriteMessages;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMode;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveSubscriptionMessageResult;
import com.microsoft.windowsazure.services.servicebus.models.SubscriptionInfo;
import com.microsoft.windowsazure.services.servicebus.models.TopicInfo;

import customservicebusexceptions.TopicExistsException;

/**
 * Implements part 2, NoSQL Consumer
 * Consumer is ran in Thread, so it can execute continuously
 * Reads every 30 seconds, to avoid overburdening the Service Bus, and accruing unnecessary costs
 * 
 * @author Gregory Mususa (081587717)
 *
 */
public class Consumer implements Runnable {

	public void run() {
		while(true) {
			//Setup PEEK_LOCK versus ReceiveAndDelete model
			ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
		    opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
		    
		    //Create Service Bus Contract
		    Configuration config = ServiceBusConfiguration.configureWithSASAuthentication("gregorym","RootManageSharedAccessKey","/RD1rhL/bNXefoNBZ6pbv97OhYNx9czsvO7J6eM/mFc=",".servicebus.windows.net");
		    ServiceBusContract service = ServiceBusService.create(config);
		    
		    try {
		    	readRegistration(service,opts);
				
				readSighting(service,opts);
				
				Thread.sleep(30000);//check message queue every 30 seconds
			}
		    catch (TopicExistsException | ServiceException | InterruptedException e) {
				System.err.println(e.getMessage());
			}
		    
		}
	}
	
	private void readRegistration(ServiceBusContract service, ReceiveMessageOptions opts) throws TopicExistsException, ServiceException {
		//Initialise Topic
		String topicName = "SmartSpeedCameras";
		TopicInfo topicInfo = WriteMessages.initializeTopic(topicName,service);
		
		//Initialise Subscriptions
		String subName = "CameraMessages";
		SubscriptionInfo subInfo = WriteMessages.initializeSubscription(subName, topicInfo, service);
		
		while(true) {
			ReceiveSubscriptionMessageResult  resultSubMsg = service.receiveSubscriptionMessage(topicInfo.getPath(),subInfo.getName(),opts);
			BrokeredMessage message = resultSubMsg.getValue();
			
			if((message != null) && (message.getMessageId() != null)) {
				InputStream inputStream = message.getBody();
				
				Scanner scanner = new Scanner(inputStream);
				while(scanner.hasNextLine()) {
					System.out.println(scanner.nextLine());
				}
				scanner.close();
				service.deleteMessage(message);
			}
			else {
				break;
			}
		}
	}
	
	private void readSighting(ServiceBusContract service, ReceiveMessageOptions opts) throws TopicExistsException, ServiceException {
		//Initialise Topic
		String topicName = "SmartSpeedCameras";
		TopicInfo topicInfo = WriteMessages.initializeTopic(topicName,service);
		
		//Initialise Subscriptions
		String subName = "CameraVehicleMonitor";
		SubscriptionInfo subInfo = WriteMessages.initializeSubscription(subName, topicInfo, service);
					
		while(true) {
			ReceiveSubscriptionMessageResult  resultSubMsg = service.receiveSubscriptionMessage(topicInfo.getPath(),subInfo.getName(),opts);
			BrokeredMessage message = resultSubMsg.getValue();
			
			if((message != null) && (message.getMessageId() != null)) {
				InputStream inputStream = message.getBody();
				
				Scanner scanner = new Scanner(inputStream);
				while(scanner.hasNextLine()) {
					System.out.println(scanner.nextLine());
				}
				scanner.close();
				service.deleteMessage(message);
			}
			else {
				break;
			}
		}
	}
}
