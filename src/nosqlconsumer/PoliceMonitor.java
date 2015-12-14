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
import entities.CameraRegistrationEntity;

/**
 * Reads messages from Service Bus — then prints them to command line or persits them to nosql storage
 * Message that are read pertain to vehicles that are going faster than the speed limit
 * 
 * Implements Part 4
 * 
 * @author Gregory Mususa (081587717)
 *
 */
public class PoliceMonitor implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	public void readSpeedingVehicles(ServiceBusContract service, ReceiveMessageOptions opts) throws TopicExistsException, ServiceException {
		//Initialise Topic
		String topicName = "SmartSpeedCameras";
		TopicInfo topicInfo = WriteMessages.initializeTopic(topicName,service);
				
		//Initialise Subscriptions
		String subName = "SpeedingVehicles";
		SubscriptionInfo subInfo = WriteMessages.initializeSubscription(subName, topicInfo, service);
				
//		String tableName = "cameraregistrations";
				
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
	
	/**
	 * Test Police Monitor
	 * @param args
	 */
	public static void main(String[] args) {
		PoliceMonitor monitor = new PoliceMonitor();
		
		//Setup PEEK_LOCK versus ReceiveAndDelete model
		ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
		opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
			    
		//Create Service Bus Contract
		Configuration config = ServiceBusConfiguration.configureWithSASAuthentication("gregorym","RootManageSharedAccessKey","/RD1rhL/bNXefoNBZ6pbv97OhYNx9czsvO7J6eM/mFc=",".servicebus.windows.net");
		ServiceBusContract service = ServiceBusService.create(config);
		
		try {
			monitor.readSpeedingVehicles(service, opts);
		} catch (TopicExistsException | ServiceException e) {
			System.out.println(e.getMessage());
		}
	}

}
