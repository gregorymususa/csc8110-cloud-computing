package entities;

import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * When saving to NoSQL, we save all the vehicle and camera descriptions, because NoSQL does not have any joins
 * @author Gregory Mususa (081587717)
 *
 */
public class SightingEntity extends TableServiceEntity {

	private String vehicleSpeed;
	private String uid;
	private String street;
	private String city;
	private String speedLimit;
	private String timestamp;
	
	public SightingEntity(String vehicleType, String plateNumber) {
		this.partitionKey = vehicleType;
		this.rowKey = plateNumber;
	}
	
	public SightingEntity() {
		
	}
	
	public void setVehicleSpeed(String speed) {
		this.vehicleSpeed = speed;
	}
	
	public void setCameraUID(String uid) {
		this.uid = uid;
	}
	
	public void setStreet(String street) {
		this.street = street;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public void setSpeedLimit(String speedLimit) {
		this.speedLimit = speedLimit;
	}
	
	public void setStartupTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getVehicleSpeed() {
		return this.vehicleSpeed;
	}
	
	public String getCameraUID() {
		return this.uid;
	}
	
	public String getStreet() {
		return this.street;
	}
	
	public String getCity() {
		return this.city;
	}
	
	public String getSpeedLimit() {
		return this.speedLimit;
	}
	
	public String getStartupTimestamp() {
		return this.timestamp;
	}
}
