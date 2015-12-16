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
		
		PoliceMonitor policeMonitor = new PoliceMonitor();
		Thread policeMonitorThread = new Thread(policeMonitor);
		
		//Accept user input
		String input = "";
		while(!("exit".equalsIgnoreCase(input))) {
			Scanner scanner = new Scanner(System.in);
			
			System.out.println("\n-----\nMenu Options" + 
			"\n" + "Enter \"A\" to start Cameras" +
			"\n" + "Enter \"B\" to start NoSQL Consumer (part 2)" +
			"\n" + "Enter \"C\" to start Police Monitor (part 4)" +
			"\n" + "Enter \"D\" to start Vehicle Check (part 5)" +
			"\n" + "Enter \"E\" Query Application to print out, all operating Cameras (part 3)" +
			"\n" + "Enter \"F\" Query Application to print out, all Speeders considered PRIORITY, that the Police monitor persisted to the Azure Table Storage (table SpeedingVehicles) (part 4 task 4)" +
			"\n" + "Enter \"G\" Police Monitor to print out, all Speed violation sightings (retrieved from Service Bus Subscription \"SpeedingVehicles\") (part 4 task 1 and task 2)" +
			"\n" + "Enter \"H\" Vehicle Check, prints results of it checking if Speeding Vehicles are stolen (retrieved from Queue \"potentiallystolenvehicle\") (part 5)" +
			"\n" + "Enter \"I\" Query Application to print out, all stolen vehicles (and only stolen vehicles) (retrieved from SQL table \"VehicleCheckResults\") (part 6 task 2)" +
			"\n" + "Enter \"exit\" to shutdown the program (wait for 0 to 2 minutes, while the Consumer, Police Monitor, and Vehicle Check are shutdown safely\n-----\n");
			
			if(scanner.hasNextLine()) {
				input = scanner.nextLine();
				if(("A".equalsIgnoreCase(input)) && (!(cameraThread.isAlive()))) {
					cameraThread.start();
				}
				else if(("B".equalsIgnoreCase(input)) && (!(consumerThread.isAlive()))) {
					consumerThread.start();
				}
				else if(("C".equalsIgnoreCase(input)) && (!(policeMonitorThread.isAlive()))) {
					policeMonitorThread.start();
				}
				else if(("D".equalsIgnoreCase(input)) && (!(vehicleCheck.isAlive()))) {
					vehicleCheck.start();
				}
				else if(("E".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					ThreadFlag.setBusy();
					StorageReader.getAllOperatingCameras();
					ThreadFlag.unsetBusy();
				}
				else if(("F".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					ThreadFlag.setBusy();
					StorageReader.getAllPrioritySpeeders();
					ThreadFlag.unsetBusy();
				}
				else if(("G".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					ThreadFlag.setBusy();
					policeMonitor.printSpeedingVehicles();
					ThreadFlag.unsetBusy();
				}
				else if(("H".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					ThreadFlag.setBusy();
					VehicleCheck.printResults();
					ThreadFlag.unsetBusy();
				}
				else if(("I".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					ThreadFlag.setBusy();
					StorageReader.getAllStolenVehicles();
					ThreadFlag.unsetBusy();
				}
			}
		}
		ThreadFlag.stop();
	}

}
