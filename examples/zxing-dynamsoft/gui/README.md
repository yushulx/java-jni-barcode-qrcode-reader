# ZXing vs Dynamsoft Barcode Reader in Java (Swing GUI)

## Usage

1. Obtain a 30-day free [trial license](https://www.dynamsoft.com/customer/license/trialLicense/?product=dcv&package=cross-platform) and export it as the `DBR_LICENSE_KEY` environment variable:

    ```bash
    :: Windows
    set DBR_LICENSE_KEY=your-trial-license-key

    # Linux / macOS
    export DBR_LICENSE_KEY=your-trial-license-key
    ```

2. Build and launch:

    ```bash
    mvn clean package
    java -jar target/test-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```

    Click **Load File** to pick an image, then use the drop-down list to switch between **ZXing** and **Dynamsoft**.

    You can also pass an image path as the first argument to auto-run both engines on startup and print a side-by-side comparison in the text area:

    ```bash
    java -jar target/test-1.0-SNAPSHOT-jar-with-dependencies.jar ../../../../images/AllSupportedBarcodeTypes.png
    ```

    ![Java barcode GUI](https://www.dynamsoft.com/codepool/img/2026/08/java-barcode-gui.png)
