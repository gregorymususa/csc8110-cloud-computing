package nosqlconsumer;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import threadflag.ThreadFlag;
import messaging.WriteMessages;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableQuery.QueryComparisons;
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
//Include the following imports to use queue APIs.
import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.queue.*;

import customservicebusexceptions.TopicExistsException;
import entities.CameraRegistrationEntity;
import entities.SpeederEntity;

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
	
	private ConcurrentLinkedQueue<String> speedingVehicleSubscriptionMessages = new ConcurrentLinkedQueue<String>();
	
	/**
	 * Prints Speeding Vehicles to Command Line Console
	 */
	public void printSpeedingVehicles() {
		
		String heading1 = "Plate number";
		String heading2 = "Vehicle type";
		String heading3 = "Vehicle Speed";
		String heading4 = "Camera UID";
		String heading5 = "Camera Street";
		String heading6 = "Camera City";
		String heading7 = "Speed Limit";
		String heading8 = "isPriority";
		System.out.printf("%-25s %-25s %-25s %-25s %-25s %-25s %-25s %-25s %n", heading1, heading2, heading3, heading4, heading5, heading6, heading7, heading8);
		System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
		
		while(!(speedingVehicleSubscriptionMessages.isEmpty())) {
			String speeder = speedingVehicleSubscriptionMessages.poll();
			String[] speeder_explode = speeder.split(",");
			System.out.printf("%-25s %-25s %-25s %-25s %-25s %-25s %-25s %-25s %n",speeder_explode[0],speeder_explode[1],speeder_explode[2],speeder_explode[3],speeder_explode[4],speeder_explode[5],speeder_explode[6],speeder_explode[7]);
		}
		System.out.println("\n---No more entries---\n");
	}

	/**
	 * This method is triggered during Thread.start()
	 */
	public void run() {
		try {
			while(ThreadFlag.isRunning()) {
				this.monitor();
				Thread.sleep(60000);//check subscription every 60 seconds
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts to monitor SpeedingVehicle subscription, for any Speeders
	 */
	private void monitor() {
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
						
						SpeederEntity speeder = new SpeederEntity(line_explode[3],line_explode[0]); 
						
						
						if(speed >= speedLimit) {
							speeder.setVehicleType(line_explode[1]);
							speeder.setVehicleSpeed(line_explode[2]);
							speeder.setCameraStreet(line_explode[4]);
							speeder.setCameraCity(line_explode[5]);
							speeder.setSpeedLimit(line_explode[6]);
							speeder.setPriorityStatus("PRIORITY");
							speedingVehicleSubscriptionMessages.offer(line_explode[0] + "," + line_explode[1] + "," + line_explode[2] + "," + line_explode[3] + "," + line_explode[4] + "," + line_explode[5] + "," + line_explode[6] + "," + "PRIORITY");
						}
						else {
							speeder.setVehicleType(line_explode[1]);
							speeder.setVehicleSpeed(line_explode[2]);
							speeder.setCameraStreet(line_explode[4]);
							speeder.setCameraCity(line_explode[5]);
							speeder.setSpeedLimit(line_explode[6]);
							speeder.setPriorityStatus("");
							speedingVehicleSubscriptionMessages.offer(line_explode[0] + "," + line_explode[1] + "," + line_explode[2] + "," + line_explode[3] + "," + line_explode[4] + "," + line_explode[5] + "," + line_explode[6] + "," + " ");
						}
						
						this.saveSpeedersToStorage("SpeedingVehicles", speeder);
						this.saveSpeedersToStolenCheckQueue(speeder);
					}
					scanner.close();
					service.deleteMessage(message);
				}
				else {
					break;
				}
			}
		} catch (TopicExistsException | ServiceException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Persists the Speeder to the Azure Table Storage
	 * This method is private, and is called from printSpeedingVehicle — the data is being persisted, as it is being shown on the console by the Police Monitor.
	 * @param tableName to persist to
	 * @param speeder to persit
	 */
	private void saveSpeedersToStorage(String tableName, SpeederEntity speeder) {
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
		   TableOperation insertSpeeder = TableOperation.insertOrReplace(speeder);
		   
		   // Submit the operation to the table service
		   cloudTable.execute(insertSpeeder);
		   
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Store speeders to the Service Bus Queue ("potentiallystolenvehicle" queue), for a "isVehicleStolen" check later by VehicleCheck (Part 5)
	 * @param speeder to check
	 */
	private void saveSpeedersToStolenCheckQueue(SpeederEntity speeder) {
		// Define the connection-string with your values.
		String storageConnectionString = 
			"DefaultEndpointsProtocol=http;" + 
			"AccountName=gregorymnosql;" + 
			"AccountKey=cj6cWnXwS8sHPPTvLKdXdUzN5aNfoZsu703DntYyrWQ4vPkCkdEaN4xfj0V1Z28IaCA/uYEfUBCnnpgVDu6Uzw==";
		try {
			// Retrieve storage account from connection-string.
		    CloudStorageAccount storageAccount;
			storageAccount = CloudStorageAccount.parse(storageConnectionString);
			
			// Create the queue client.
			CloudQueueClient queueClient = storageAccount.createCloudQueueClient();
			
			// Retrieve a reference to a queue.
			CloudQueue queue = queueClient.getQueueReference("potentiallystolenvehicle");
			
			// Create the queue if it doesn't already exist.
			queue.createIfNotExists();
			
			// Create a message and add it to the queue.
		    CloudQueueMessage message = new CloudQueueMessage(speeder.getRowKey() + "," + speeder.getVehicleType() + "," + speeder.getVehicleSpeed() + "," + speeder.getPartitionKey() + "," + speeder.getCameraStreet() + "," + speeder.getCameraCity() + "," + speeder.getSpeedLimit());
		    queue.addMessage(message);
		    
		} catch (InvalidKeyException | URISyntaxException | StorageException e) {
			e.printStackTrace();
		}

	   
	}

}
