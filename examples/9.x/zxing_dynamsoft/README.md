# ZXing & Dynamsoft Java Barcode Reader: Command Line, GUI and Web

This repository contains three samples (command line, GUI, and web) demonstrating how to implement a Java barcode and QR code reader using [ZXing](https://github.com/zxing/zxing) and [Dynamsoft Barcode Reader](https://www.dynamsoft.com/barcode-reader/sdk-desktop-server/).

## Install ZXing and Dynamsoft Barcode Reader in Maven Project
To set up your Maven project, configure the `pom.xml` file to include the necessary repositories and dependencies:

```xml
<repositories>
<repository>
    <id>dbr</id>
    <url>https://download2.dynamsoft.com/maven/dbr/jar</url>
</repository>
</repositories>
<dependencies>
<dependency>
    <groupId>com.dynamsoft</groupId>
    <artifactId>dbr</artifactId>
    <version>9.6.40.1</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.4.0</version>
</dependency>
```

## License Key
Get a valid [license key](https://www.dynamsoft.com/customer/license/trialLicense?product=dbr) for Dynamsoft Barcode Reader and update your Java code as follows:

```java
BarcodeReader.initLicense("LICENSE-KEY");
BarcodeReader br = new BarcodeReader();
```

## Usage
- [Command Line](command-line/README.md)
- [GUI](gui/README.md)
- [Web](web/README.md)

## Blog
[How to Integrate Java Barcode SDK to Command-Line, GUI and Web Apps](https://www.dynamsoft.com/codepool/java-barcode-command-line-gui-web.html)
