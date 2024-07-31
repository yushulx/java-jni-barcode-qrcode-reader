package com.java.barcode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class BarcodeController {

    private DynamsoftBarcode mDynamsoftBarcode;
    private ZXingBarcode mZXingBarcode;

    @Autowired
    public BarcodeController(DynamsoftBarcode dynamsoft, ZXingBarcode zxing) {
        mDynamsoftBarcode = dynamsoft;
        mZXingBarcode = zxing;
    }

    @PostMapping(value = "/api/dynamsoft"
            , consumes = MediaType.MULTIPART_FORM_DATA_VALUE
            , produces = MediaType.APPLICATION_JSON_VALUE)
    public BarcodeResponse getDynamsoft(@RequestPart MultipartFile file) throws Exception {
        return mDynamsoftBarcode.decode(file.getOriginalFilename(), file.getInputStream());
    }

    @PostMapping(value = "/api/zxing"
            , consumes = MediaType.MULTIPART_FORM_DATA_VALUE
            , produces = MediaType.APPLICATION_JSON_VALUE)
    public BarcodeResponse getZXing(@RequestPart MultipartFile file) throws Exception {
        return mZXingBarcode.decode(file.getOriginalFilename(), file.getInputStream());
    }
}
