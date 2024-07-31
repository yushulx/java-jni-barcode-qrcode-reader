package com.java.barcode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.awt.image.*;
import javax.imageio.ImageIO;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.*;

@Service
public class ZXingBarcode {
    private static final Logger LOG = LoggerFactory.getLogger(ZXingBarcode.class);

    public BarcodeResponse decode(String filename, InputStream is) {
        try {
            LOG.info("Decoding barcodes...");
            BufferedImage image = ImageIO.read(is);
            int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(image.getWidth(), image.getHeight(), pixels)));
                
            MultiFormatReader reader = new MultiFormatReader();  
            GenericMultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(reader);
            String[] allResults = null, allFormats = null;

            try {
                Result[] results = multiReader.decodeMultiple(bitmap);
                
                if (results != null) {
                    allResults = new String[results.length];
                    allFormats = new String[results.length];
                    for (int i = 0; i < results.length; ++i) {
                        allResults[i] = results[i].getText();
                        allFormats[i] = results[i].getBarcodeFormat().toString();
                    }
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
            pixels = null;
            bitmap = null;
            reader = null;
            image.flush();
            image = null;
            
            return BarcodeResponse.builder().filename(filename).results(allResults).formats(allFormats).build();
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            return BarcodeResponse.builder().filename(filename).error(ex.getMessage()).build();
        }
    }
}