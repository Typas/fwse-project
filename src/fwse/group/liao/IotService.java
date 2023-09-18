package fwse.group.liao;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import fwse.group.liao.Iot;

public class IotService extends Service {
	private static final String TAG = "IotService";

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "started");
	}

	private IIotService.Stub mBinder = new IIotService.Stub() {
		@Override
		public void send(Bundle bundle) throws RemoteException {
			Log.d(TAG, "sending");
			bundle.setClassLoader(getClass().getClassLoader());
			try {
				Iot.send(bundle);
			} catch (IOException e) {
				throw new RemoteException(e.toString());
			}
		}

		@Override
		public Bundle receive() throws RemoteException {
			Bundle bundle;
			Log.d(TAG, "receiving");
			try {
				bundle = Iot.receive();
			} catch (IOException e) {
				throw new RemoteException(e.toString());
			}
			return bundle;
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Iot.close();
		Log.d(TAG, "destroyed");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (Iot.open() == false)
			onDestroy();
		Log.d(TAG, "created");
	}
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// parse message and set it
			Bundle bundle = msg.getData();
			// FIXME: action depends on command
			boolean command = bundle.getBoolean("power on");
			if (command == true) {
				// turn on
				try {
				Relay.open();
				} catch (IOException e) {
					Log.e(TAG, e.toString());
					super.handleMessage(msg);
					return;
				}
				Relay.setOn();
				try {
					Relay.close();
				} catch (IOException e) {
					Log.e(TAG, e.toString());
				}
			} else {
				// turn off
				try {
				Relay.open();
				} catch (IOException e) {
					Log.e(TAG, e.toString());
					super.handleMessage(msg);
					return;
				}
				Relay.setOff();
				try {
					Relay.close();
				} catch (IOException e) {
					Log.e(TAG, e.toString());
				}
			}
			super.handleMessage(msg);
		}
	};
}
