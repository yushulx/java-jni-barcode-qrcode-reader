package com.dynamsoft;

import com.dynamsoft.dbr.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.awt.image.*;
import javax.imageio.ImageIO;

public final class App {
    private App() {
    }

    public void decodefile(String filename) {
        int iIndex = 0;
        String pszImageFile = filename;
        BarcodeReader br = null;
        try {
            // Get a license key from https://www.dynamsoft.com/customer/license/trialLicense?product=dbr
            BarcodeReader.initLicense("DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ==");
            br = new BarcodeReader();
        } catch (Exception e) {
            System.out.println(e);
            return;
        }

        // BufferedImage img = null;
        // try {
        //     img = ImageIO.read(new File(pszImageFile));
        // } catch (IOException e) {
        //     System.out.println(e);
        // }

        TextResult[] results = null;
        try {
            // results = br.decodeBufferedImage(img, "");
            results = br.decodeFile(pszImageFile, "");
            // results = br.decodeFileInMemory(Files.readAllBytes(new File(pszImageFile).toPath()), "");
        } catch (Exception e) {
            System.out.println("decode buffered image: " + e);
        }

        if (results != null && results.length > 0) {
            String pszTemp = null;
            iIndex = 0;
            for (TextResult result : results) {
                iIndex++;
                pszTemp = String.format("  Barcode %d:", iIndex);
                System.out.println(pszTemp);
                pszTemp = String.format("    Page: %d", result.localizationResult.pageNumber + 1);
                System.out.println(pszTemp);
                if (result.barcodeFormat != 0) {
                    pszTemp = "    Type: " + result.barcodeFormatString;
                } else {
                    pszTemp = "    Type: " + result.barcodeFormatString_2;
                }
                System.out.println(pszTemp);
                pszTemp = "    Value: " + result.barcodeText;
                System.out.println(pszTemp);

                pszTemp = String.format("    Region points: {(%d,%d),(%d,%d),(%d,%d),(%d,%d)}",
                        result.localizationResult.resultPoints[0].x, result.localizationResult.resultPoints[0].y,
                        result.localizationResult.resultPoints[1].x, result.localizationResult.resultPoints[1].y,
                        result.localizationResult.resultPoints[2].x, result.localizationResult.resultPoints[2].y,
                        result.localizationResult.resultPoints[3].x, result.localizationResult.resultPoints[3].y);

                System.out.println(pszTemp);
                System.out.println();
            }
        }
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please add an image file");
            return;
        }
        
        final String filename = args[0];

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
         
        for (int i = 1; i <= 1; i++) 
        {
            executor.execute(new Runnable(){
            
                @Override
                public void run() {
                    App test = new App();
                    test.decodefile(filename);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }
                }
            });
        }
        executor.shutdown();
    }
}
