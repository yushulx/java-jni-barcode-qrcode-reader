cmake_minimum_required(VERSION 3.15)

# Project name and version
project(CameraProject VERSION 1.0 LANGUAGES CXX)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)

# Build type
if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Release)
endif()

message(STATUS "Build type: ${CMAKE_BUILD_TYPE}")
message(STATUS "Project: ${PROJECT_NAME} v${PROJECT_VERSION}")

# Define include directories
set(INCLUDE_DIR ${CMAKE_SOURCE_DIR}/include)

# Platform detection
if(WIN32)
    set(PLATFORM_NAME "windows")
elseif(APPLE)
    set(PLATFORM_NAME "macos")
elseif(UNIX)
    set(PLATFORM_NAME "linux")
else()
    set(PLATFORM_NAME "unknown")
endif()

# Architecture detection
if(CMAKE_SIZEOF_VOID_P EQUAL 8)
    set(ARCH_NAME "x86_64")
else()
    set(ARCH_NAME "x86")
endif()

message(STATUS "Target platform: ${PLATFORM_NAME}-${ARCH_NAME}")

# Compiler-specific settings
if(MSVC)
    # Set runtime library for Windows
    if(CMAKE_BUILD_TYPE STREQUAL "Debug")
        set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreadedDebug")
    else()
        set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreaded") 
    endif()
    
    # Enable parallel compilation
    add_compile_options(/MP)
    
    # Disable specific warnings
    add_compile_options(/wd4251 /wd4275)
    
    # Enable UTF-8 encoding
    add_compile_options(/utf-8)
endif()

# Define source files for the Camera library based on platform
if (WIN32)
    set(LIBRARY_SOURCES
        src/CameraWindows.cpp
        src/CameraPreviewWindows.cpp
    )
    message(STATUS "Building for Windows platform")
elseif (UNIX AND NOT APPLE)
    set(LIBRARY_SOURCES
        src/CameraLinux.cpp
        src/CameraPreviewLinux.cpp
    )
    message(STATUS "Building for Linux platform")
elseif (APPLE)
    # Support universal binaries on macOS
    set(CMAKE_OSX_ARCHITECTURES "x86_64;arm64")
    
    # Ensure that Objective-C++ source files are compiled as Objective-C++
    set(LIBRARY_SOURCES
        src/CameraMacOS.mm
        src/CameraPreviewMacOS.mm
    )
    set_source_files_properties(src/CameraMacOS.mm src/CameraPreviewMacOS.mm PROPERTIES COMPILE_FLAGS "-x objective-c++")

    # Set main.cpp to be treated as Objective-C++ for macOS
    set_source_files_properties(src/main.cpp PROPERTIES COMPILE_FLAGS "-x objective-c++")
    
    message(STATUS "Building for macOS platform with architectures: ${CMAKE_OSX_ARCHITECTURES}")
endif()

# Add JNI wrapper source (common for all platforms)
list(APPEND LIBRARY_SOURCES
    src/LiteCamJNI.cpp
)

message(STATUS "Library sources: ${LIBRARY_SOURCES}")

# Define source files for the executable
set(EXECUTABLE_SOURCES
    src/main.cpp
)

# Add the Camera shared library
add_library(litecam SHARED ${LIBRARY_SOURCES})

# Set library properties
set_target_properties(litecam PROPERTIES
    VERSION ${PROJECT_VERSION}
    SOVERSION ${PROJECT_VERSION_MAJOR}
    OUTPUT_NAME "litecam"
)

# Platform-specific library naming
if(WIN32)
    set_target_properties(litecam PROPERTIES
        PREFIX ""
        SUFFIX ".dll"
    )
elseif(APPLE)
    set_target_properties(litecam PROPERTIES
        PREFIX "lib"
        SUFFIX ".dylib"
    )
else()
    set_target_properties(litecam PROPERTIES
        PREFIX "lib"
        SUFFIX ".so"
    )
endif()

# Set include directories for the Camera library
target_include_directories(litecam PUBLIC 
    $<BUILD_INTERFACE:${INCLUDE_DIR}>
    $<INSTALL_INTERFACE:include>
)

# Define the CAMERA_EXPORTS macro for the shared library
target_compile_definitions(litecam PRIVATE 
    CAMERA_EXPORTS
    LITECAM_VERSION_MAJOR=${PROJECT_VERSION_MAJOR}
    LITECAM_VERSION_MINOR=${PROJECT_VERSION_MINOR}
    LITECAM_VERSION_PATCH=${PROJECT_VERSION_PATCH}
)

# Platform-specific dependencies for the Camera library
if (UNIX AND NOT APPLE)
    # Linux dependencies
    find_package(X11 REQUIRED)
    find_package(PkgConfig REQUIRED)
    
    # Check for Video4Linux2
    pkg_check_modules(V4L2 libv4l2)
    
    if (X11_FOUND)
        target_include_directories(litecam PUBLIC ${X11_INCLUDE_DIR})
        target_link_libraries(litecam PRIVATE ${X11_LIBRARIES} pthread)
        message(STATUS "Found X11: ${X11_LIBRARIES}")
    endif()
    
    if (V4L2_FOUND)
        target_include_directories(litecam PRIVATE ${V4L2_INCLUDE_DIRS})
        target_link_libraries(litecam PRIVATE ${V4L2_LIBRARIES})
        message(STATUS "Found Video4Linux2: ${V4L2_LIBRARIES}")
    else()
        message(WARNING "Video4Linux2 not found - camera functionality may be limited")
    endif()
    
