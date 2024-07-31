# ZXing vs Dynamsoft Barcode Reader in Java

## Usage
1. Obtain a valid [license key](https://www.dynamsoft.com/customer/license/trialLicense) and update the code in `src/main/java/com/java/barcode/App.java`:

    ```java
    BarcodeReader.initLicense("LICENSE-KEY");
    ```

2. Run the following commands in your command-line tools:

    ```bash
    mvn clean package
    java -jar target/test-1.0-SNAPSHOT-jar-with-dependencies.jar ../../../../images/AllSupportedBarcodeTypes.png
    ```

    ![Java barcode command line](http://www.dynamsoft.com/codepool/img/2020/03/java-barcode-command-line.png)
