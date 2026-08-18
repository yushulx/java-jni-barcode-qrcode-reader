package com.java.barcode;

import com.dynamsoft.core.EnumErrorCode;
import com.dynamsoft.cvr.CaptureVisionRouter;
import com.dynamsoft.cvr.CapturedResult;
import com.dynamsoft.cvr.EnumPresetTemplate;
import com.dynamsoft.dbr.BarcodeResultItem;
import com.dynamsoft.dbr.DecodedBarcodesResult;
import com.dynamsoft.license.LicenseError;
import com.dynamsoft.license.LicenseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class DynamsoftBarcode {
    private static final Logger LOG = LoggerFactory.getLogger(DynamsoftBarcode.class);

    // Get a 30-day free trial license from https://www.dynamsoft.com/customer/license/trialLicense/?product=dcv&package=cross-platform
    @Value("${DBR_LICENSE_KEY:LICENSE-KEY}")
    private String license;

    public BarcodeResponse decode(String filename, InputStream is) {
        try {
            LOG.info("Decoding barcodes...");

            LicenseError licenseError = LicenseManager.initLicense(license);
            if (licenseError.getErrorCode() != EnumErrorCode.EC_OK) {
                return BarcodeResponse.builder().filename(filename)
                        .error("License initialization failed: " + licenseError.getErrorString()).build();
            }

            CaptureVisionRouter cvRouter = new CaptureVisionRouter();
            CapturedResult result = cvRouter.capture(is.readAllBytes(), EnumPresetTemplate.PT_READ_BARCODES);

            String[] allResults = null, allFormats = null;
            if (result.getErrorCode() == EnumErrorCode.EC_OK) {
                DecodedBarcodesResult barcodeResult = result.getDecodedBarcodesResult();
                BarcodeResultItem[] items = barcodeResult != null ? barcodeResult.getItems() : null;
                if (items != null) {
                    allResults = new String[items.length];
                    allFormats = new String[items.length];
                    for (int i = 0; i < items.length; ++i) {
                        allResults[i] = items[i].getText();
                        allFormats[i] = items[i].getFormatString();
                    }
                }
            } else {
                return BarcodeResponse.builder().filename(filename).error(result.getErrorString()).build();
            }

            return BarcodeResponse.builder().filename(filename).results(allResults).formats(allFormats).build();
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            return BarcodeResponse.builder().filename(filename).error(ex.getMessage()).build();
        }
    }
}