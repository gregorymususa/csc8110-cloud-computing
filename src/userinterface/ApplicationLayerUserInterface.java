package userinterface;

import java.util.Scanner;

import nosqlconsumer.PoliceMonitor;
import nosqlconsumer.VehicleCheck;
import nosqlreader.StorageReader;
import threadflag.ThreadFlag;

/**
 * User Interface for the user to interact with the system, on the Application Layer (on the Microsoft Azure Cloud)
 * This acts as the Command Centre:
 *  
 * 1. Provides a way for a Law Enforcement agent, to come in and view:
 * 
 * 		a. All camera registrations
 * 
 * 		b. "Speeding Vehicles" Messages created by Police Monitor, and sent to the Service Bus (part 4, task 1 and 2) — 
 * 			aids "obtaining immediately the details of any sightings where a vehicle is travelling over the speed limit" (without having to wait for it to get persisted in storage first.
 * 
 * 		c. The history of all speeders, that were persisted to Azure Table Storage — this created by the Query Application, and only speeders marked as PRIORITY are printed out
 * 
 * 		d. "potentiallystolenvehicles" Queue Messages, that VehicleCheck stored on the Queue
 * 
 * 		e. The history of all vehicles marked as stolen — Query Application, queries the SQL database (WHERE isStolen = true) — returning entire history of stolen vehicles (and only stolen vehicles)
 * 
 * @author Gregory Mususa (081587717)
 *
 */
public class ApplicationLayerUserInterface {

	public static void main(String[] args) {
		//Accept user input
		String input = "";
		while(!("exit".equalsIgnoreCase(input))) {
			
			System.out.println("\n-----\nMenu Options" + 
			"\n" + "Enter \"A\" to get Query Application to print out, all Camera Registrations (part 3)" +
			"\n" + "Enter \"B\" to get Police Monitor to print out, all Speed violation sightings (retrieved from Service Bus Subscription \"SpeedingVehicles\") (part 4 task 1 and task 2)" +
			"\n" + "Enter \"C\" to get Query Application to print out, all Speeders considered PRIORITY, that the Police monitor persisted to the Azure Table Storage (table SpeedingVehicles) (part 4 task 4)" +
			"\n" + "Enter \"D\" to get Vehicle Check to print results, of it checking if Speeding Vehicles are stolen (retrieved from Queue \"potentiallystolenvehicle\") (part 5)" +
			"\n" + "Enter \"E\" to get Query Application to print out, all stolen vehicles (and only stolen vehicles) (retrieved from SQL table \"VehicleCheckResults\") (part 6 task 2)" +
			"\n" + "Enter \"exit\" to safely shutdown the Application Layer - User Interface program\n-----\n");
			
			Scanner scanner = new Scanner(System.in);
			
			if(scanner.hasNextLine()) {
				input = scanner.nextLine();
				if(("A".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					ThreadFlag.setBusy();
					StorageReader.getAllOperatingCameras();
					ThreadFlag.unsetBusy();
				}
				else if(("B".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					ThreadFlag.setBusy();
					PoliceMonitor policeMonitor = new PoliceMonitor();
					policeMonitor.printSpeedingVehicles();
					ThreadFlag.unsetBusy();
				}
				else if(("C".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					ThreadFlag.setBusy();
					StorageReader.getAllPrioritySpeeders();
					ThreadFlag.unsetBusy();
				}
				else if(("D".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					ThreadFlag.setBusy();
					VehicleCheck.printResults();
					ThreadFlag.unsetBusy();
				}
				else if(("E".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					ThreadFlag.setBusy();
					StorageReader.getAllStolenVehicles();
					ThreadFlag.unsetBusy();
				}
			}
		}
	}
}
