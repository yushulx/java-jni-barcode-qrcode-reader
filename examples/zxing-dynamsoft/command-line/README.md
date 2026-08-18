# ZXing vs Dynamsoft Barcode Reader in Java (Command Line)

## Usage

1. Obtain a 30-day free [trial license](https://www.dynamsoft.com/customer/license/trialLicense/?product=dcv&package=cross-platform) and export it as the `DBR_LICENSE_KEY` environment variable (the code falls back to the placeholder `LICENSE-KEY` if unset):

    ```bash
    :: Windows
    set DBR_LICENSE_KEY=your-trial-license-key

    # Linux / macOS
    export DBR_LICENSE_KEY=your-trial-license-key
    ```

2. Build and run:

    ```bash
    mvn clean package
    java -jar target/test-1.0-SNAPSHOT-jar-with-dependencies.jar <path-to-image>
    ```

    Example with the bundled multi-barcode test sheet:

    ```bash
    java -jar target/test-1.0-SNAPSHOT-jar-with-dependencies.jar ../../../../images/AllSupportedBarcodeTypes.png
    ```

    The program first decodes the image with ZXing, then with Dynamsoft Barcode Reader 11.x (`CaptureVisionRouter`), and prints both result lists with timings:

    ![Java barcode command line](https://www.dynamsoft.com/codepool/img/2026/08/java-barcode-command-line.png)
