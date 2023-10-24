package fwse.group.gateway;

import java.io.IOException;

import fwse.group.gateway.Iot;
import fwse.group.gateway.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GatewayActivity extends Activity {
	/** Called when the activity is first created. */
	public final int CMD_RELAY_OFF = 0;
	public final int CMD_RELAY_ON = 1;
	public final int SEND_CMD = 3;
	private final String TAG = "GatewayActivity";
	private final int READ_INTERVAL = 300; // in milliseconds
	private final int RETRY_INTERVAL = 1000; // in milliseconds
	private Button on, off;
	private boolean connected = false;
	private final Handler mUIHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SEND_CMD:
				try {
					Iot.send(msg.getData());
				} catch (IOException e) {
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
			if (!connected) {
				if (!Iot.join()) {
					mUIHandler.postDelayed(mRun, RETRY_INTERVAL);
					return;
				}
				connected = true;
			}
			try {
				Bundle data = Iot.receive();
				boolean success = data.getBoolean("SUCCESS");
				boolean isOn = data.getBoolean("INFO");
				if (success) {
					setTitle("Success with Relay " + (isOn ? "on" : "off"));
				} else {
					setTitle("Fail");
				}
			} catch (IOException e) {
				// nothing, just another lazy period
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			} finally {
				mUIHandler.postDelayed(mRun, READ_INTERVAL);
			}
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
		findViews();
		setListeners();
        if (!Iot.open()) {
        	Log.e(TAG, "cannot open iot net");
			finish(); // XXX: let it explode
        }
		mUIHandler.postDelayed(mRun, READ_INTERVAL);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mUIHandler.removeCallbacks(mRun); // not necessary
        Iot.close();
		Log.d(TAG, "destroyed");
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
