package camera;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestingConnection {

	public static void main(String[] args) {
		try {
			
			InetAddress addr = InetAddress.getByName("gregorym.servicebus.windows.net");
			System.out.println(addr.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
