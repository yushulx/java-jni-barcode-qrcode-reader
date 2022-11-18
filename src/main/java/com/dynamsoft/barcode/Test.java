package com.dynamsoft.barcode;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length < 1) {
			System.out.println("Please input an image file.");
			return;
		}
		String fileName = args[0];
		try {
			NativeBarcodeReader barcodeReader = new NativeBarcodeReader();
			System.out.println("Version number: " + barcodeReader.getVersion());
			barcodeReader.setLicense("DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ==");
			barcodeReader.decodeFile(fileName);
			barcodeReader.destroyInstance();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
