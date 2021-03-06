package camera;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.Scanner;

import nosqlconsumer.Consumer;
import threadflag.ThreadFlag;
import vehicle.Vehicle;
import vehicle.VehicleGenerator;
import messaging.WriteMessages;

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
 * Class that implements the Smart Speed Camera
 * @author Gregory Mususa (081587717)
 *
 */
public class SmartSpeedCamera implements Runnable {

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
	
	/**
	 * Nullary constructor
	 */
	public SmartSpeedCamera() {
		
	}
	
	/**
	 * Runs when Thread.start() is called
	 */
	public void run() {
		int threadSleepTime = 60000/VehicleGenerator.getVehicleRate();		
		
		try {
			while(ThreadFlag.isRunning()) {
				Thread.sleep(threadSleepTime);		
				Vehicle vehc = VehicleGenerator.getRandomVehicle();
				this.recordPassingVehicle(vehc.getPlate(), vehc.getType(), vehc.getSpeed());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts up the camera, and broadcasts to the rest of the system, camera's uid, street name, city, speed limit, startupTimestamp
	 */
	private final void startup() {
		this.io = 1;
		this.startupTimestamp = Calendar.getInstance().getTimeInMillis();
		this.broadcast();
	}
	
	/**
	 * Use to change camera's speed limit � causes camera to restart (issuing, a new timestamp)
	 * @param maxMPH new speed limit
	 */
	public final void changeSpeedLimit(Integer maxMPH) {
		this.speedLimitMPH = maxMPH;
		this.io = 0;
		this.startup();
	}
	
	/**
	 * Change street, and let the system know that street has been changed
	 * Useful when camera is mounted within police vehicle
	 * @param newStreetName is the name of the new street, where the camera is currently located
	 */
	public final void changeStreet(String newStreetName) {
		this.streetName = newStreetName;
		this.broadcast();
	}
	
	/**
	 * Change city, and let the system know that camera's location has been changed
	 * Useful when camera is mounted within police vehicle
	 * @param newCityName is the name of the new city, where the camera is currently located
	 */
	public final void changeCity(String newCityName) {
		this.city = newCityName;
		this.broadcast();
	}
	
	/**
	 * Record vehicle passing camera, and send these details to the Service Bus
	 * 
	 * If a Vehicle is going faster than the speed limit, recordSpeedingVehicle() will do something about it
	 * 
	 * @param vehiclePlate
	 * @param vehicleType
	 * @param speed
	 */
	public final void recordPassingVehicle(String vehiclePlate, String vehicleType, Integer speed) {
		//Create Service Bus Contract
		Configuration config = ServiceBusConfiguration.configureWithSASAuthentication("gregorym","RootManageSharedAccessKey","/RD1rhL/bNXefoNBZ6pbv97OhYNx9czsvO7J6eM/mFc=",".servicebus.windows.net");
		ServiceBusContract service = ServiceBusService.create(config);
		
		try {
			//Initialise Topic
			String topicName = "SmartSpeedCameras";
			TopicInfo topicInfo = WriteMessages.initializeTopic(topicName,service);
			
			//Initialise Subscriptions
			String subName = "CameraVehicleMonitor";
			SubscriptionInfo subInfo = WriteMessages.initializeSubscription(subName, topicInfo, service);
			
			//Set Rules
			RuleInfo ruleInfo = new RuleInfo("vehicleHasPassed");
			ruleInfo.withSqlExpressionFilter("vehicleHasPassed = 1");
			
			if(!(WriteMessages.ruleExists(ruleInfo.getName(),subInfo,topicInfo,service))) {
				CreateRuleResult ruleResult = service.createRule(topicName, subName, ruleInfo);
				service.deleteRule(topicName, subName, "$Default");
			}
			
			// Create message, passing a string message for the body
		    BrokeredMessage message = new BrokeredMessage(vehiclePlate + "," + vehicleType + "," + speed + "," + this.uniqueIdentifier.toString() + "," + this.streetName + "," + this.city + "," + this.speedLimitMPH.toString() + "," + this.startupTimestamp);
		    
		    // Set some additional custom app-specific property
		    message.setProperty("vehicleHasPassed", 1);
		    
		    // Send message to the topic
		    service.sendTopicMessage(topicName, message);
		    
		    this.recordSpeedingVehicle(vehiclePlate,vehicleType,speed,service);
		    
		} catch (TopicExistsException | ServiceException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Record vehicle, if it is Speeding � and notify system, by sending message to a Service Bus Subscription
	 * This makes use of the Subscriptions with Filters feature of Azure Service Bus � part 4
	 * @param vehiclePlate
	 * @param vehicleType
	 * @param speed
	 * @param service
	 */
	private final void recordSpeedingVehicle(String vehiclePlate, String vehicleType, Integer speed, ServiceBusContract service) {
		try {
			//Initialise Topic
			String topicName = "SmartSpeedCameras";
			TopicInfo topicInfo = WriteMessages.initializeTopic(topicName,service);
			
			//Initialise Subscriptions
			String subName = "SpeedingVehicles";
			SubscriptionInfo subInfo = WriteMessages.initializeSubscription(subName, topicInfo, service);
			
			//Set Rules
			RuleInfo ruleInfo = new RuleInfo("speed");
			ruleInfo.withSqlExpressionFilter("speed >= " + this.speedLimitMPH.toString());
			
			if(!(WriteMessages.ruleExists(ruleInfo.getName(),subInfo,topicInfo,service))) {
				CreateRuleResult ruleResult = service.createRule(topicName, subName, ruleInfo);
				service.deleteRule(topicName, subName, "$Default");
			}
			
			// Create message, passing a string message for the body
		    BrokeredMessage message = new BrokeredMessage(vehiclePlate + "," + vehicleType + "," + speed + "," + this.uniqueIdentifier.toString() + "," + this.streetName + "," + this.city + "," + this.speedLimitMPH.toString() + "," + this.startupTimestamp);
		    
		    // Set some additional custom app-specific property
		    message.setProperty("speed", speed.intValue());
		    
		    // Send message to the topic
		    service.sendTopicMessage(topicName, message);
		    
		} catch (TopicExistsException | ServiceException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Inform (by adding a message to the Azure Service Bus Topic) the rest of the system of its 
	 * Unique Identifier, Street Name, Town/City, Maximum speed limit for the area they monitor, 
	 * and the date and time (timestamp) the Smart Speed Camera started up
	 * 
	 * The message will be Comma Delimited (CSV) � uid,street,city,speedLimitMPH,startupTimestamp (example: 5430,Claremont Road,Newcastle,70)
	 */
	private final void broadcast() {
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
}
