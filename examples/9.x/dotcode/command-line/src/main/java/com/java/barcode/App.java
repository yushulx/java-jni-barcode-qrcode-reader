package com.java.barcode;

import com.dynamsoft.dbr.*;

public class App 
{
    public void decodefile(String filename) {

        BarcodeReader br = null;
        try {
            BarcodeReader.initLicense("DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ==");
            br = new BarcodeReader();
            //Best coverage settings
            br.initRuntimeSettingsWithString("{\"ImageParameter\":{\"Name\":\"BestCoverage\",\"DeblurLevel\":9,\"ExpectedBarcodesCount\":512,\"ScaleDownThreshold\":100000,\"LocalizationModes\":[{\"Mode\":\"LM_CONNECTED_BLOCKS\"},{\"Mode\":\"LM_SCAN_DIRECTLY\"},{\"Mode\":\"LM_STATISTICS\"},{\"Mode\":\"LM_LINES\"},{\"Mode\":\"LM_STATISTICS_MARKS\"}],\"GrayscaleTransformationModes\":[{\"Mode\":\"GTM_ORIGINAL\"},{\"Mode\":\"GTM_INVERTED\"}]}}", EnumConflictMode.CM_OVERWRITE);
            //Best speed settings
            //br.initRuntimeSettingsWithString("{\"ImageParameter\":{\"Name\":\"BestSpeed\",\"DeblurLevel\":3,\"ExpectedBarcodesCount\":512,\"LocalizationModes\":[{\"Mode\":\"LM_SCAN_DIRECTLY\"}],\"TextFilterModes\":[{\"MinImageDimension\":262144,\"Mode\":\"TFM_GENERAL_CONTOUR\"}]}}",EnumConflictMode.CM_OVERWRITE);
            //Balance settings
            //br.initRuntimeSettingsWithString("{\"ImageParameter\":{\"Name\":\"Balance\",\"DeblurLevel\":5,\"ExpectedBarcodesCount\":512,\"LocalizationModes\":[{\"Mode\":\"LM_CONNECTED_BLOCKS\"},{\"Mode\":\"LM_STATISTICS\"}]}}",EnumConflictMode.CM_OVERWRITE);

            PublicRuntimeSettings runtimeSettings = br.getRuntimeSettings();
            runtimeSettings.barcodeFormatIds_2 = EnumBarcodeFormat_2.BF2_DOTCODE;
            br.updateRuntimeSettings(runtimeSettings);
        } catch (Exception e) {
            System.out.println(e);
            return;
        }
        
        TextResult[] results = null;
        try {
            results = br.decodeFile(filename, "");
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
