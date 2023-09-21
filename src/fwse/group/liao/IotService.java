package fwse.group.liao;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
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
			this.onDestroy();
		Log.d(TAG, "created");
	}
}
