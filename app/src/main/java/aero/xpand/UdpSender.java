package aero.xpand;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UdpSender implements Runnable {

	public static final String SERVER_IP = "127.0.0.1"; // 'Within' the
														// emulator!
	public static final String TARGET_IP = "192.168.0.10";

	public static final int PORT = 49000;
	public static final int STATE_RUNNING = 1;
	public static final int STATE_STOPPED = 0;

	private int state = 0;

	private InetAddress address; // the adress we send the data to
	private DatagramSocket socket;
	public byte[] buffer;

	public UdpSender() {
	}

	@Override
	public void run() {
		try {
			state = STATE_RUNNING;
			// create socket
			socket = new DatagramSocket();
			// Retrieve the ServerName
			address = InetAddress.getByName(TARGET_IP);

			while (state == STATE_RUNNING) {
				// Prepare a UDP-Packet that contain the data we want to send
				if (buffer == null) {
					continue;
				}
				DatagramPacket packet = new DatagramPacket(buffer,
						buffer.length, address, PORT);
				socket.send(packet);
				
				// reset the buffer, so we don't send anything when not needed
				buffer = null;
			}

			if (state == STATE_STOPPED) {
				socket.close();
			}

		} catch (Exception e) {

		}
	}

	public void loadPacket(byte[] buff) {
		this.buffer = buffer;
	}
	
	public void setState(int threadState) {
		state = threadState;
	}
}