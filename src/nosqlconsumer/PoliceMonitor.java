package nosqlconsumer;

import java.io.InputStream;
import java.util.Scanner;

import threadflag.ThreadFlag;
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
		
		
	}
	
	public static void printSpeedingVehicles() {
		//Setup PEEK_LOCK versus ReceiveAndDelete model
		ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
		opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
					    
		//Create Service Bus Contract
		Configuration config = ServiceBusConfiguration.configureWithSASAuthentication("gregorym","RootManageSharedAccessKey","/RD1rhL/bNXefoNBZ6pbv97OhYNx9czsvO7J6eM/mFc=",".servicebus.windows.net");
		ServiceBusContract service = ServiceBusService.create(config);
				
		//Initialise Topic
		try {
			String topicName = "SmartSpeedCameras";
			TopicInfo topicInfo;
			topicInfo = WriteMessages.initializeTopic(topicName,service);
			
			
			//Initialise Subscriptions
			String subName = "SpeedingVehicles";
			SubscriptionInfo subInfo = WriteMessages.initializeSubscription(subName, topicInfo, service);
			
					
			while(true) {
				ReceiveSubscriptionMessageResult  resultSubMsg = service.receiveSubscriptionMessage(topicInfo.getPath(),subInfo.getName(),opts);
				BrokeredMessage message = resultSubMsg.getValue();
						
				if((message != null) && (message.getMessageId() != null)) {
					InputStream inputStream = message.getBody();
					
					Scanner scanner = new Scanner(inputStream);
					while(scanner.hasNextLine()) {
						String line = scanner.nextLine();
						String[] line_explode = line.split(",");
						
						Integer speed = Integer.parseInt(line_explode[2]);
						Double speedLimit = Integer.parseInt(line_explode[6]) * 1.1;
						
						if(speed >= speedLimit) {
							System.out.println(line + "\t PRIORITY");
						}
						else {
							System.out.println(line);
						}
					}
					scanner.close();
					service.deleteMessage(message);
				}
				else {
					System.out.print("\n");
					break;
				}
			}
		} catch (TopicExistsException | ServiceException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Test Police Monitor
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

}
