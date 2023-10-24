package fwse.group.gateway;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.util.UUID;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothServer {
	private static BluetoothAdapter btAdapter;
	// XXX: should accept more clients
	private static BluetoothSocket btSocket;
	private static final String btName = "HC05_Master";
	// XXX: hard coded address, should use pairing mechanism
	// FIXME: should have a table for lookup
	private static final String remoteAddress = "00:21:11:01:4A:88";
	private static final UUID uuid = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB"); // HC-05 UUID
	private static OutputStream outputStream = null;
	private static InputStream inputStream = null;
	private static final String TAG = "BTServer";

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
		btAdapter.getRemoteDevice(remoteAddress);

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
		btAdapter.startDiscovery();
		BluetoothServerSocket btServerSocket = btAdapter.listenUsingInsecureRfcommWithServiceRecord(btName, uuid);
		btSocket = btServerSocket.accept();
		btServerSocket.close();
		btAdapter.cancelDiscovery();
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
