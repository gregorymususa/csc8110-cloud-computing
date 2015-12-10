package primer;

import java.io.InputStream;
import java.util.Scanner;

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
 * Tests for the receipt of messages
 * @author Gregory Mususa (081587717)
 *
 */
public class ReceiveMessages {
//	public static void main(String[] args) {
//		//Setup PEEK_LOCK versus ReceiveAndDelete model
//		ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
//	    opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
//	    
//	    //Create Service Bus Contract
//	    Configuration config = ServiceBusConfiguration.configureWithSASAuthentication("gregorym","RootManageSharedAccessKey","/RD1rhL/bNXefoNBZ6pbv97OhYNx9czsvO7J6eM/mFc=",".servicebus.windows.net");
//	    ServiceBusContract service = ServiceBusService.create(config);
//	    
//	    
//		try {
//			//Initialise Topic
//			String topicName = "TestTopic";
//			TopicInfo topicInfo = WriteMessages.initializeTopic(topicName,service);
//			
//			//Initialise Subscriptions
//			String subName1 = "AllMessages";
//			String subName2 = "HighMessages";
//			String subName3 = "LowMessages";
//			SubscriptionInfo subInfo1 = WriteMessages.initializeSubscription(subName1, topicInfo, service);
//			SubscriptionInfo subInfo2 = WriteMessages.initializeSubscription(subName2, topicInfo, service);
//			SubscriptionInfo subInfo3 = WriteMessages.initializeSubscription(subName3, topicInfo, service);
//			
//			while(true) {
//				ReceiveSubscriptionMessageResult  resultSubMsg = service.receiveSubscriptionMessage(topicInfo.getPath(),subInfo3.getName(),opts);
//				BrokeredMessage message = resultSubMsg.getValue();
//				if((message != null) && (message.getMessageId() != null)) {
//					InputStream inputStream = message.getBody();
//					
//					Scanner scanner = new Scanner(inputStream);
//					while(scanner.hasNextLine()) {
//						System.out.println(scanner.nextLine());
//					}
//					scanner.close();
//					service.deleteMessage(message);
//				}
//				else {
//					break;
//				}
//			}
//		} catch (TopicExistsException | ServiceException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
//	}
	
	public static void main(String[] args) {
		//Setup PEEK_LOCK versus ReceiveAndDelete model
		ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
	    opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
	    
	    //Create Service Bus Contract
	    Configuration config = ServiceBusConfiguration.configureWithSASAuthentication("gregorym","RootManageSharedAccessKey","/RD1rhL/bNXefoNBZ6pbv97OhYNx9czsvO7J6eM/mFc=",".servicebus.windows.net");
	    ServiceBusContract service = ServiceBusService.create(config);
	    
	    
		try {
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
		} catch (TopicExistsException | ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
