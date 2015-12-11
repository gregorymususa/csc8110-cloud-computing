package camera;

import java.util.Calendar;

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
	private Integer speedLimitMPH;
	private int io;
	private long startupTimestamp;
	
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
		this.speedLimitMPH = maxMPH;
		this.io = 0;
		this.startup();
	}
	
	private void startup() {
		this.io = 1;
		this.startupTimestamp = Calendar.getInstance().getTimeInMillis();
		this.broadcast();
	}
	
	/**
	 * Use to change camera's speed limit — causes camera to restart (issuing, a new timestamp)
	 * @param maxMPH new speed limit
	 */
	public void changeSpeedLimit(Integer maxMPH) {
		this.speedLimitMPH = maxMPH;
		this.io = 0;
		this.startup();
	}
	
	/**
	 * Change street, and let the system know that street has been changed
	 * Useful when camera is mounted within police vehicle
	 * @param newStreetName is the name of the new street, where the camera is currently located
	 */
	public void changeStreet(String newStreetName) {
		this.streetName = newStreetName;
		this.broadcast();
	}
	
	/**
	 * Change city, and let the system know that camera's location has been changed
	 * Useful when camera is mounted within police vehicle
	 * @param newCityName is the name of the new city, where the camera is currently located
	 */
	public void changeCity(String newCityName) {
		//TODO
		this.city = newCityName;
		this.broadcast();
	}
	
	/**
	 * Inform (by adding a message to the Azure Service Bus Topic) the rest of the system of its 
	 * Unique Identifier, Street Name, Town/City, Maximum speed limit for the area they monitor, 
	 * and the date and time (timestamp) the Smart Speed Camera started up
	 * 
	 * The message will be Comma Delimited (CSV) — uid,street,city,speedLimitMPH,startupTimestamp (example: 5430,Claremont Road,Newcastle,70)
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
			ruleInfo.withSqlExpressionFilter("isCameraMessage = 1");
			
			if(!(WriteMessages.ruleExists(ruleInfo.getName(),subInfo,topicInfo,service))) {
				CreateRuleResult ruleResult = service.createRule(topicName, subName, ruleInfo);
				service.deleteRule(topicName, subName, "$Default");
			}
//			if(!(ruleInfo.getName().equalsIgnoreCase(service.getRule(topicInfo.getPath(), subInfo.getName(), ruleInfo.getName()).getValue().getName()))) {
//				CreateRuleResult ruleResult = service.createRule(topicName, subName, ruleInfo);
//				service.deleteRule(topicName, subName, "$Default");
//			}
			
			// Create message, passing a string message for the body
		    BrokeredMessage message = new BrokeredMessage(this.uniqueIdentifier.toString() + "," + this.streetName + "," + this.city + "," + this.speedLimitMPH.toString() + "," + this.startupTimestamp);
		    
		    // Set some additional custom app-specific property
		    message.setProperty("isCameraMessage", 1);
		    
		    // Send message to the topic
		    service.sendTopicMessage(topicName, message);
		    
		} catch (TopicExistsException | ServiceException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Method created, to test SmartSpeedCamera
	 * @param args
	 */
	public static void main(String[] args) {
		SmartSpeedCamera cam1 = new SmartSpeedCamera(5430, "Claremont Road", "Newcastle upon Tyne", 40);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cam1.changeSpeedLimit(20);
		cam1.changeStreet("Stepney Lane");
	}
}