elseif (APPLE)
    # macOS dependencies
    find_library(COCOA_LIBRARY Cocoa REQUIRED)
    find_library(AVFOUNDATION_LIBRARY AVFoundation REQUIRED)
    find_library(COREMEDIA_LIBRARY CoreMedia REQUIRED)
    find_library(COREVIDEO_LIBRARY CoreVideo REQUIRED)
    find_library(OBJC_LIBRARY objc REQUIRED)

    target_link_libraries(litecam PRIVATE 
        ${COCOA_LIBRARY} 
        ${AVFOUNDATION_LIBRARY} 
        ${COREMEDIA_LIBRARY} 
        ${COREVIDEO_LIBRARY} 
        ${OBJC_LIBRARY}
    )
    
    message(STATUS "Found macOS frameworks: Cocoa, AVFoundation, CoreMedia, CoreVideo")
    
elseif (WIN32)
    # Windows dependencies
    target_link_libraries(litecam PRIVATE 
        ole32 
        uuid 
        mfplat 
        mf 
        mfreadwrite 
        mfuuid
    )
    
    message(STATUS "Linking Windows Media Foundation libraries")
endif()

# JNI support - enhanced detection
find_package(JNI)
if (JNI_FOUND)
    message(STATUS "JNI found: ${JNI_INCLUDE_DIRS}")
    target_include_directories(litecam PRIVATE ${JNI_INCLUDE_DIRS})
    target_compile_definitions(litecam PRIVATE LITECAM_JNI_ENABLED)
    
    # Add JNI libraries on some platforms
    if(WIN32)
        # Windows doesn't typically need to link JNI libraries
    elseif(APPLE)
        # macOS typically has JNI in the framework
    else()
        # Linux might need explicit JNI library linking
        if(JNI_LIBRARIES)
            target_link_libraries(litecam PRIVATE ${JNI_LIBRARIES})
        endif()
    endif()
else()
    message(WARNING "JNI not found; Java wrapper native methods won't be available.")
    message(WARNING "Set JAVA_HOME environment variable or install JDK to enable JNI support.")
endif()

# Optional: Add position independent code for shared library
set_property(TARGET litecam PROPERTY POSITION_INDEPENDENT_CODE ON)

# Add the camera_capture executable
add_executable(camera_capture ${EXECUTABLE_SOURCES})

# Set executable properties
set_target_properties(camera_capture PROPERTIES
    OUTPUT_NAME "camera_capture"
)

# Link the Camera library to the executable
target_link_libraries(camera_capture PRIVATE litecam)

# Include the shared library's headers in the executable
target_include_directories(camera_capture PRIVATE ${INCLUDE_DIR})

# For macOS, link against the frameworks for the executable too
if (APPLE)
    target_link_libraries(camera_capture PRIVATE 
        ${COCOA_LIBRARY} 
        ${AVFOUNDATION_LIBRARY} 
        ${COREMEDIA_LIBRARY} 
        ${COREVIDEO_LIBRARY} 
        ${OBJC_LIBRARY}
    )
endif()

# Installation rules (optional)
install(TARGETS litecam camera_capture
    EXPORT CameraProjectTargets
    LIBRARY DESTINATION lib
    ARCHIVE DESTINATION lib
    RUNTIME DESTINATION bin
    INCLUDES DESTINATION include
)

install(DIRECTORY ${INCLUDE_DIR}/
    DESTINATION include
    FILES_MATCHING PATTERN "*.h"
)

# Export targets for find_package support
install(EXPORT CameraProjectTargets
    FILE CameraProjectTargets.cmake
    NAMESPACE CameraProject::
    DESTINATION lib/cmake/CameraProject
)

# Generate and install package config files
include(CMakePackageConfigHelpers)

configure_package_config_file(
    "${CMAKE_CURRENT_SOURCE_DIR}/cmake/CameraProjectConfig.cmake.in"
    "${CMAKE_CURRENT_BINARY_DIR}/CameraProjectConfig.cmake"
    INSTALL_DESTINATION lib/cmake/CameraProject
)

write_basic_package_version_file(
    "${CMAKE_CURRENT_BINARY_DIR}/CameraProjectConfigVersion.cmake"
    VERSION ${PROJECT_VERSION}
    COMPATIBILITY SameMajorVersion
)

# Summary
message(STATUS "=== Configuration Summary ===")
message(STATUS "Project: ${PROJECT_NAME} v${PROJECT_VERSION}")
message(STATUS "Platform: ${PLATFORM_NAME}-${ARCH_NAME}")
message(STATUS "Build type: ${CMAKE_BUILD_TYPE}")
message(STATUS "C++ standard: ${CMAKE_CXX_STANDARD}")
message(STATUS "JNI support: ${JNI_FOUND}")
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    message(STATUS "Optimization: Enabled")
elseif(CMAKE_BUILD_TYPE STREQUAL "Debug")
    message(STATUS "Debug info: Enabled")
endif()
message(STATUS "===============================")
