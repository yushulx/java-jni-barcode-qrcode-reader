package com.example.litecam;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LiteCam implements AutoCloseable {
    static {
        // Attempt to load the native library from bundled jar resources first, else fallback to standard library path.
        boolean loaded = false;
        try {
            loaded = loadBundled();
        } catch (Throwable t) {
            // ignore and fallback
        }
        if (!loaded) {
            System.loadLibrary("litecam");
        }
    }

    private static boolean loadBundled() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        String osToken;
        if (os.contains("win")) osToken = "windows"; else if (os.contains("mac") || os.contains("darwin")) osToken = "macos"; else if (os.contains("nux") || os.contains("linux")) osToken = "linux"; else return false;
        String archToken;
        if (arch.contains("aarch64") || arch.contains("arm64")) archToken = "arm64"; else if (arch.contains("64")) archToken = "x86_64"; else archToken = arch; // fallback

        String libBase = "litecam";
        String ext = osToken.equals("windows") ? ".dll" : (osToken.equals("macos") ? ".dylib" : ".so");
        String resourcePath = "/natives/" + osToken + "-" + archToken + "/" + (osToken.equals("windows") ? libBase + ext : "lib" + libBase + ext);
        try (java.io.InputStream in = LiteCam.class.getResourceAsStream(resourcePath)) {
            if (in == null) return false;
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile(libBase + "-", ext);
            try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(tempFile)) {
                byte[] buf = new byte[8192]; int r; while ((r = in.read(buf)) != -1) out.write(buf, 0, r);
            }
            tempFile.toFile().deleteOnExit();
            System.load(tempFile.toAbsolutePath().toString());
            return true;
        }
    }

    private int handle = 0;

    // Native methods
    public static native String[] listDevices();
    private native int open(int deviceIndex);
    private native void nativeClose(int handle);
    public native int[] listSupportedResolutions(int handle); // flattened width,height pairs
    public native boolean setResolution(int handle, int width, int height);
    public native boolean captureFrame(int handle, ByteBuffer rgbOut); // expects direct buffer
    public native int getFrameWidth(int handle);
    public native int getFrameHeight(int handle);

    public void openDevice(int index) {
        if (handle != 0) throw new IllegalStateException("Already opened");
        handle = open(index);
        if (handle == 0) throw new RuntimeException("Failed to open camera index " + index);
    }

    public void closeDevice() {
        if (handle != 0) {
        nativeClose(handle);
            handle = 0;
        }
    }

    @Override
    public void close() { closeDevice(); }

    public List<int[]> getSupportedResolutions() {
        int[] flat = listSupportedResolutions(handle);
        List<int[]> list = new ArrayList<>();
        if (flat != null) {
            for (int i=0;i+1<flat.length;i+=2) {
                list.add(new int[]{flat[i], flat[i+1]});
            }
        }
        return list;
    }

    public boolean setResolution(int w, int h) { return setResolution(handle, w, h); }

    public int getWidth() { return getFrameWidth(handle); }
    public int getHeight() { return getFrameHeight(handle); }

    public boolean grabFrame(ByteBuffer dst) { return captureFrame(handle, dst); }

    public boolean isOpen() { return handle != 0; }
}
