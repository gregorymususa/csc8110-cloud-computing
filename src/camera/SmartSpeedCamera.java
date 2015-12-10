package camera;

import primer.WriteMessages;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.CreateRuleResult;
import com.microsoft.windowsazure.services.servicebus.models.RuleInfo;
import com.microsoft.windowsazure.services.servicebus.models.SubscriptionInfo;
import com.microsoft.windowsazure.services.servicebus.models.TopicInfo;

import customservicebusexceptions.TopicExistsException;

/**
 * 
 * @author Gregory Mususa (081587717)
 *
 */
public class SmartSpeedCamera {

	private Integer uniqueIdentifier;
	private String streetName;
	private String city;
	private Integer areasMaxMPH;
	
	/**
	 * Constructor for SmartSpeedCamera class
	 * @param uid is the Unique Identifier
	 * @param street is the Street Name
	 * @param city is the Town or City
	 * @param maxMPH is the maximum speed limit for monitored area
	 */
	public SmartSpeedCamera(Integer uid, String street, String city, Integer maxMPH) {
		this.uniqueIdentifier = uid;
		this.streetName = street;
		this.city = city;
		this.areasMaxMPH = maxMPH;
	}
	
	/**
	 * Inform (by adding a message to the Azure Service Bus Topic) the rest of the system of its 
	 * Unique Identifier, Street Name, Town/City, Maximum speed limit for the area they monitor, 
	 * and the date and time (timestamp) the Smart Speed Camera started up
	 * 
	 * The message will be Comma Delimited (CSV) — uid,street,city,maxMPH (example: 5430,Claremont Road,Newcastle,70)
	 */
	public void broadcast() {
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
			
			//Set Rules
			RuleInfo ruleInfo = new RuleInfo("isCameraMessage");
			ruleInfo.withSqlExpressionFilter("isCameraMessage = true");
			
			if(!(WriteMessages.ruleExists(ruleInfo.getName(),subInfo,topicInfo,service))) {
				CreateRuleResult ruleResult = service.createRule(topicName, subName, ruleInfo);
				service.deleteRule(topicName, subName, "$Default");
			}
//			if(!(ruleInfo.getName().equalsIgnoreCase(service.getRule(topicInfo.getPath(), subInfo.getName(), ruleInfo.getName()).getValue().getName()))) {
//				CreateRuleResult ruleResult = service.createRule(topicName, subName, ruleInfo);
//				service.deleteRule(topicName, subName, "$Default");
//			}
			
			// Create message, passing a string message for the body
		    BrokeredMessage message = new BrokeredMessage(this.uniqueIdentifier + "," + this.streetName + "," + this.city + "," + this.areasMaxMPH);
		    
		    // Set some additional custom app-specific property
		    message.setProperty("isCameraMessage", "true");
		    
		    // Send message to the topic
		    service.sendTopicMessage(topicInfo.getPath(), message);
		    
		} catch (TopicExistsException | ServiceException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Method created, to test SmartSpeedCamera
	 * @param args
	 */
	public static void main(String[] args) {
		SmartSpeedCamera cam1 = new SmartSpeedCamera(5430, "Claremont Road", "Newcastle upon Tyne", 20);
		cam1.broadcast();
	}
}
