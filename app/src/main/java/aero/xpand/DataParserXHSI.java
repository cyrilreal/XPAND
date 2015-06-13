package aero.xpand;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class DataParserXHSI {

    private byte[] data;

    public float speedIndicated, speedTrue;
    public float aoa, pitch, roll, headingMag;
    public float slip;
    public float positionAltAboveMeanSeaLevel;
    public float verticalSpeed;
    public float flapsHandle, flapsPosition;

    public float devLocDot, devGlideDot;

    private float decodedValue;

    public DataParserXHSI() {

    }

    /**
     * packet decoding uses bits of code from XHSI project, thanks to them ;-)
     */
    public void parseData() {
        // these vars will be re-used several times, so define them here and not in a for-loop
        int data_point_id;
        // int int_data;
        float float_data;
        String string_data;

        // identify the packet type (identified by the first four bytes)
        String packet_type = new String(data, 0, 4).trim();

        if (packet_type.equals("ADCD") || packet_type.equals(("AVIO"))) {
            DataInputStream data_stream = new DataInputStream(new ByteArrayInputStream(data));
            try {
                data_stream.skipBytes(4);    // skip the bytes containing the packet type id
                int nb_of_data_points = data_stream.readInt();

                for (int i = 0; i < nb_of_data_points; i++) {
                    data_point_id = data_stream.readInt();
                    if (data_point_id >= 10000) {
                        // a string of 4 bytes
                        string_data = new String(data, 8 + (i * 8) + 4, 4).trim();
                        data_stream.skipBytes(4);
                        //this.xplane_data_repository.store_sim_string(data_point_id, string_data);
                    } else {
                        // Float
                        decodedValue = data_stream.readFloat();

                        switch (data_point_id) {
                            case 2:
                                headingMag = decodedValue;
                                break;

                            case 6:
                                roll = decodedValue;
                                break;

                            case 11:
                                pitch = decodedValue;
                                break;

                            case 14:
                                slip = decodedValue;
                                break;

                            case 52:
                                speedTrue = decodedValue;
                                break;

                            case 54:
                                positionAltAboveMeanSeaLevel = decodedValue;
                                break;

                            case 56:
                                verticalSpeed = decodedValue;
                                break;

                            case 118:
                                devLocDot = decodedValue;
                                break;

                            case 122:
                                devGlideDot = decodedValue;
                                break;

                            default:
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
