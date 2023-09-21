package fwse.group.liao;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.util.UUID;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothClient {
	private static BluetoothAdapter btAdapter;
	private static BluetoothDevice btDevice;
	private static BluetoothSocket btSocket;
	// XXX: hard coded address, should use pairing mechanism
	private static final String remoteAddress = "00:21:11:01:76:ED";
	private static final UUID uuid = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB"); // HC-05 UUID
	private static OutputStream outputStream = null;
	private static InputStream inputStream = null;
	private static final String TAG = "BTClient";

	public static void init() throws IOException {
		Log.i(TAG, "init(): creating bluetooth adapter");
		btAdapter = BluetoothAdapter.getDefaultAdapter();

		if (btAdapter == null) {
			Log.e(TAG, "init(): device does not support Bluetooth");
			throw new IOException("Device not supported");
		}

		if (!btAdapter.isEnabled())
			btAdapter.enable();

		Log.d(TAG, "Bluetooth adapter opened");
		btDevice = btAdapter.getRemoteDevice(remoteAddress);

	}

	public static void exit() throws IOException {
		btAdapter.disable();
		btAdapter = null;
	}

	public static void send(byte[] buffer) throws IOException {
		if (outputStream == null)
			throw new IOException("no opened output stream");
		outputStream.write(buffer);
	}

	public static void receive(byte[] buffer) throws IOException {
		if (inputStream == null)
			throw new IOException("no opened input stream");
		inputStream.read(buffer);
	}

	public static void join() throws IOException {
		btSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
		btAdapter.cancelDiscovery();
		btSocket.connect();
		inputStream = btSocket.getInputStream();
		outputStream = btSocket.getOutputStream();
	}

	public static void leave() throws IOException {
		if (btSocket == null)
			throw new IOException("no opened socket");
		if (btSocket.isConnected())
			btSocket.close();
		outputStream = null;
		inputStream = null;
	}
}
