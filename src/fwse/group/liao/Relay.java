package fwse.group.liao;

import java.io.IOException;

import android.util.Log;

public class Relay {
    static {
        try {
            Log.i("JNI", "Trying to load librelay.so");
            System.loadLibrary("relay");
        }
        catch (UnsatisfiedLinkError ule) {
            Log.e("JNI", "WARNING: Could not load librelay.so");
        }
    }

    public static void open() throws IOException {
        Log.i("RelayService", "Go to open Relay...");
        int ret = _open();
        if (ret < 0) {
            Log.e("RelayService", "Cannot open Relay");
            throw new IOException();
        }
    }

    public static boolean setOn() {
        Log.i("RelayService", "Relay On");
        int ret = _on(0);
        if (ret < 0) {
            return false;
        }
        return true;
    }

    public static boolean setOff() {
        Log.i("RelayService", "Relay Off");
        int ret = _off(0);
        if (ret < 0) {
            return false;
        }
        return true;
    }

    public static void close() throws IOException {
        Log.i("RelayService", "Shutting down");
        int ret = _close();
        if (ret < 0) {
            Log.e("RelayService", "Cannot close Relay");
        }
    }

    private static native int _open();
    private static native int _on(int num);
    private static native int _off(int num);
    private static native int _close();
}
