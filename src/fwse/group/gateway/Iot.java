package fwse.group.gateway;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.os.Bundle;
import android.os.Parcel;
import android.util.Base64;
import android.util.Log;

import fwse.group.gateway.BluetoothServer;

public class Iot {
	private static final String TAG = "Iot";

	public static final int PACKET_LEN = 256; // randomly set

	public static boolean open() {
		Log.i(TAG, "Go to get Iot Stub...");
		try {
			BluetoothServer.init();
			BluetoothServer.join();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public static boolean close() {
		Log.i(TAG, "Shutting down");
		try {
			BluetoothServer.leave();
			BluetoothServer.exit();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public static void send(Bundle bundle) throws IOException {
		Log.i(TAG, "Sending bytes");
		String str = Utility.serializeBundle(bundle);
		byte[] buffer = str.getBytes("UTF-8");
		// FIXME: need to limit size
		/* send data with packets */
		BluetoothServer.send(buffer);
	}

	public static Bundle receive() throws IOException {
		Log.i(TAG, "Receiving bytes");
		/* receive data with packets */
		String str = new String();
		byte[] buffer = new byte[PACKET_LEN];
		// FIXME: need to limit size
		BluetoothServer.receive(buffer);
		str = new String(buffer, Charset.forName("UTF-8"));
		return Utility.deserializeBundle(str);
	}

	public static boolean join() {
		Log.i(TAG, "Try to join the net");
		try {
			BluetoothServer.join();
		} catch (IOException e) {
			Log.e(TAG, "Cannot join the net");
			return false;
		} 
		return true;
	}

	public static boolean leave() {
		Log.i(TAG, "Leave the net");
		try {
			BluetoothServer.leave();
		} catch (IOException e) {
			Log.e(TAG, "what happened when leaving the net?!");
			return false;
		} 
		return true;
	}
}

class Utility {
	static String serializeBundle(final Bundle bundle) {
		String base64 = null;
		final Parcel parcel = Parcel.obtain();
		try {
			parcel.writeBundle(bundle);
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final GZIPOutputStream zos = new GZIPOutputStream(
					new BufferedOutputStream(bos));
			zos.write(parcel.marshall());
			zos.close();
			base64 = Base64.encodeToString(bos.toByteArray(), 0);
		} catch (IOException e) {
			e.printStackTrace();
			base64 = null;
		} finally {
			parcel.recycle();
		}
		return base64;
	}

	static Bundle deserializeBundle(final String base64) {
		Bundle bundle = null;
		final Parcel parcel = Parcel.obtain();
		try {
			final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];
			final GZIPInputStream zis = new GZIPInputStream(
					new ByteArrayInputStream(Base64.decode(base64, 0)));
			int len = 0;
			while ((len = zis.read(buffer)) != -1) {
				byteBuffer.write(buffer, 0, len);
			}
			zis.close();
			parcel.unmarshall(byteBuffer.toByteArray(), 0, byteBuffer.size());
			parcel.setDataPosition(0);
			bundle = parcel.readBundle();
		} catch (IOException e) {
			e.printStackTrace();
			bundle = null;
		} finally {
			parcel.recycle();
		}

		return bundle;
	}
}
