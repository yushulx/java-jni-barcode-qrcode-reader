# Barcode Qr Java SDK for Windows, Linux and macOS
The repository aims to help developers build a cross-platform Java jar package that contains JNI shared libraries (`Windows`, `Linux` and `macOS`) and [Dynamsoft Barcode Reader](https://www.dynamsoft.com/barcode-reader/overview/).

## Dynamsoft Java SDK
If you don't want to build the jar package by yourself, you can download the [Dynamsoft Java SDK](https://www.dynamsoft.com/Downloads/Dynamic-Barcode-Reader-Download.aspx) directly.

```xml
<repositories>
    <repository>
        <id>dbr</id>
        <url>
        https://download2.dynamsoft.com/maven/dbr/jar
        </url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>com.dynamsoft</groupId>
        <artifactId>dbr</artifactId>
        <version>9.4.0</version>
    </dependency>
</dependencies>
```

## License Key
Apply for a [30-day free trial license](https://www.dynamsoft.com/customer/license/trialLicense/?product=dbr).

## JNI Header Generation

```bash
cd src/main/java
javah -o ../../../jni/NativeBarcodeReader.h com.dynamsoft.barcode.NativeBarcodeReader
```

## Build the JNI Shared Library with CMake

### Windows
```
mkdir build
cd build
cmake -DCMAKE_GENERATOR_PLATFORM=x64 ..
cmake --build . --config Release --target install
```

### Linux & macOS

```
mkdir build
cd build
cmake .. 
cmake --build . --config Release --target install
```

## Build the Jar Package Using Maven

```
mvn package
```

## Test the Jar Package for Barcode Reading

```
java -cp target/barcode-1.0.0.jar com.dynamsoft.barcode.Test <image file>
```

## Blog
[How to Package JNI Shared Library into Jar File](https://www.dynamsoft.com/codepool/package-jni-shared-library-jar-file.html)
