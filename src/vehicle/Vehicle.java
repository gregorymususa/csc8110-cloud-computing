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
	
	public Vehicle(String plate, String type, Integer speed) {
		this.plate = plate;
		this.type = type;
		this.speed = speed;
	}
	
	public String getPlate() {
		return this.plate;
	}
	
	public String getType() {
		return this.type;
	}
	
	public Integer getSpeed() {
		return this.speed;
	}
}
