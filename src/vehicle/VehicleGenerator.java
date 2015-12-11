package vehicle;

import java.util.Random;

/**
 * Implements a class, that randomly generates Vehicles
 * @author Gregory Mususa (081587717)
 *
 */
public class VehicleGenerator {

	/**
	 * Generate a random <code>Vehicle</code>
	 * @return <code>Vehicle</code> with randomly generated — number plate, vehicle type, and vehicle speed
	 */
	public static final Vehicle getRandomVehicle() {
		return new Vehicle(VehicleGenerator.generateNumberPlate(), VehicleGenerator.generateVehicleType(), VehicleGenerator.generateVehicleSpeed());
	}
	
	/**
	 * Generate a random number plate
	 * @return <code>String</code> representing the vehicle number plate
	 */
	private static final String generateNumberPlate() {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuilder plate = new StringBuilder();
		Random rnd = new Random();
		
		for(int i=0; i < 8; i+=1) {
			if((i >= 2) && (i <= 3)) {
				plate.append(rnd.nextInt(10));
			}
			else if(i == 4) {
				plate.append(" ");
			}
			else {
				plate.append(characters.charAt(rnd.nextInt(26)));
			}
		}
		return plate.toString();
	}
	
	/**
	 * Generate a random vehicle type, choosing among:
	 * Car, Truck, and Motorcycle
	 * @return <code>String</code> representing the vehicle type
	 */
	private static final String generateVehicleType() {
		String[] vehicleTypes = {"Car","Truck","Motorcycle"};
		Random rnd = new Random();
		return vehicleTypes[rnd.nextInt(3)];
	}
	
	/**
	 * Generate a vehicle speed between 0 – 100 Miles per hour
	 * @return <code>int</code> representing the vehicle speed
	 */
	private static final int generateVehicleSpeed() {
		return new Random().nextInt(101);
	}
}
