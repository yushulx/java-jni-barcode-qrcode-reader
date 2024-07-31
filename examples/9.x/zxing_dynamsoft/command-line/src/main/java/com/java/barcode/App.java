package com.java.barcode;

import com.dynamsoft.dbr.*;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.*;
import java.io.File;
import java.io.IOException;
import java.awt.image.*;
import javax.imageio.ImageIO;

public class App 
{
    public void decodefile(String filename) {
        // Read an image to BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(filename));
        } catch (IOException e) {
            System.out.println(e);
            return;
        }

        // ZXing
        BinaryBitmap bitmap = null;
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        RGBLuminanceSource source = new RGBLuminanceSource(image.getWidth(), image.getHeight(), pixels);
        bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
        MultiFormatReader reader = new MultiFormatReader();  
        GenericMultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(reader);
        try {
            Result[] zxingResults = multiReader.decodeMultiple(bitmap);
            System.out.println("ZXing result count: " + zxingResults.length);
            if (zxingResults != null) {
                for (Result zxingResult : zxingResults) {
                    System.out.println("Format: " + zxingResult.getBarcodeFormat());
                    System.out.println("Text: " + zxingResult.getText());
                    System.out.println();
                }
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        pixels = null;
        bitmap = null;

        System.out.println("------------------------------------------------------");


        // Dynamsoft
        BarcodeReader br = null;
        try {
            // Get a license key from https://www.dynamsoft.com/customer/license/trialLicense?product=dbr
            BarcodeReader.initLicense("DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ==");
            br = new BarcodeReader();
        } catch (Exception e) {
            System.out.println(e);
            return;
        }
        
        TextResult[] results = null;
        try {
            results = br.decodeBufferedImage(image, "");
        } catch (Exception e) {
            System.out.println("decode buffered image: " + e);
        }

        if (results != null && results.length > 0) {
            System.out.println("DBR result count: " + results.length);
            String pszTemp = null;
            for (TextResult result : results) {
                if (result.barcodeFormat != 0) {
                    pszTemp = "Format: " + result.barcodeFormatString;
                } else {
                    pszTemp = "Format: " + result.barcodeFormatString_2;
                }
                System.out.println(pszTemp);
                System.out.println("Text: " + result.barcodeText);
                System.out.println();
            }
        }

        if (image != null) {
            image.flush();
            image = null;
        }
    }

    public static void main( String[] args )
    {
        if (args.length == 0) {
            System.out.println("Please add an image file");
            return;
        }
        
        final String filename = args[0];
        App test = new App();
        test.decodefile(filename);
    }
}
