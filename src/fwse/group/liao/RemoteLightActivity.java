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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RemoteLightActivity extends Activity {
	/** Called when the activity is first created. */
	public final int CMD_RELAY_OFF = 0;
	public final int CMD_RELAY_ON = 1;
	private IIotService iotService = null;
	private final String TAG = "RemoteLightActivity";
	private final int READ_INTERVAL = 300; // in milliseconds
	private boolean isOn = false;
	private Button on, off;
	private HandlerThread iotHandlerThread = new HandlerThread("IotHT");
	private Handler iotHandler;
	private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case CMD_RELAY_OFF:
				setTitle("Remote cmd off");
				break;
			case CMD_RELAY_ON:
				setTitle("Remote cmd on");
				break;
			default:
				break;
			}
		}
	};

	private Runnable mRun = new Runnable() {
		@Override
		public void run() {
			try {
				Log.d(TAG, "run(): start loop run");
				if (iotService == null) {
					Log.w(TAG, "run(): cannot start iotService");
					return;
				}
				Bundle data = iotService.receive();
				Bundle echo = new Bundle();
				Log.d(TAG, "run(): received data");
				int cmd = data.getInt("CMD");
				int retry = 0;
				Log.d(TAG, "run(): execute command");
				switch (cmd) {
				case CMD_RELAY_OFF:
					mUIHandler.sendEmptyMessage(CMD_RELAY_OFF);
					while (!relayCtrl(false)) {
						retry += 1;
						Thread.sleep(10);
						if (retry > 10) {
							Log.e(TAG, "cannot turn off relay");
							break;
						}
					}
					isOn = (retry <= 10) ? false : isOn;
					echo.putBoolean("SUCCESS", retry <= 10);
					echo.putBoolean("INFO", isOn);
					iotService.send(echo);
					break;
				case CMD_RELAY_ON:
					mUIHandler.sendEmptyMessage(CMD_RELAY_ON);
					while (!relayCtrl(true)) {
						retry += 1;
						Thread.sleep(10);
						if (retry > 10) {
							Log.e(TAG, "cannot turn on relay");
							break;
						}
					}
					isOn = (retry <= 10) ? true : isOn;
					echo.putBoolean("SUCCESS", retry <= 10);
					echo.putBoolean("INFO", isOn);
					iotService.send(echo);
					break;
				default:
					break;
				}
				Log.d(TAG, "End one run in success state");
			} catch (RemoteException e) {
				// nothing, just another lazy period
				Log.d(TAG, "End one run in no get state");
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
		Log.d(TAG, "creating");
		bindService(new Intent(RemoteLightActivity.this, IotService.class),
				connection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "connection bound");
		setContentView(R.layout.main);
		findViews();
		setListeners();
		iotHandlerThread.start();
		iotHandler = new Handler(iotHandlerThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
			}
		};
		iotHandler.postDelayed(mRun, READ_INTERVAL);
		Log.d(TAG, "onCreate() success");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		iotHandler.removeCallbacks(mRun); // not necessary
		iotHandlerThread.quit();
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
