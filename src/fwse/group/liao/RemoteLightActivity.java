package fwse.group.liao;

import java.io.IOException;

import fwse.group.liao.R;
import fwse.group.liao.IotService;
import fwse.group.liao.Relay;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RemoteLightActivity extends Activity {
	/** Called when the activity is first created. */
	private IIotService iotService = null;
	private final String TAG = "RemoteLightActivity";
    private Button on, off;
    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case IotService.RECV_START:
                    break;
                case IotService.RECV_END:
                    break;
                case IotService.SEND_SUCCESS:
                    break;
                case IotService.SEND_FAIL:
                    break;
            }
        }
    };

	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			iotService = IIotService.Stub.asInterface(service);
			Log.d(TAG, "connect iot");
            if (iotService == null) {
                Log.e(TAG, "cannot connect");
            }
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			iotService = null;
			Log.d(TAG, "disconnect iot");
		}
	};

    private void findViews() {
		on = (Button) findViewById(R.id.on);
		off = (Button) findViewById(R.id.off);
    }

    private void setListeners() {
		on.setOnClickListener(lightOnListener);
		off.setOnClickListener(lightOffListener);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "creating");
		bindService(new Intent(RemoteLightActivity.this, IotService.class),
				connection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "connection bound");
		setContentView(R.layout.main);
        findViews();
        setListeners();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(connection);
		Log.d(TAG, "disconnected");
	}

	private OnClickListener lightOnListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			setTitle("Power On");
            relayCtrl(true);
		}
	};

	private OnClickListener lightOffListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			setTitle("Power Off");
            relayCtrl(false);
		}
	};

    private boolean relayCtrl(boolean on) {
        boolean ret = true;
        try {
            Relay.open();
            if (on == true)
                ret = Relay.setOn();
            else
                ret = Relay.setOff();
            Relay.close();
        } catch (IOException e) {
            return false;
        }
        return ret;
    }
}
