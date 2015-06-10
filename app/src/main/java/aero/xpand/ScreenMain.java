package aero.xpand;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

public class ScreenMain extends FragmentActivity implements OnClickListener {

    static class MyInnerHandler extends Handler {
        WeakReference<ScreenMain> wkRef;

        MyInnerHandler(ScreenMain aFragment) {
            wkRef = new WeakReference<ScreenMain>(aFragment);
        }

        @Override
        public void handleMessage(Message message) {
            updateUI(message);
        }
    }

    MyInnerHandler handler = new MyInnerHandler(this);

    private static View2D view2D;
    private static ProgressBar pbFlapsPosition;

    private Button btnFlapsExtend;

    private UdpReceiver udpReceiver;
    private static DataParserXHSI dataParser;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.screen_main);

        view2D = (View2D) findViewById(R.id.view2D);
        pbFlapsPosition = (ProgressBar) findViewById(R.id.pbFlapsPosition);
        btnFlapsExtend = (Button) findViewById(R.id.btnFlapsExtend);
        btnFlapsExtend.setOnClickListener(this);
        dataParser = new DataParserXHSI();

        // instantiate server
        udpReceiver = new UdpReceiver(handler);

        // Kickoff the Server
        new Thread(udpReceiver).start();
        /* GIve the Server some time for startup */
        try {

            Thread.sleep(500);

        } catch (InterruptedException e) {
        }

        // prevent screen frm going off
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected void onResume() {
        super.onResume();
        udpReceiver.setState(UdpReceiver.STATE_RUNNING);
    }

    protected void onPause() {
        super.onPause();
        udpReceiver.setState(UdpReceiver.STATE_STOPPED);
    }

    protected void onStop() {
        super.onStop();
        udpReceiver.setState(UdpReceiver.STATE_STOPPED);
    }

    protected void onDestroy() {
        super.onDestroy();
        udpReceiver.setState(UdpReceiver.STATE_STOPPED);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.menu_ip_address:
                DialogFragment fragment = new IpAddressDialogFragment(
                        udpReceiver.getLocalIpAddress());
                fragment.show(getSupportFragmentManager(), "ip_address");

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static void updateUI(Message message) {
        dataParser.setData((byte[]) message.obj);
        dataParser.parseData();
        if (!view2D.initGraphicsOk) {
            return;
        }
        // update PFD
        view2D.componentAdi.pitch = dataParser.pitch;
        view2D.componentAdi.roll = dataParser.roll;
        view2D.componentAdi.slip = dataParser.slip;
        view2D.componentSpeed.mSpeed = dataParser.speedTrue;
        view2D.componentAltitude.alt = dataParser.positionAltAboveMeanSeaLevel;
        view2D.componentHeading.heading = dataParser.headingMag;
        view2D.componentVsi.mVerticalSpeed = dataParser.verticalSpeed;
        // update controls
        pbFlapsPosition.setProgress((int) (dataParser.flapsPosition * 100));
    }

    @Override
    public void onClick(View v) {
        if (v.equals(btnFlapsExtend)) {
            // build byte buffer
            byte[] buffer = new byte[]{68, 65, 84, 65, 48, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            //set the index
            buffer[5] = 13;
            // convert value
            ByteBuffer bufftemp = ByteBuffer.allocate(4).putFloat(1211.489f);


        }
    }
}
