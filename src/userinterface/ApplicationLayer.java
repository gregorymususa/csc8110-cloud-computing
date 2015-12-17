package userinterface;

import java.util.Scanner;

import camera.SmartSpeedCamera;
import nosqlconsumer.Consumer;
import nosqlconsumer.PoliceMonitor;
import nosqlconsumer.VehicleCheck;
import nosqlreader.StorageReader;
import threadflag.ThreadFlag;

/**
 * Automatically starts up NoSQLConsumer, Police Monitor, and Vehicle Check — when JAR is ran
 * 
 * This is to be used, to run these classes continuously — these will start running, when the VM is booted / and will cease running, when the VM is stopped
 * 
 * @author Gregory Mususa (081587717)
 *
 */
public class ApplicationLayer {

	public static void main(String[] args) {
		
		ThreadFlag.run();
		
		Thread consumerThread = new Thread(new Consumer());
		Thread policeMonitorThread = new Thread(new PoliceMonitor());
		Thread vehicleCheckThread = new Thread(new VehicleCheck());
		
		while(true) {			
			if (!(consumerThread.isAlive())) {
				consumerThread.start();
			}
			
			if (!(policeMonitorThread.isAlive())) {
				policeMonitorThread.start();
			}
			
			if(!(vehicleCheckThread.isAlive())) {
				vehicleCheckThread.start();
			}
		}
	}

}
