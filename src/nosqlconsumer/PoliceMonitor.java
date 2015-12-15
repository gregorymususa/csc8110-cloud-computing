package nosqlconsumer;

import java.io.InputStream;
import java.util.Scanner;

import threadflag.ThreadFlag;
import messaging.WriteMessages;

import com.microsoft.azure.storage.CloudStorageAccount;
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

	@Override
	public void run() {
		ThreadFlag.setBusy();
		this.printSpeedingVehicles();
		ThreadFlag.unsetBusy();
	}
	
	/**
	 * Prints Speeding Vehicles to Command Line Console
	 */
	private void printSpeedingVehicles() {
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
			
			while(ThreadFlag.isRunning()) {
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
							System.out.printf("%-25s %-25s %-25s %-25s %-25s %-25s %-25s %-25s %n",line_explode[0],line_explode[1],line_explode[2],line_explode[3],line_explode[4],line_explode[5],line_explode[6],"PRIORITY");
						}
						else {
							speeder.setVehicleType(line_explode[1]);
							speeder.setVehicleSpeed(line_explode[2]);
							speeder.setCameraStreet(line_explode[4]);
							speeder.setCameraCity(line_explode[5]);
							speeder.setSpeedLimit(line_explode[6]);
							speeder.setPriorityStatus("");
							System.out.printf("%-25s %-25s %-25s %-25s %-25s %-25s %-25s %-25s %n",line_explode[0],line_explode[1],line_explode[2],line_explode[3],line_explode[4],line_explode[5],line_explode[6],"");
						}
						
						this.saveSpeedersToStorage("SpeedingVehicles", speeder);
					}
					scanner.close();
					service.deleteMessage(message);
				}
				else {
					System.out.print("\n---No more entries---\n");
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
		   
		   /*TRACE — PART 4*/
//		   for(String s :tableClient.listTables()) {
//			   // Define constants for filters.
//			   final String PARTITION_KEY = "PartitionKey";
//			   final String ROW_KEY = "RowKey";
//			   final String TIMESTAMP = "Timestamp";
//			   // Create a filter condition where the partition key is "Smith".
//			   String partitionFilter = TableQuery.generateFilterCondition(
//					   PARTITION_KEY, 
//					   QueryComparisons.EQUAL,
//					   "5431");
//			   
//			   // Specify a partition query, using "Smith" as the partition key filter.
//			   TableQuery<SpeederEntity> partitionQuery = TableQuery.from(SpeederEntity.class).where(partitionFilter);
//		
//			   // Loop through the results, displaying information about the entity.
//			   for (SpeederEntity entity : cloudTable.execute(partitionQuery)) {
//				   System.out.println("TRACE:\t" + entity.getPartitionKey() +
//						   "\t" + entity.getRowKey() + 
//						   "\t" + entity.getVehicleSpeed() +
//						   "\t" + entity.getSpeedLimit() +
//						   "\t" + entity.getVehicleType());
//			   }
//		   }
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

}
