cmake_minimum_required (VERSION 2.6)
project (dbr)
MESSAGE( STATUS "PROJECT_NAME: " ${PROJECT_NAME} )

# Check platforms
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

MESSAGE( STATUS "JAVA_INCLUDE: " ${JAVA_INCLUDE})

# Set RPATH
if(WINDOWS)
else()
    set(CMAKE_INSTALL_RPATH "${CMAKE_INSTALL_PREFIX}/lib")
    MESSAGE( STATUS "CMAKE_INSTALL_RPATH: " "${CMAKE_INSTALL_PREFIX}/lib" )
    set(CMAKE_INSTALL_RPATH_USE_LINK_PATH TRUE)
endif()

# Configure a header file to pass some of the CMake settings
# to the source code
configure_file (
    "${PROJECT_SOURCE_DIR}/BarcodeReaderConfig.h.in"
    "${PROJECT_BINARY_DIR}/BarcodeReaderConfig.h"
)

# Add search path for include and lib files
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


# Add the executable
add_library(dbr SHARED NativeBarcodeReader.cxx)
if(MACOS)
    set_target_properties(dbr PROPERTIES SUFFIX ".jnilib")
endif()
if(WINDOWS)
    if(CMAKE_CL_64)
        target_link_libraries (dbr "DBRx64")
    else()
        target_link_libraries (dbr "DBRx86")
    endif()
else()
    target_link_libraries (dbr "DynamsoftBarcodeReader")
endif()

if(WINDOWS)
    # Copy DLL files to output directory
    if(CMAKE_CL_64)
        add_custom_command(TARGET dbr POST_BUILD 
        COMMAND ${CMAKE_COMMAND} -E copy_if_different
        "${PROJECT_SOURCE_DIR}/platforms/win/DynamsoftBarcodeReaderx64.dll"              
        $<TARGET_FILE_DIR:dbr>)
    else()
        add_custom_command(TARGET dbr POST_BUILD 
        COMMAND ${CMAKE_COMMAND} -E copy_if_different
        "${PROJECT_SOURCE_DIR}/platforms/win/DynamsoftBarcodeReaderx86.dll"              
        $<TARGET_FILE_DIR:dbr>)
    endif()

endif()

# Set installation directory
set(CMAKE_INSTALL_PREFIX "${PROJECT_SOURCE_DIR}/../src/main/")
set(ECLIPSE_PATH "java/com/dynamsoft/barcode/native")
set(MAVEN_PATH "resources/com/dynamsoft/barcode/native")
if(WINDOWS)
    install (DIRECTORY "${PROJECT_SOURCE_DIR}/platforms/win" DESTINATION "${CMAKE_INSTALL_PREFIX}${ECLIPSE_PATH}")
    install (DIRECTORY "${PROJECT_SOURCE_DIR}/platforms/win" DESTINATION "${CMAKE_INSTALL_PREFIX}${MAVEN_PATH}")
    install (TARGETS dbr DESTINATION "${CMAKE_INSTALL_PREFIX}${ECLIPSE_PATH}/win")
    install (TARGETS dbr DESTINATION "${CMAKE_INSTALL_PREFIX}${MAVEN_PATH}/win")
elseif(LINUX)
    install (DIRECTORY "${PROJECT_SOURCE_DIR}/platforms/linux" DESTINATION "${CMAKE_INSTALL_PREFIX}${ECLIPSE_PATH}")
    install (DIRECTORY "${PROJECT_SOURCE_DIR}/platforms/linux" DESTINATION "${CMAKE_INSTALL_PREFIX}${MAVEN_PATH}")
    install (TARGETS dbr DESTINATION "${CMAKE_INSTALL_PREFIX}${ECLIPSE_PATH}/linux")
    install (TARGETS dbr DESTINATION "${CMAKE_INSTALL_PREFIX}${MAVEN_PATH}/linux")
elseif(MACOS)
    install (DIRECTORY "${PROJECT_SOURCE_DIR}/platforms/macos" DESTINATION "${CMAKE_INSTALL_PREFIX}${ECLIPSE_PATH}")
    install (DIRECTORY "${PROJECT_SOURCE_DIR}/platforms/macos" DESTINATION "${CMAKE_INSTALL_PREFIX}${MAVEN_PATH}")
    install (TARGETS dbr DESTINATION "${CMAKE_INSTALL_PREFIX}${ECLIPSE_PATH}/macos")
    install (TARGETS dbr DESTINATION "${CMAKE_INSTALL_PREFIX}${MAVEN_PATH}/macos")
endif()

install (FILES "${PROJECT_SOURCE_DIR}/NativeBarcodeReader.h" DESTINATION "${CMAKE_INSTALL_PREFIX}${ECLIPSE_PATH}")
install (FILES "${PROJECT_SOURCE_DIR}/NativeBarcodeReader.h" DESTINATION "${CMAKE_INSTALL_PREFIX}${MAVEN_PATH}")


