package com.java.barcode;

import com.dynamsoft.dbr.*;
import java.awt.image.BufferedImage;
public interface BarcodeCallback {
    public void onResult(TextResult[] results, BufferedImage image);
}