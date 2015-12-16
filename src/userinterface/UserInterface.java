package userinterface;

import java.util.Scanner;

import camera.SmartSpeedCamera;
import nosqlconsumer.Consumer;
import nosqlconsumer.PoliceMonitor;
import nosqlconsumer.VehicleCheck;
import nosqlreader.StorageReader;
import threadflag.ThreadFlag;

/**
 * User Interface for the user to interact with the system
 * @author Gregory Mususa (081587717)
 *
 */
public class UserInterface {

	public static void main(String[] args) {
		ThreadFlag.run();

		Thread cameraThread = new Thread(new SmartSpeedCamera());
		Thread consumerThread = new Thread(new Consumer());
		Thread vehicleCheck = new Thread(new VehicleCheck());
		
		
		//Accept user input
		String input = "";
		while(!("exit".equalsIgnoreCase(input))) {
			Scanner scanner = new Scanner(System.in);
			
			System.out.println("Menu Options" + 
			"\n" + "Enter \"A\" to start Cameras" +
			"\n" + "Enter \"B\" to start NoSQL Consumers" +
			"\n" + "Enter \"C\" to start Vehicle Check" +
			"\n" + "Enter \"D\" to see all operating Cameras" +
			"\n" + "Enter \"E\" to see all Speeding Vehicles in the SpeedingVehicle subscription (on the Service Bus)" +
			"\n" + "Enter \"F\" to retrieve all Speeders considered PRIORITY, that the Police monitor persisted to the Azure Table Storage (table SpeedingVehicles)" +
			"\n" + "Enter \"G\" to check if speeding vehicles are stolen" +
			"\n" + "Enter \"exit\" to shutdown the program");
			
			if(scanner.hasNextLine()) {
				input = scanner.nextLine();
				if(("A".equalsIgnoreCase(input)) && (!(cameraThread.isAlive()))) {
					cameraThread.start();
				}
				else if(("B".equalsIgnoreCase(input)) && (!(consumerThread.isAlive()))) {
					consumerThread.start();
				}
				else if(("C".equalsIgnoreCase(input)) && (!(vehicleCheck.isAlive()))) {
					vehicleCheck.start();
				}
				else if(("D".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					ThreadFlag.setBusy();
					StorageReader.getAllOperatingCameras();
					ThreadFlag.unsetBusy();
				}
				else if(("E".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					Thread policeMonitorThread = new Thread(new PoliceMonitor());
					policeMonitorThread.start();
				}
				else if(("F".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					ThreadFlag.setBusy();
					StorageReader.getAllPrioritySpeeders();
					ThreadFlag.unsetBusy();
				}
				else if(("G".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					VehicleCheck.printResults();
				}
			}
		}
		ThreadFlag.stop();
	}

}
