package userinterface;

import java.util.Scanner;

import camera.SmartSpeedCamera;
import nosqlconsumer.Consumer;
import nosqlconsumer.PoliceMonitor;
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
		
		
		//Accept user input
		String input = "";
		while(!("exit".equalsIgnoreCase(input))) {
			Scanner scanner = new Scanner(System.in);
			
			System.out.println("Menu Options" + 
			"\n" + "Enter \"A\" to start Cameras" +
			"\n" + "Enter \"B\" to start NoSQL Consumers" +
			"\n" + "Enter \"C\" to see all operating Cameras" +
			"\n" + "Enter \"D\" to see all Speeding Vehicles");
			
			if(scanner.hasNextLine()) {
				input = scanner.nextLine();
				if(("A".equalsIgnoreCase(input)) && (!(cameraThread.isAlive()))) {
					cameraThread.start();
				}
				else if(("B".equalsIgnoreCase(input)) && (!(consumerThread.isAlive()))) {
					consumerThread.start();
				}
				else if(("C".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					ThreadFlag.setBusy();
					StorageReader.getAllOperatingCameras();
					ThreadFlag.unsetBusy();
				}
				else if(("D".equalsIgnoreCase(input)) && (!(ThreadFlag.isBusy()))) {
					Thread policeMonitorThread = new Thread(new PoliceMonitor());
					policeMonitorThread.start();
				}
			}
		}
		ThreadFlag.stop();
	}

}
