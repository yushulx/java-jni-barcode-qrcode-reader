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
			if (Utils.isWindows()) {
				barcodeReader.setLicense("t0068NQAAAHUXFSYfiYRRYxSfFw91lAu3tHxtyJ3cB2gcBAeGbYLh0Z1qy5/OTmj9SGsOCfjniJXnjHwRcCHSfOazmetgsQM=");
				// test image - g:\\images\\AllSupportedBarcodeTypes.tif
				barcodeReader.decodeFile(fileName);
			}
			else if (Utils.isLinux()) {
				barcodeReader.setLicense("t0068NQAAAIfO7vqx/meDGR0UNVwA+Lh18IGJE+8qQRkEr6FSiEaKgtcC13mm2i6ypPu6NYiUqIiQVLg685oV/P3sUYZ9Efc=");
				// test image - /mnt/g/images/AllSupportedBarcodeTypes.tif
				barcodeReader.decodeFile(fileName);
			}
			else if (Utils.isMac()) {
				barcodeReader.setLicense("t0068NQAAAK9aYt6BEaJ8M9GZVtK7WdhO06QMwajp640ftbwXH93h9xsoc8vuI+OQyr9gW2TOzE/gcHJbiaN9QZQ8vjsw444=");
				// test image - /Users/catherinesea/barcode/AllSupportedBarcodeTypes.tif
				barcodeReader.decodeFile(fileName);
			}
			barcodeReader.destroyInstance();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
