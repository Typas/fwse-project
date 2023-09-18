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

	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			iotService = IIotService.Stub.asInterface(service);
			Log.d(TAG, "connect iot");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			iotService = null;
			Log.d(TAG, "disconnect iot");
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "creating");
		bindService(new Intent(RemoteLightActivity.this, IotService.class),
				connection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "connection bound");
		setContentView(R.layout.main);
		Button on = (Button) findViewById(R.id.on);
		on.setOnClickListener(lightOnListener);
		Button off = (Button) findViewById(R.id.off);
		off.setOnClickListener(lightOffListener);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(connection);
	}

	private OnClickListener lightOnListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			setTitle("Power On");
			try {
				Relay.open();
			} catch (IOException e) {
				return;
			}
			Relay.setOn();
			try {
				Relay.close();
			} catch (IOException e) {
			}
		}
	};

	private OnClickListener lightOffListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			setTitle("Power Off");
			try {
				Relay.open();
			} catch (IOException e) {
				return;
			}
			Relay.setOff();
			try {
				Relay.close();
			} catch (IOException e) {
			}
		}
	};
}