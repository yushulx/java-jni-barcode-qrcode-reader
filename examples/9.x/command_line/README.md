# Multiple 1D/2D Barcode Detection in Java

This sample demonstrates how to use the [Dynamsoft Barcode Reader SDK](https://www.dynamsoft.com/barcode-reader/sdk-desktop-server/) to build a simple Java barcode and QR code reader on **Windows**, **Linux (AMD64 and ARM64)**, and **macOS**.

## Usage
1. Obtain a valid [license key](https://www.dynamsoft.com/customer/license/trialLicense) and update the code in `src/main/java/com/dynamsoft/App.java`:

    ```java
    BarcodeReader.initLicense("LICENSE-KEY");
    ```

2. Run the following commands in your command-line tools:

    ```bash
    mvn clean package
    java -jar target/test-1.0-SNAPSHOT-jar-with-dependencies.jar ../../../images/AllSupportedBarcodeTypes.png
    ```

    ![Java barcode and QR code reader](https://www.dynamsoft.com/codepool/img/2022/03/arm64-jetson-nano-java-barcode.png)

## Docker Linux Container
Build and Run Docker Container:

```
docker rmi dynamsoft/barcode-reader -f
docker build -t dynamsoft/barcode-reader -f Dockerfile .
docker run -it dynamsoft/barcode-reader
```

![Java barcode reader in Docker](https://www.dynamsoft.com/codepool/img/2020/02/java-barcode-reader-docker.png)
    

## Blog
[How to Use Dynamsoft Java Barcode Reader to Scan Multiple Barcodes](https://www.dynamsoft.com/codepool/java-barcode-reader-scan-multiple.html)
