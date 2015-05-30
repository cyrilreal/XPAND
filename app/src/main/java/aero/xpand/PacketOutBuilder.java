package aero.xpand;

// build the packet to be sent to X-Plane via network
public class PacketOutBuilder {

	public static final byte[] PACKET_GEAR_DOWN = new byte[] { 68, 65, 84, 65, 48,
			13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	public static final byte[] PACKET_FLAPS_EXTEND = new byte[] { 68, 65, 84, 65, 48,
		13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
}
