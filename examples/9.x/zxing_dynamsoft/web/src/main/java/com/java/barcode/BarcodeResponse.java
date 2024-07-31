package com.java.barcode;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BarcodeResponse {
    private String filename;
    private String[] results;
    private String[] formats;
    private String error;
}
