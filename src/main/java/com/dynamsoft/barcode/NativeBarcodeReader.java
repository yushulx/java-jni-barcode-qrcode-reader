package com.dynamsoft.barcode;

public class NativeBarcodeReader {
	
	private long nativePtr = 0;

	static {
		if (System.getProperty("java.vm.vendor").contains("Android")) {
			System.loadLibrary("dbr");
		} else {
			try {
				if (NativeLoader.load()) {
					System.out.println("Successfully loaded Dynamsoft Barcode Reader.");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public NativeBarcodeReader() {
		nativePtr = nativeCreateInstance();
	}
	
	public void destroyInstance() {
		if (nativePtr != 0)
			nativeDestroyInstance(nativePtr);
	}
	
	public void setLicense(String license) {
		nativeInitLicense(nativePtr, license);
	}
	
	public void decodeFile(String fileName) {
		nativeDecodeFile(nativePtr, fileName);
	}

	public String getVersion() {
		return nativeGetVersion();
	}

	private native int nativeInitLicense(long nativePtr, String license);
	
	private native long nativeCreateInstance();
	
	private native void nativeDestroyInstance(long nativePtr);
	
	private native void nativeDecodeFile(long nativePtr, String fileName);

	private native String nativeGetVersion();
}
