package fwse.group.gateway;

import fwse.group.gateway.IIotService;
import fwse.group.gateway.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GatewayActivity extends Activity {
	/** Called when the activity is first created. */
	public final int CMD_RELAY_OFF = 0;
	public final int CMD_RELAY_ON = 1;
	public final int SEND_CMD = 3;
	private IIotService iotService = null;
	private final String TAG = "GatewayActivity";
	private final int READ_INTERVAL = 100; // in milliseconds
	private Button on, off;
	private final Handler mUIHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SEND_CMD:
				try {
					iotService.send(msg.getData());
				} catch (RemoteException e) {
					Log.e(TAG, "cannot send command");
				}
				break;
			default:
				break;
			}
		}
	};

	private final Runnable mRun = new Runnable() {
		@Override
		public void run() {
			try {
				Bundle data = iotService.receive();
				boolean success = data.getBoolean("SUCCESS");
				boolean isOn = data.getBoolean("INFO");
				if (success) {
					setTitle("Success with Relay " + (isOn ? "on" : "off"));
				} else {
					setTitle("Fail");
				}
			} catch (RemoteException e) {
				// nothing, just another lazy period
			} catch (Exception e) {
				Log.e(TAG, e.toString());
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
		setContentView(R.layout.main);
		Log.d(TAG, "creating");
		bindService(new Intent(GatewayActivity.this, IotService.class),
				connection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "connection bound");
		findViews();
		setListeners();
		mUIHandler.postDelayed(mRun, READ_INTERVAL);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mUIHandler.removeCallbacks(mRun); // not necessary
		unbindService(connection);
		Log.d(TAG, "disconnected");
	}

	private OnClickListener lightOnListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			setTitle("Remote Power On");
			Bundle data = new Bundle();
			data.putInt("CMD", CMD_RELAY_OFF);
			Message msg = new Message();
			msg.what = SEND_CMD;
			msg.setData(data);
			mUIHandler.sendMessage(msg);
		}
	};

	private OnClickListener lightOffListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			setTitle("Remote Power Off");
			Bundle data = new Bundle();
			data.putInt("CMD", CMD_RELAY_OFF);
			Message msg = new Message();
			msg.what = SEND_CMD;
			msg.setData(data);
			mUIHandler.sendMessage(msg);
		}
	};
}