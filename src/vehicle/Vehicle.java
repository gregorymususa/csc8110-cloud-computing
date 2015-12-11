package vehicle;

/**
 * Class that implements a Vehicle
 * plate — Vehicle's number plate
 * type — Vehicle type (Car, Truck, Motorcycle)
 * speed — Vehicle's speed in Miles per hour. 
 * 
 * @author Gregory Mususa (081587717)
 * 
 */
public class Vehicle {

	private String plate;
	private String type;
	private Integer speed;
	
	/**
	 * Constructor for Vehicle class
	 * @param plate number of Vehicle
	 * @param type of Vehicle (Car, Truck, or Motorcycle)
	 * @param speed of Vehicle
	 */
	public Vehicle(String plate, String type, Integer speed) {
		this.plate = plate;
		this.type = type;
		this.speed = speed;
	}
	
	/**
	 * TODO documentation
	 * @return
	 */
	public String getPlate() {
		return this.plate;
	}
	
	/**
	 * TODO documentation
	 * @return
	 */
	public String getType() {
		return this.type;
	}
	
	/**
	 * TODO documentation
	 * @return
	 */
	public Integer getSpeed() {
		return this.speed;
	}
	
	/**
	 * Overrides toString method
	 * @return a String representation of the Vehicle object
	 */
	public String toString() {
		String vehicleDesc = plate + "," + type + "," + speed;
		return vehicleDesc;
	}
}
