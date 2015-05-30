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

public class UdpReceiver implements Runnable {

	public static final String SERVER_IP = "127.0.0.1"; // 'Within' the
														// emulator!
	public static final int SERVER_PORT = 49001;
	public static final int STATE_RUNNING = 1;
	public static final int STATE_STOPPED = 0;

	private int state = 0;

	private Handler handler;
	private Message msg;

	private InetAddress serverAddr;
	private DatagramSocket socket;
	private byte[] buf;

	public UdpReceiver(Handler handler) {
		this.handler = handler;
		msg = new Message();
	}

	@Override
	public void run() {
		try {
			state = STATE_RUNNING;
			/* Retrieve the ServerName */
			serverAddr = InetAddress.getByName(SERVER_IP);
			// serverAddr = InetAddress.getByName(getLocalIpAddress());

			Log.d("UDP", "S: Connecting...");
			/* Create new UDP-Socket */
			socket = new DatagramSocket(SERVER_PORT);

			while (state == STATE_RUNNING) {
				/* buffer that get data */
				buf = new byte[2048];
				/*
				 * Prepare a UDP-Packet that can contain the data we want to
				 * receive
				 */
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				// Log.d("UDP", "S: Receiving...");
				socket.receive(packet);
				// Log.d("UDP", "Received " + new String(packet.getData())
				// + " from " + packet.getAddress().toString());

				// update UI
				Message msg = Message.obtain(handler);
				msg.obj = packet.getData();
				handler.sendMessage(msg);
			}

			if (state == STATE_STOPPED) {
				socket.close();
			}

		} catch (Exception e) {
			Log.e("UDP", "S: Error", e);
			msg.obj = "Error";

		}
	}

	public void setState(int threadState) {
		state = threadState;
	}

	public String getLocalIpAddress() {
		StringBuilder ipAdress = new StringBuilder();
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& !inetAddress.isLinkLocalAddress()
							&& inetAddress.isSiteLocalAddress()) {
						ipAdress.append(inetAddress.getHostAddress().toString()
								+ "\n");
					}

				}
			}
		} catch (SocketException ex) {
			Log.e("LOG_TAG", ex.toString());
		}
		return ipAdress.toString();
	}
}