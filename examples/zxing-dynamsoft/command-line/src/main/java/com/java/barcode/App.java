package com.java.barcode;

import com.dynamsoft.core.EnumErrorCode;
import com.dynamsoft.cvr.CaptureVisionRouter;
import com.dynamsoft.cvr.CapturedResult;
import com.dynamsoft.cvr.EnumPresetTemplate;
import com.dynamsoft.dbr.BarcodeResultItem;
import com.dynamsoft.dbr.DecodedBarcodesResult;
import com.dynamsoft.license.LicenseError;
import com.dynamsoft.license.LicenseException;
import com.dynamsoft.license.LicenseManager;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class App 
{
    public void decodeWithZXing(String filename) {
        // Read an image to BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(filename));
        } catch (IOException e) {
            System.out.println(e);
            return;
        }

        // ZXing
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        RGBLuminanceSource source = new RGBLuminanceSource(image.getWidth(), image.getHeight(), pixels);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
        MultiFormatReader reader = new MultiFormatReader();  
        GenericMultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(reader);
        long startTime = System.currentTimeMillis();
        try {
            Result[] zxingResults = multiReader.decodeMultiple(bitmap);
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("ZXing result count: " + zxingResults.length + " (in " + elapsed + " ms)");
            for (Result zxingResult : zxingResults) {
                System.out.println("Format: " + zxingResult.getBarcodeFormat());
                System.out.println("Text: " + zxingResult.getText());
                System.out.println();
            }
        } catch (NotFoundException e) {
            System.out.println("ZXing found no barcode.");
        }
        image.flush();
    }

    public void decodeWithDynamsoft(String filename) {
        // Dynamsoft
        // Get a 30-day free trial license from https://www.dynamsoft.com/customer/license/trialLicense/?product=dcv&package=cross-platform
        String licenseKey = System.getenv().getOrDefault("DBR_LICENSE_KEY", "LICENSE-KEY");
        try {
            LicenseError licenseError = LicenseManager.initLicense(licenseKey);
            if (licenseError.getErrorCode() != EnumErrorCode.EC_OK) {
                System.out.println("License initialization failed: " + licenseError.getErrorString());
                return;
            }
        } catch (LicenseException e) {
            System.out.println("License initialization failed: " + e.getErrorString());
            return;
        }
        
        CaptureVisionRouter cvRouter = new CaptureVisionRouter();
        long startTime = System.currentTimeMillis();
        CapturedResult result = cvRouter.capture(filename, EnumPresetTemplate.PT_READ_BARCODES);
        long elapsed = System.currentTimeMillis() - startTime;

        if (result.getErrorCode() != EnumErrorCode.EC_OK) {
            System.out.println("Error: " + result.getErrorCode() + ", " + result.getErrorString());
            return;
        }

        DecodedBarcodesResult barcodeResult = result.getDecodedBarcodesResult();
        BarcodeResultItem[] items = barcodeResult != null ? barcodeResult.getItems() : null;
        if (items == null || items.length == 0) {
            System.out.println("Dynamsoft found no barcode.");
            return;
        }

        System.out.println("Dynamsoft result count: " + items.length + " (in " + elapsed + " ms)");
        for (BarcodeResultItem item : items) {
            System.out.println("Format: " + item.getFormatString());
            System.out.println("Text: " + item.getText());
            System.out.println();
        }
    }

    public void decodeFile(String filename) {
        decodeWithZXing(filename);
        System.out.println("------------------------------------------------------");
        decodeWithDynamsoft(filename);
    }

    public static void main( String[] args )
    {
        if (args.length == 0) {
            System.out.println("Please add an image file");
            return;
        }
        
        final String filename = args[0];
        App test = new App();
        test.decodeFile(filename);
    }
}
