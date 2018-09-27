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


## Build the JNI Shared Library with CMake
Install [CMake](https://cmake.org/download/).

Edit `CMakeLists.txt`. Replace Java include and lib directories with yours.

```cmake
if (CMAKE_HOST_WIN32)
    set(WINDOWS 1)
    set(JAVA_INCLUDE "C:/Program Files/Java/jdk1.8.0_181/include")
    set(JAVA_INCLUDE_OS "C:/Program Files/Java/jdk1.8.0_181/include/win32")
elseif(CMAKE_HOST_APPLE)
    set(MACOS 1)
    set(JAVA_INCLUDE "/System/Library/Frameworks/JavaVM.framework/Headers")
    set(JAVA_INCLUDE_OS "")
elseif(CMAKE_HOST_UNIX)
    set(LINUX 1)
    set(JAVA_INCLUDE "/usr/lib/jvm/java-1.8.0-openjdk-amd64/include/")
    set(JAVA_INCLUDE_OS "/usr/lib/jvm/java-1.8.0-openjdk-amd64/include/linux")
endif()

if(WINDOWS)
    link_directories("${PROJECT_SOURCE_DIR}/platforms/win" "C:/Program Files/Java/jdk1.8.0_181/lib") 
    include_directories("${PROJECT_BINARY_DIR}" "${PROJECT_SOURCE_DIR}/include" "${PROJECT_SOURCE_DIR}" "${JAVA_INCLUDE}" "${JAVA_INCLUDE_OS}")
elseif(LINUX)
    link_directories("${PROJECT_SOURCE_DIR}/platforms/linux") 
    include_directories("${PROJECT_BINARY_DIR}" "${PROJECT_SOURCE_DIR}/include" "${PROJECT_SOURCE_DIR}" "${JAVA_INCLUDE}" "${JAVA_INCLUDE_OS}")
elseif(MACOS)
    link_directories("${PROJECT_SOURCE_DIR}/platforms/macos") 
    include_directories("${PROJECT_BINARY_DIR}" "${PROJECT_SOURCE_DIR}/include" "${PROJECT_SOURCE_DIR}" "${JAVA_INCLUDE}")
endif()
```

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
Install [Maven](https://maven.apache.org/download.cgi).

Build the jar package.

```
mvn package
```

## Use the Java Barcode Reader

```
java -cp target/barcode-1.0.0.jar com.dynamsoft.barcode.Test <image file>
```

## Blog
[How to Package JNI Shared Library into Jar File](https://www.codepool.biz/package-jni-shared-library-jar-file.html)
