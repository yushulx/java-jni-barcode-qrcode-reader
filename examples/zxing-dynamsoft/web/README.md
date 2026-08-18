# ZXing vs Dynamsoft Barcode Reader in Java (Spring Boot Web)

## Usage

1. Obtain a 30-day free [trial license](https://www.dynamsoft.com/customer/license/trialLicense/?product=dcv&package=cross-platform) and export it as the `DBR_LICENSE_KEY` environment variable (Spring reads it via `@Value("${DBR_LICENSE_KEY:LICENSE-KEY}")`):

    ```bash
    :: Windows
    set DBR_LICENSE_KEY=your-trial-license-key

    # Linux / macOS
    export DBR_LICENSE_KEY=your-trial-license-key
    ```

2. Build and run the Spring Boot 3 service:

    ```bash
    mvn clean package
    java -jar target/web-1.0-SNAPSHOT.jar
    ```

3. Open **http://localhost:8080/swagger-ui/index.html** in your browser, expand **POST /api/dynamsoft** or **POST /api/zxing**, click **Try it out**, upload a barcode image, and hit **Execute**:

    ![Java barcode spring boot](https://www.dynamsoft.com/codepool/img/2026/08/java-barcode-spring-boot.png)

## Endpoints

- `POST /api/dynamsoft` — decodes the uploaded image with Dynamsoft Barcode Reader 11.x (`CaptureVisionRouter.capture(byte[], PT_READ_BARCODES)`)
- `POST /api/zxing` — decodes the uploaded image with ZXing `GenericMultipleBarcodeReader`

Both endpoints consume `multipart/form-data` and return a JSON payload with the filename, decoded texts, symbology formats, and an optional `error` field.
