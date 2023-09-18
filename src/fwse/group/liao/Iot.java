package fwse.group.liao;

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

public class Iot {
	static {
		try {
			Log.i("JNI", "Trying to load libiot.so");
			System.loadLibrary("iot");
		} catch (UnsatisfiedLinkError ule) {
			Log.e("JNI", "WARNING: Could not load libiot.so");
		}
	}

	public static final int PACKET_LEN = 64;

	public static boolean open() {
		Log.i("IotService", "Go to get Iot Stub...");
		if (_init() == false) {
			return false;
		}
		return _join();
	}

	public static boolean close() {
		Log.i("IotService", "Shutting down");
		if (_leave() == false) {
			return false;
		}
		return _exit();
	}

	public static void send(Bundle bundle) throws IOException {
		Log.i("IotService", "Sending bytes");
		String str = Utility.serializeBundle(bundle);
		byte[] buffer = str.getBytes("UTF-8");
		/* send data with packets */
		if (_send(buffer) == false) {
			throw new IOException();
		}
	}

	public static Bundle receive() throws IOException {
		Log.i("IotService", "Receiving bytes");
		boolean ret = false;
		String str = new String();
		byte[] buffer = new byte[PACKET_LEN];
		/* receive data with packets */
		while (_recv(buffer) == true) {
			ret = true;
			str += new String(buffer, Charset.forName("UTF-8"));
		}

		if (ret == false) {
			throw new IOException();
		}
		return Utility.deserializeBundle(str);
	}

	public static boolean join() {
		Log.i("IotService", "Try to join the net");
		return _join();
	}

	public static boolean leave() {
		Log.i("IotService", "Leave the net");
		return _leave();
	}

	/* private methods link to jni */
	private static native boolean _init();

	private static native boolean _exit();

	private static native boolean _send(byte[] buffer);

	private static native boolean _recv(byte[] buffer);

	private static native boolean _join();

	private static native boolean _leave();
}

class Utility {
	// public static byte[][] splitBytes(final byte[] array, final int
	// chunkSize) {
	// final int length = array.length;
	// final byte[][] dest = new byte[(length + chunkSize - 1) / chunkSize][];
	// int destIndex = 0;
	// int stopIndex = 0;

	// for (int index = 0; index + chunkSize <= length; index += chunkSize) {
	// stopIndex += chunkSize;
	// dest[destIndex] = Arrays.copyOfRange(array, index, stopIndex);
	// ++destIndex;
	// }

	// if (stopIndex < length) {
	// dest[destIndex] = Arrays.copyOfRange(array, stopIndex, length);
	// }

	// return dest;
	// }

	// public static byte[] mergeBytes(final byte[] first, final byte[] second)
	// {
	// final byte[] dest = Arrays.copyOf(first, first.length + second.length);
	// System.arraycopy(second, 0, dest, first.length, second.length);
	// return dest;
	// }

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
