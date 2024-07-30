# Java DotCode Reader
These samples demonstrate how to decode **DotCode** using the [Dynamsoft Barcode Reader SDK](https://www.dynamsoft.com/barcode-reader/downloads/).

## License Key
Get a [valid license](https://www.dynamsoft.com/customer/license/trialLicense/) and update the code in `src/main/java/com/java/barcode/App.java`:

```java
BarcodeReader.initLicense("LICENSE-KEY");
```

## Usage

- Command-line
    
  ```bash
  mvn clean package
  java -jar target/test-1.0-SNAPSHOT-jar-with-dependencies.jar ../../../../images/dotcode.png
  ```

- OpenCV
  
  1. Install the OpenCV JAR file to your local Maven repository:
  
      ```bash
      cmd
      mvn install:install-file -Dfile=opencv-430.jar -DgroupId=org -DartifactId=opencv -Dversion=4.3.0 -Dpackaging=jar
      ```
    
  2. Build and run the project.
  
      ```bash
      mvn clean package
      java -jar target/test-1.0-SNAPSHOT-jar-with-dependencies.jar
      ```

     ![Java DotCode Reader](http://www.dynamsoft.com/codepool/img/2020/04/java-dotcode-reader.png)
     
 ## Blog
 [How to Build DotCode Reader in Java on Windows 10](https://www.dynamsoft.com/codepool/java-dotcode-reader-webcam-opencv.html)
