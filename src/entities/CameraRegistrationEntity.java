package entities;

import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * Creates an entity to store the registration of Cameras
 * The following data will be stored — Integer uid, String street, String city, Integer maxMPH
 * @author Gregory Mususa (081587717)
 *
 */
public class CameraRegistrationEntity extends TableServiceEntity {
	
	private String street;
	private String maxMPH;
	private String timestamp;
	
	public CameraRegistrationEntity(String city, String uid) {
		this.partitionKey = city;
		this.rowKey = uid;
	}
	
	public CameraRegistrationEntity() {
		
	}
	
	public void setStreet(String street) {
		this.street = street;
	}
	
	public void setSpeedLimit(String maxMPH) {
		this.maxMPH = maxMPH;
	}
	
	public void setStartupTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getStreet() {
		return this.street;
	}
	
	public String getSpeedLimit() {
		return this.maxMPH;
	}
	
	public String getStartupTimestamp() {
		return this.timestamp;
	}
}
