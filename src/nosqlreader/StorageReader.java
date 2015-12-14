package nosqlreader;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableQuery.QueryComparisons;

import entities.CameraRegistrationEntity;

/**
 * 
 * @author a8158771
 *
 */
public class StorageReader {

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
					   
			// Create the table if it doesn't exist
			CloudTable cloudTable = tableClient.getTableReference("cameraregistrations");
			
			TableQuery<CameraRegistrationEntity> partitionQuery = TableQuery.from(CameraRegistrationEntity.class);
			
//			String PARTITION_KEY = "PartitionKey";
//			String partitionFilter = TableQuery.generateFilterCondition(
//					   PARTITION_KEY, 
//					   QueryComparisons.EQUAL,
//					   "Newcastle upon Tyne");
//			TableQuery<CameraRegistrationEntity> partitionQuery = TableQuery.from(CameraRegistrationEntity.class).where(partitionFilter);
			
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
}
