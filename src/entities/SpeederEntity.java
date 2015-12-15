package entities;

import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * Entity to persist speeders found by nosqlconsumer.PoliceMonitor
 * 
 * Partition key was made camera unique identifies, so we can easily find the speeders caught by a given camera
 * Row key was made the vehicle plate
 * @author Gregory Mususa (081587717)
 *
 */
public class SpeederEntity extends TableServiceEntity {

	private String vehicleType;
	private String vehicleSpeed;
	private String cameraStreet;
	private String cameraCity;
	private String speedLimit;
	private String priorityStatus;
	
	public SpeederEntity(String camera_uid, String vehicle_plate) {
		this.partitionKey = camera_uid;
		this.rowKey = vehicle_plate;
	}
	
	public SpeederEntity() {
		
	}
	
	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
	}
	
	public void setVehicleSpeed(String vehicleSpeed) {
		this.vehicleSpeed = vehicleSpeed;
	}
	
	public void setCameraStreet(String cameraStreet) {
		this.cameraStreet = cameraStreet;
	}
	
	public void setCameraCity(String cameraCity) {
		this.cameraCity = cameraCity;
	}
	
	public void setSpeedLimit(String speedLimit) {
		this.speedLimit = speedLimit;
	}
	
	public void setPriorityStatus(String priorityStatus) {
		this.priorityStatus = priorityStatus;
	}
	
	public String getVehicleType() {
		return this.vehicleType;
	}

	public String getVehicleSpeed() {
		return this.vehicleSpeed;
	}

	public String getCameraStreet() {
		return this.cameraStreet;
	}

	public String getCameraCity() {
		return this.cameraCity;
	}

	public String getSpeedLimit() {
		return this.speedLimit;
	}

	public String getPriorityStatus() {
		return this.priorityStatus;
	}
}
