package nosqlconsumer;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import threadflag.ThreadFlag;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueueMessage;

/**
 * Part 5 implemented
 * @author Gregory Mususa (081587717)
 *
 */
public class VehicleCheck implements Runnable {

	private static ConcurrentHashMap<String,Boolean> checkedVehicles = new ConcurrentHashMap<String,Boolean>();
	
	public VehicleCheck() {
		
	}

	/**
	 * Executes when Thread.start() is executed
	 */
	public void run() {
		while(ThreadFlag.isRunning()) {
			VehicleCheck.getMsgsFromQueue();
		}
	}
	
	/**
	 * Checks if Vehicle is stolen
	 * @param vehicleRegistration is the number plate of the vehicle to be checked
	 * @return <code>true</code> if stolen, <code>false</code> if not
	 */
	public static boolean isVehicleStolen(String vehicleRegistration) {
	    try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
	    return (Math.random() < 0.95);
	}
	
	/**
	 * Returns messages from the Azure storage queue — "potentiallystolenvehicle" queue
	 */
	public static void getMsgsFromQueue() {
		try {
			// Define the connection-string with your values.
			String storageConnectionString = 
				"DefaultEndpointsProtocol=http;" + 
				"AccountName=gregorymnosql;" + 
				"AccountKey=cj6cWnXwS8sHPPTvLKdXdUzN5aNfoZsu703DntYyrWQ4vPkCkdEaN4xfj0V1Z28IaCA/uYEfUBCnnpgVDu6Uzw==";
			
			// Retrieve storage account from connection-string.
		    CloudStorageAccount storageAccount = 
		        CloudStorageAccount.parse(storageConnectionString);
	
		    // Create the queue client.
		    CloudQueueClient queueClient = storageAccount.createCloudQueueClient();
	
		    // Retrieve a reference to a queue.
		    CloudQueue queue = queueClient.getQueueReference("potentiallystolenvehicle");
	
		    while(true) {
		    	// Retrieve the first visible message in the queue.
			    CloudQueueMessage retrievedMessage = queue.retrieveMessage();
			    
		    	if (retrievedMessage != null) {
		    		String plate = retrievedMessage.getMessageContentAsString().split(",")[0];
			    
		    		checkedVehicles.put(plate, VehicleCheck.isVehicleStolen(plate));
		    		
		    		// Process the message in less than 30 seconds, and then delete the message.
			        queue.deleteMessage(retrievedMessage);
		    	}
		    	else {
		    		break;
		    	}
		    }
		} catch(StorageException | InvalidKeyException | URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Prints all vehicles and their "isVehicleStolen" status to the command line
	 * Example: (EB81 KIB	true) (plateNumber	isVehicleStolen)
	 */
	public static void printResults() {
		Iterator<String> keys = checkedVehicles.keySet().iterator();
		
		System.out.println("Plate \t isStolen");
		
		String heading1 = "Plate";
		String heading2 = "Heading";
		if(!(checkedVehicles.isEmpty())) {
			System.out.printf("%-20s %-20s %n", heading1, heading2);
			System.out.println("-----------------------------------------------------------------");
		}
		
		while(keys.hasNext()) {
			String key = keys.next();
//			System.out.println(key + "\t" + checkedVehicles.get(key));
			System.out.printf("%-20s %-20s %n", key, checkedVehicles.get(key));
		}
		
		checkedVehicles.clear();
	}

}
