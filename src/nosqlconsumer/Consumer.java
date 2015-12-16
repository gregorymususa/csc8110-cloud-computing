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
import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.table.*;
import com.microsoft.azure.storage.table.TableQuery.*;

import customservicebusexceptions.TopicExistsException;
import entities.CameraRegistrationEntity;
import entities.SightingEntity;

/**
 * Implements part 2, NoSQL Consumer
 * Consumer is ran in Thread, so it can execute continuously
 * Reads every 30 seconds, to avoid overburdening the Service Bus, and accruing unnecessary costs
 * 
 * Reads messages from Service Bus — then prints them to command line or persits them to nosql storage
 * @author Gregory Mususa (081587717)
 *
 */
public class Consumer implements Runnable {

	/**
	 * The method that executes, when this class is started as a thread
	 */
	public void run() {
		while(ThreadFlag.isRunning()) {
			ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
		    opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
		    
		    //Create Service Bus Contract
		    Configuration config = ServiceBusConfiguration.configureWithSASAuthentication("gregorym","RootManageSharedAccessKey","/RD1rhL/bNXefoNBZ6pbv97OhYNx9czsvO7J6eM/mFc=",".servicebus.windows.net");
		    ServiceBusContract service = ServiceBusService.create(config);
		    
		    try {
		    	readRegistration(service,opts);
				
				readSighting(service,opts);
				
				Thread.sleep(30000);//check subscription every 30 seconds
			}
		    catch (TopicExistsException | ServiceException | InterruptedException e) {
				System.err.println(e.getMessage());
			}
		    
		}
	}
	
