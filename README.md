# DBR Java for Windows, Linux and macOS
Version 6.3

The repository aims to help developers build a cross-platform Java jar package that contains JNI shared libraries (`Windows`, `Linux` and `macOS`) and Dynamsoft Barcode Reader.

## License
Get the [trial license](https://www.dynamsoft.com/CustomerPortal/Portal/Triallicense.aspx).

## Contact Us
<support@dynamsoft.com>

## SDK
Download [Dynamsoft Barcode Reader for Windows, Linux and macOS](https://www.dynamsoft.com/Downloads/Dynamic-Barcode-Reader-Download.aspx).

Copy OS-dependent shared libraries to `jni/platforms` folder.

* jni/platforms/win
    - `DBRx64.lib`
    - `DynamicPdfx64.dll`
    - `DynamsoftBarcodeReaderx64.dll`
    - `vcomp110.dll`

* jni/platforms/linux
    - `libDynamicPdf.so`
    - `libDynamsoftBarcodeReader.so`

* jni/platforms/macos
    - `libDynamsoftBarcodeReader.dylib`


## Build the JNI Shared Library
### Windows
E.g. Visual Studio 2017

```
mkdir build
cd build
cmake -G"Visual Studio 15 2017 Win64" .. 
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

## Use the Java Barcode Reader

```
java -cp target/barcode-1.0.0.jar com.dynamsoft.barcode.Test <image file>
```