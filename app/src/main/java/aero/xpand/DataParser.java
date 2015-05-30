package aero.xpand;

public class DataParser {

	private byte[] data;
	public float speedIndicated;
	public float aoa, pitch, roll, headingMag;
	public float slip;
	public float positionAltAboveMeanSeaLevel;
	public float verticalSpeed;
	public float flapsHandle, flapsPosition;

	public DataParser() {

	}

	public void parseData() {
		int i = 5;
		int channel = 0;
		float[] values = new float[8];
		while (i < data.length && channel < 5) {
			int index = readInt(data[i++], data[i++], data[i++], data[i++]);
			resetFloatValues(values);
			for (int k = 0; k < 8; k++) {
				values[k] = readFloat(data[i++], data[i++], data[i++],
						data[i++]);
			}

			switch (index) {
			case 3:
				speedIndicated = values[1];
				break;

			case 13:
				flapsHandle = values[3];
				flapsPosition = values[4];
				break;

			case 17:
				roll = values[1];
				pitch = values[0];
				headingMag = values[3];
				break;

			case 18:
				aoa = values[0];
				slip = values[7];
				break;

			case 20:
				positionAltAboveMeanSeaLevel = values[2];
				break;

			default:
				break;
			}
			channel++;
		}

	}

	private void resetFloatValues(float[] values) {
		values[0] = 0;
		values[1] = 0;
		values[2] = 0;
		values[3] = 0;
		values[4] = 0;
		values[5] = 0;
		values[6] = 0;
		values[7] = 0;
	}

	protected int readInt(byte a, byte b, byte c, byte d) {
		return (((d & 0xff) << 24) | ((c & 0xff) << 16) | ((b & 0xff) << 8) | (a & 0xff));
	}

	protected float readFloat(byte a, byte b, byte c, byte d) {
		int bits = readInt(a, b, c, d);
		return Float.intBitsToFloat(bits);
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
