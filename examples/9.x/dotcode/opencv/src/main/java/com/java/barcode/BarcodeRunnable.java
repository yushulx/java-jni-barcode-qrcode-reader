package com.java.barcode;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dynamsoft.dbr.*;
import org.opencv.core.Mat;

public class BarcodeRunnable implements Runnable {
    private Mat frame;
    private BarcodeReader reader;
    private BarcodeCallback callback;
    private AtomicBoolean status;

    public BarcodeRunnable(Mat frame, BarcodeReader reader, BarcodeCallback callback, AtomicBoolean status) {
        this.frame = frame;
        this.reader = reader;
        this.callback = callback;
        this.status = status;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            TextResult[] results = reader.decodeBuffer(Utils.matToByteArray(frame), frame.width(), frame.height(), (int)frame.step1(), EnumImagePixelFormat.IPF_BGR_888, "");
            if (results != null && results.length > 0) {
                if (callback != null) {
                    callback.onResult(results, Utils.matToBufferedImage(frame));
                }
            }
            else {
                status.set(false);
            }
            
        } catch (BarcodeReaderException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        }
    }

}