	private void saveRegistrationToStorage(String tableName, CameraRegistrationEntity cam) {
		try {
			// Define the connection-string with your values.
			String storageConnectionString = 
			    "DefaultEndpointsProtocol=http;" + 
			    "AccountName=gregorymnosql;" + 
			    "AccountKey=cj6cWnXwS8sHPPTvLKdXdUzN5aNfoZsu703DntYyrWQ4vPkCkdEaN4xfj0V1Z28IaCA/uYEfUBCnnpgVDu6Uzw==";
			
			// Retrieve storage account from connection-string.
		    CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
	
		   // Create the table client
		   CloudTableClient tableClient = storageAccount.createCloudTableClient();
		   
		   // Create the table if it doesn't exist
		   CloudTable cloudTable = tableClient.getTableReference(tableName);
		   cloudTable.createIfNotExists();
		   
		   // Create an operation to add the new customer to the people table
		   TableOperation insertCameraRegistration = TableOperation.insertOrReplace(cam);
		   
		   // Submit the operation to the table service
		   cloudTable.execute(insertCameraRegistration);
		   
		   /*TRACE — PART 3*/
//		   for(String s :tableClient.listTables()) {
//			   // Define constants for filters.
//			   final String PARTITION_KEY = "PartitionKey";
//			   final String ROW_KEY = "RowKey";
//			   final String TIMESTAMP = "Timestamp";
//			   // Create a filter condition where the partition key is "Smith".
//			   String partitionFilter = TableQuery.generateFilterCondition(
//					   PARTITION_KEY, 
//					   QueryComparisons.EQUAL,
//					   "Newcastle upon Tyne");
//			   
//			   // Specify a partition query, using "Smith" as the partition key filter.
//			   TableQuery<CameraRegistrationEntity> partitionQuery = TableQuery.from(CameraRegistrationEntity.class).where(partitionFilter);
//		
//			   // Loop through the results, displaying information about the entity.
//			   for (CameraRegistrationEntity entity : cloudTable.execute(partitionQuery)) {
//				   System.out.println(entity.getPartitionKey() +
//						   " " + entity.getRowKey() + 
//						   "\t" + entity.getStreet() +
//						   "\t" + entity.getSpeedLimit());
//			   }
//		   }
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	private void readRegistration(ServiceBusContract service, ReceiveMessageOptions opts) throws TopicExistsException, ServiceException {
		//Initialise Topic
		String topicName = "SmartSpeedCameras";
		TopicInfo topicInfo = WriteMessages.initializeTopic(topicName,service);
		
		//Initialise Subscriptions
		String subName = "CameraMessages";
		SubscriptionInfo subInfo = WriteMessages.initializeSubscription(subName, topicInfo, service);
		
		String tableName = "cameraregistrations";
		
		while(true) {
			ReceiveSubscriptionMessageResult  resultSubMsg = service.receiveSubscriptionMessage(topicInfo.getPath(),subInfo.getName(),opts);
			BrokeredMessage message = resultSubMsg.getValue();
			
			if((message != null) && (message.getMessageId() != null)) {
				InputStream inputStream = message.getBody();
				
				Scanner scanner = new Scanner(inputStream);
				while(scanner.hasNextLine()) {
					String camera = scanner.nextLine();
					String[] camera_explode = camera.split(",");
					
					CameraRegistrationEntity cam = new CameraRegistrationEntity(camera_explode[2],camera_explode[0]);
					cam.setStreet(camera_explode[1]);
					cam.setSpeedLimit(camera_explode[3]);
					cam.setStartupTimestamp(camera_explode[4]);
					
					this.saveRegistrationToStorage(tableName,cam);
				}
				scanner.close();
				service.deleteMessage(message);
			}
			else {
				break;
			}
		}
	}
	
	private void saveSightingToStorage(String tableName, SightingEntity sighting) {
		try {
			// Define the connection-string with your values.
			String storageConnectionString = 
			    "DefaultEndpointsProtocol=http;" + 
			    "AccountName=gregorymnosql;" + 
			    "AccountKey=cj6cWnXwS8sHPPTvLKdXdUzN5aNfoZsu703DntYyrWQ4vPkCkdEaN4xfj0V1Z28IaCA/uYEfUBCnnpgVDu6Uzw==";
			
			// Retrieve storage account from connection-string.
		    CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
	
		   // Create the table client
		   CloudTableClient tableClient = storageAccount.createCloudTableClient();
		   
		   // Create the table if it doesn't exist
		   CloudTable cloudTable = tableClient.getTableReference(tableName);
		   cloudTable.createIfNotExists();
		   
		   // Create an operation to add the new customer to the people table
		   TableOperation insertSighting = TableOperation.insertOrReplace(sighting);
		   
		   // Submit the operation to the table service
		   cloudTable.execute(insertSighting);
		   
		   /*TRACE — PART 3*/
//		   for(String s :tableClient.listTables()) {
//			   // Define constants for filters.
//			   final String PARTITION_KEY = "PartitionKey";
//			   final String ROW_KEY = "RowKey";
//			   final String TIMESTAMP = "Timestamp";
//			   // Create a filter condition where the partition key is "Smith".
//			   String partitionFilter = TableQuery.generateFilterCondition(
//					   PARTITION_KEY, 
//					   QueryComparisons.EQUAL,
//					   "Car");
//			   
//			   // Specify a partition query, using "Smith" as the partition key filter.
//			   TableQuery<SightingEntity> partitionQuery = TableQuery.from(SightingEntity.class).where(partitionFilter);
//		
//			   // Loop through the results, displaying information about the entity.
//			   for (SightingEntity entity : cloudTable.execute(partitionQuery)) {
//				   System.out.println(entity.getPartitionKey() +
//						   " " + entity.getRowKey() + 
//						   "\t" + entity.getVehicleSpeed() +
//						   "\t" + entity.getSpeedLimit() +
//						   "\t" + entity.getCameraUID());
//			   }
//		   }
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	private void readSighting(ServiceBusContract service, ReceiveMessageOptions opts) throws TopicExistsException, ServiceException {
		//Initialise Topic
		String topicName = "SmartSpeedCameras";
		TopicInfo topicInfo = WriteMessages.initializeTopic(topicName,service);
		
		//Initialise Subscriptions
		String subName = "CameraVehicleMonitor";
		SubscriptionInfo subInfo = WriteMessages.initializeSubscription(subName, topicInfo, service);
		
		String tableName = "vehiclesightings";
					
		while(true) {
			ReceiveSubscriptionMessageResult  resultSubMsg = service.receiveSubscriptionMessage(topicInfo.getPath(),subInfo.getName(),opts);
			BrokeredMessage message = resultSubMsg.getValue();
			
			if((message != null) && (message.getMessageId() != null)) {
				InputStream inputStream = message.getBody();
				
				Scanner scanner = new Scanner(inputStream);
				while(scanner.hasNextLine()) {
					String sighting = scanner.nextLine();
					String[] sighting_explode = sighting.split(",");
					
					SightingEntity sightingEntity = new SightingEntity(sighting_explode[1],sighting_explode[0]);
					sightingEntity.setVehicleSpeed(sighting_explode[2]);
					sightingEntity.setCameraUID(sighting_explode[3]);
					sightingEntity.setStreet(sighting_explode[4]);
					sightingEntity.setCity(sighting_explode[5]);
					sightingEntity.setSpeedLimit(sighting_explode[6]);
					sightingEntity.setStartupTimestamp(sighting_explode[7]);
					
					this.saveSightingToStorage(tableName, sightingEntity);
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
