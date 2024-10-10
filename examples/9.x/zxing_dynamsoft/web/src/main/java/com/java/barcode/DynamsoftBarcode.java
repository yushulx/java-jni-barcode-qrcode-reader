package com.java.barcode;

import com.dynamsoft.dbr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class DynamsoftBarcode {
    private static final Logger LOG = LoggerFactory.getLogger(DynamsoftBarcode.class);
    private String license;

    public DynamsoftBarcode() {
        // Get license from https://www.dynamsoft.com/CustomerPortal/Portal/Triallicense.aspx
        license = "LICENSE-KEY";
    }

    public BarcodeResponse decode(String filename, InputStream is) {
        try {
            LOG.info("Decoding barcodes...");

            // Get a license key from https://www.dynamsoft.com/customer/license/trialLicense/?product=dcv&package=cross-platform
            BarcodeReader.initLicense("LICENSE-KEY");
            BarcodeReader barcodeReader = new BarcodeReader();
            TextResult[] results = barcodeReader.decodeFileInMemory(is, "");
            String[] allResults = null, allFormats = null;
            if (results != null) {
                allResults = new String[results.length];
                allFormats = new String[results.length];
                for (int i = 0; i < results.length; ++i) {
                    allResults[i] = results[i].barcodeText;
                    allFormats[i] = results[i].barcodeFormatString;
                }
            }
            
            if (barcodeReader != null) {
                barcodeReader = null;
            }
            return BarcodeResponse.builder().filename(filename).results(allResults).formats(allFormats).build();
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            return BarcodeResponse.builder().filename(filename).error(ex.getMessage()).build();
        }
    }
}