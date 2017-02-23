package com.frederictheriault.parrotai.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.frederictheriault.parrotai.R;
import com.frederictheriault.parrotai.ai.BasicAI;
import com.frederictheriault.parrotai.ai.DroneAI;
import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_STREAM_CODEC_TYPE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.arsal.ARNativeData;
import com.frederictheriault.parrotai.audio.AudioPlayer;
import com.frederictheriault.parrotai.audio.AudioRecorder;
import com.frederictheriault.parrotai.drone.JSDrone;
import com.frederictheriault.parrotai.view.JSVideoView;

public class JSActivity extends CommonActivity {
    private static final String TAG = "JSActivity";

    private JSDrone mJSDrone;
    private DroneAI droneAI;

    private ProgressDialog mConnectionProgressDialog;

    private JSVideoView mVideoView;
    private AudioPlayer mAudioPlayer;

    private TextView mBatteryLabel;
    private Button basicAIBtn;
    private Button stopBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_js);

        Intent intent = getIntent();
        ARDiscoveryDeviceService service = intent.getParcelableExtra(DeviceListActivity.EXTRA_DEVICE_SERVICE);

        mJSDrone = new JSDrone(this, service);
        mJSDrone.addListener(mJSListener);

        initIHM();

        mAudioPlayer = new AudioPlayer();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // show a loading view while the JumpingSumo drone is connecting
        if ((mJSDrone != null) && !(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mJSDrone.getConnectionState())))
        {
            mConnectionProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Connecting ...");
            mConnectionProgressDialog.show();

            // if the connection to the Jumping fails, finish the activity
            if (!mJSDrone.connect()) {
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mJSDrone != null)
        {
            stopAI();

            mConnectionProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Disconnecting ...");
            mConnectionProgressDialog.show();

            if (!mJSDrone.disconnect()) {
                finish();
            }
        }
    }

    public void stopAI() {
        if (droneAI != null) {
            setRunningUI(false);
            droneAI.stopRunning();
            droneAI = null;

            mJSDrone.setTurn((byte)0);
            mJSDrone.setSpeed((byte)0);
            mJSDrone.setFlag((byte)0);
        }
    }

    @Override
    public void onDestroy()
    {
        stopAI();
        mAudioPlayer.stop();
        mAudioPlayer.release();

        mJSDrone.dispose();
        super.onDestroy();
    }

    private void initIHM() {
        mVideoView = (JSVideoView) findViewById(R.id.videoView);
        mBatteryLabel = (TextView) findViewById(R.id.batteryLabel);
        basicAIBtn = (Button)findViewById(R.id.basicAIBtn);
        stopBtn = (Button)findViewById(R.id.stopBtn);

        basicAIBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            if (droneAI == null) {
                setRunningUI(true);
                droneAI = new BasicAI(mJSDrone);
                droneAI.start();
            }
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopAI();
            }
        });

        setRunningUI(false);
    }

    private void setRunningUI(boolean running) {
        stopBtn.setEnabled(running);
        basicAIBtn.setEnabled(!running);
    }

    private final JSDrone.Listener mJSListener = new JSDrone.Listener() {
        @Override
        public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
            switch (state)
            {
                case ARCONTROLLER_DEVICE_STATE_RUNNING:
                    mConnectionProgressDialog.dismiss();
                    break;

                case ARCONTROLLER_DEVICE_STATE_STOPPED:
                    // if the deviceController is stopped, go back to the previous activity
                    mConnectionProgressDialog.dismiss();
                    finish();
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onBatteryChargeChanged(int batteryPercentage) {
            mBatteryLabel.setText(String.format("%d%%", batteryPercentage));
        }

        @Override
        public void onPictureTaken(ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {

        }

        @Override
        public void configureDecoder(ARControllerCodec codec) {
        }

        @Override
        public void onFrameReceived(ARFrame frame) {
            byte[] data = frame.getByteData();
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

            if (droneAI != null) {
                droneAI.getDroneState().setBitmap(bmp);

                Bitmap tmp = droneAI.getDroneState().getDisplayBitmap();

                if (tmp != null) {
                    bmp = tmp;
                }
            }

            mVideoView.displayFrame(bmp);
        }

        @Override
        public void onAudioStateReceived(boolean inputEnabled, boolean outputEnabled) {
            if (inputEnabled) {
                mAudioPlayer.start();
            } else {
                mAudioPlayer.stop();
            }
        }

        @Override
        public void configureAudioDecoder(ARControllerCodec codec) {
            if (codec.getType() == ARCONTROLLER_STREAM_CODEC_TYPE_ENUM.ARCONTROLLER_STREAM_CODEC_TYPE_PCM16LE) {
                ARControllerCodec.PCM16LE codecPCM16le = codec.getAsPCM16LE();
                mAudioPlayer.configureCodec(codecPCM16le.getSampleRate());
            }
        }

        @Override
        public void onAudioFrameReceived(ARFrame frame) {
            mAudioPlayer.onDataReceived(frame);
        }

        @Override
        public void onMatchingMediasFound(int nbMedias) {
        }

        @Override
        public void onDownloadProgressed(String mediaName, int progress) {

        }

        @Override
        public void onDownloadComplete(String mediaName) {

        }
    };

    private final AudioRecorder.Listener mAudioListener = new AudioRecorder.Listener() {
        @Override
        public void sendFrame(ARNativeData data) {
            mJSDrone.sendStreamingFrame(data);
        }
    };
}
