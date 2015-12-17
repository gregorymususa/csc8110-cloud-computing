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

import java.sql.*;

import com.microsoft.sqlserver.jdbc.*;

/**
 * Part 5 implemented
 * @author Gregory Mususa (081587717)
 *
 */
public class VehicleCheck implements Runnable {
	
	public VehicleCheck() {
		
	}

	/**
	 * Executes when Thread.start() is executed
	 */
	public void run() {
		try {
			while(ThreadFlag.isRunning()) {
				VehicleCheck.getMsgsFromQueue();
				Thread.sleep(120000);//check queue again after 2 minutes, don't be wasteful with resources, let messages build up a little
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
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
		    		Boolean isStolen = VehicleCheck.isVehicleStolen(plate);
		    		
		    		VehicleCheck.saveToSQL(plate, isStolen.toString());
		    		
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
	
		    String heading1 = "Plate";
			String heading2 = "isStolen";
			System.out.printf("%-20s %-20s %n", heading1, heading2);
			System.out.println("-----------------------------------------------------------------");
				
		    while(true) {
		    	// Retrieve the first visible message in the queue.
			    CloudQueueMessage retrievedMessage = queue.retrieveMessage();
			    
		    	if (retrievedMessage != null) {
		    		String plate = retrievedMessage.getMessageContentAsString().split(",")[0];
		    		Boolean isStolen = VehicleCheck.isVehicleStolen(plate);
		    		
		    		System.out.printf("%-20s %-20s %n", plate, isStolen.toString());
		    		VehicleCheck.saveToSQL(plate, isStolen.toString());
		    		
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
	 * Persit results of VehicleCheck, to a Relational database
	 * @param plate number to be persisted
	 * @param isStolen status to be persisted
	 */
	private static void saveToSQL(String plate, String isStolen) {
		try {
			String connectionString =
	            "jdbc:sqlserver://gregorymsql.database.windows.net:1433;"
	            + "database=gregorymsql;"
	            + "user=gregorym@gregorymsql;"
	            + "password=MiN4wew77uu;"
	            + "encrypt=true;"
	            + "trustServerCertificate=false;"
	            + "hostNameInCertificate=*.database.windows.net;"
	            + "loginTimeout=30;";
	
	        // Declare the JDBC objects.
	        Connection connection = DriverManager.getConnection(connectionString);
	        Statement statement = null;
	        ResultSet resultSet = null;
	        PreparedStatement createTablePrepStat = null;
	        PreparedStatement insertToTablePrepStat = null;
	        
	        connection.setAutoCommit(true);
	        
	        /* *********************************** */
	        /* CREATE THE TABLE — RUN ONCE (START) */
	        /* *********************************** */
//	        String createTableSQL = 
//	        	"CREATE TABLE VehicleCheckResults" +
//				"(" +
//	        	"id INT PRIMARY KEY IDENTITY(1,1)," +
//				"plate VARCHAR(12)," +
//				"isStolen  VARCHAR(12)" +
//				");";
//	        
//	        createTablePrepStat = connection.prepareStatement(createTableSQL);
//	        createTablePrepStat.execute();
	        /* *********************************** */
	        /* CREATE THE TABLE — RUN ONCE (END) */
	        /* *********************************** */
	        
	        String insertToTableSQL = 
	        	"INSERT INTO VehicleCheckResults(plate, isStolen) VALUES" +
				"('" + plate + "','" + isStolen + "');";
		        
	        insertToTablePrepStat = connection.prepareStatement(insertToTableSQL);
	        insertToTablePrepStat.execute();
	        
	        // Close everything
	        if(createTablePrepStat != null) {
	        	createTablePrepStat.close();
	        }
	        
	        if(insertToTablePrepStat != null) {
	        	insertToTablePrepStat.close();
	        }
	        
	        if(resultSet != null) {
	        	resultSet.close();
	        }
	        
	        if(statement != null) {
	        	statement.close();
	        }
	        
	        if(connection != null) {
	        	connection.close();
	        }
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
        
	}

}
