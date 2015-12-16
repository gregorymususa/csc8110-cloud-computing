package nosqlreader;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableQuery.QueryComparisons;

import entities.CameraRegistrationEntity;
import entities.SpeederEntity;

/**
 * Query Application that reads from the Azure Table Storage (NoSQL reader)
 * @author Gregory Mususa 081587717
 */
public class StorageReader {

	/**
	 * Prints to command line console, all operating cameras.
	 * Complete with their unique identifiers, street, city, speed limit, and timestamp
	 */
	public static void getAllOperatingCameras() {
		// Define the connection-string with your values.
		String storageConnectionString = 
			"DefaultEndpointsProtocol=http;" + 
			"AccountName=gregorymnosql;" + 
			"AccountKey=cj6cWnXwS8sHPPTvLKdXdUzN5aNfoZsu703DntYyrWQ4vPkCkdEaN4xfj0V1Z28IaCA/uYEfUBCnnpgVDu6Uzw==";
		
		// Retrieve storage account from connection-string.
		CloudStorageAccount storageAccount;
		try {
			storageAccount = CloudStorageAccount.parse(storageConnectionString);
				
			// Create the table client
			CloudTableClient tableClient = storageAccount.createCloudTableClient();
					   
			// Get the cloud table
			CloudTable cloudTable = tableClient.getTableReference("cameraregistrations");
			
			TableQuery<CameraRegistrationEntity> partitionQuery = TableQuery.from(CameraRegistrationEntity.class);
			
			//Loop through the results, displaying information about the entity.
			String heading1 = "City";
			String heading2 = "Unique Identifier";
			String heading3 = "Street";
			String heading4 = "Speed Limit";
			String heading5 = "Timestamp";
			
			System.out.print("\n");
			System.out.printf("%-25s %-25s %-25s %-20s %-25s %n", heading1, heading2, heading3, heading4, heading5);
			System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
			
			for (CameraRegistrationEntity entity : cloudTable.execute(partitionQuery)) {
				System.out.printf("%-25s %-25s %-25s %-20s %-25s %n", entity.getPartitionKey(), entity.getRowKey(),  entity.getStreet(), entity.getSpeedLimit(), entity.getStartupTimestamp());
			}
			System.out.print("\n");
			
		} catch (InvalidKeyException | URISyntaxException | StorageException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Prints to command line console, all speeders, who are considered PRIORITY
	 */
	public static void getAllPrioritySpeeders() {
		// Define the connection-string with your values.
		String storageConnectionString = 
			"DefaultEndpointsProtocol=http;" + 
			"AccountName=gregorymnosql;" + 
			"AccountKey=cj6cWnXwS8sHPPTvLKdXdUzN5aNfoZsu703DntYyrWQ4vPkCkdEaN4xfj0V1Z28IaCA/uYEfUBCnnpgVDu6Uzw==";
				
		// Retrieve storage account from connection-string.
		CloudStorageAccount storageAccount;
		try {
			storageAccount = CloudStorageAccount.parse(storageConnectionString);
			
			// Create the table client
			CloudTableClient tableClient = storageAccount.createCloudTableClient();
			
			// Get the cloud table
			CloudTable cloudTable = tableClient.getTableReference("SpeedingVehicles");
			
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
			
			for(String s :tableClient.listTables()) {
				
				// Define constants for filters.
				final String PRIORITY_STATUS = "PriorityStatus";
				
				// Create a filter condition where the partition key is "Smith".
				String partitionFilter = TableQuery.generateFilterCondition(
						PRIORITY_STATUS, 
						QueryComparisons.EQUAL,
						"PRIORITY");
			   
				// Specify a partition query, using "Smith" as the partition key filter.
				TableQuery<SpeederEntity> partitionQuery = TableQuery.from(SpeederEntity.class).where(partitionFilter);
		
				// Loop through the results, displaying information about the entity.
				for (SpeederEntity entity : cloudTable.execute(partitionQuery)) {
					System.out.printf("%-25s %-25s %-25s %-25s %-25s %-25s %-25s %-25s %n", entity.getRowKey(), entity.getVehicleType(), entity.getVehicleSpeed(), entity.getPartitionKey(), entity.getCameraStreet(), entity.getCameraCity(), entity.getSpeedLimit(), entity.getPriorityStatus());
				}
			}
		} catch (InvalidKeyException | URISyntaxException | StorageException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Prints to screen a list of all vehicles, marked as stolen, by VehicleCheck
	 */
	public static void getAllStolenVehicles() {
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
	        
	        String selectSQL = "SELECT plate, isStolen FROM VehicleCheckResults WHERE isStolen = 'true';";
	        statement = connection.createStatement();
	        resultSet = statement.executeQuery(selectSQL);
	        
	        String heading1 = "Plate";
			String heading2 = "isStolen";
			System.out.printf("%-20s %-20s %n", heading1, heading2);
			System.out.println("-----------------------------------------------------------------");
			
	        while (resultSet.next()) {
	        	System.out.printf("%-20s %-20s %n", resultSet.getString(1), resultSet.getString(2));
	        }
	        
	        System.out.print("\n---No more entries---\n");
	        
	        // Close everything
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
