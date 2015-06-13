package aero.xpand;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UdpReceiver implements Runnable {

	public static final int SERVER_PORT = 49020;
	public static final int STATE_RUNNING = 1;
	public static final int STATE_STOPPED = 0;

	private int state = 0;

	private Handler handler;
	private Message msg;

	private DatagramSocket socket;
	private byte[] buf;

	public UdpReceiver(Handler handler) {
		this.handler = handler;
		msg = new Message();

		buf = new byte[4096];
		try {
			socket = new DatagramSocket(SERVER_PORT);
			socket.setSoTimeout(1000);

		} catch (SocketException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		try {
			state = STATE_RUNNING;

			while (state == STATE_RUNNING) {
				 // Prepare a UDP-Packet that can contain the data we want to receive
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);

				//TODO: update the IP adress of destination for the UDP Sender class

				// update UI
				Message msg = Message.obtain(handler);
				msg.obj = packet.getData();
				handler.sendMessage(msg);
			}

			if (state == STATE_STOPPED) {
				socket.close();
			}

		} catch (SocketTimeoutException e){
			Message msg = Message.obtain(handler);
			msg.obj = "NO DATA RECEPTION";
			handler.sendMessage(msg);

		} catch (Exception e) {
			Message msg = Message.obtain(handler);
			msg.obj = "ERROR CONNECTING WITH X-PLANE";
			handler.sendMessage(msg);
		} finally {
			if (!socket.isClosed())
				socket.close();
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