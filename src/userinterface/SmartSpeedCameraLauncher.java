package userinterface;

import java.util.Scanner;

import threadflag.ThreadFlag;
import vehicle.Vehicle;
import vehicle.VehicleGenerator;
import camera.SmartSpeedCamera;

/**
 * This class is created, to launch the Smart Speed Camera
 * @author Gregory Mususa (081587717)
 *
 */
public class SmartSpeedCameraLauncher {

	/**
	 * The main method that will get executed
	 * @param args
	 * args[0] Integer — uid is the Unique Identifier
	 * args[1] String  — street is the Street Name
	 * args[2] String  — city is the Town or City
	 * args[3] Integer — maxMPH is the maximum speed limit for monitored area
	 * args[4] Integer — Vehicle rate in Vehicles per minute
	 */
	public static void main(String[] args) {
		Integer uid = Integer.valueOf(args[0]);
		String street = args[1];
		String city = args[2];
		Integer speedLimit = Integer.valueOf(args[3]).intValue();//miles per hour
		
		VehicleGenerator.setVehicleRate(Integer.valueOf(args[4]).intValue());//vehicles per minute
		
		ThreadFlag.run();
		SmartSpeedCamera camera = new SmartSpeedCamera(uid,street,city,speedLimit);
		Thread cameraThread = new Thread(camera);
		cameraThread.start();
		
		//Accept user input
		String input = "";
		while(!("exit".equalsIgnoreCase(input))) {
			System.out.println("\n-----\nMenu Options" + 
				"\n" + "Enter \"speedLimit=50\" (replacing 50 with the desired number) to change the Speed Limit (perhaps due to bad weather, a school opening, etc.) - camera will be restarted" +
				"\n\n" + "Enter \"street=Street Name\" to change the Street name (to simulate a camera mounted inside police vehicle) - camera will not be restarted" +
				"\n\n" + "Enter \"city=City Name\" to change the City name (to simulate a camera mounted inside police vehicle) - camera will not be restarted" +
				"\n\n" + "Enter \"exit\" to shutdown Camera\n-----\n");
			
			Scanner scanner = new Scanner(System.in);
			
			if(scanner.hasNext()) {
				input = scanner.nextLine();
				String[] input_explode = input.split("=");
				
				if(input.contains("speedLimit")) {
					camera.changeSpeedLimit(Integer.valueOf(input_explode[1]));
					System.out.println("\n>>Speed limit set to: " + input_explode[1]);
				}
				else if(input.contains("street")) {
					camera.changeStreet(input_explode[1]);
					System.out.println("\n>>Street name set to: " + input_explode[1]);
				}
				else if(input.contains("city")) {
					camera.changeCity(input_explode[1]);
					System.out.println("\n>>City name set to: " + input_explode[1]);
				}
			}
		}
		ThreadFlag.stop();
	}
}
