cmake_minimum_required (VERSION 2.6)
project (dbr)
MESSAGE( STATUS "PROJECT_NAME: " ${PROJECT_NAME} )

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

MESSAGE( STATUS "JAVA_INCLUDE: " ${JAVA_INCLUDE})

# Check platforms
if (CMAKE_HOST_WIN32)
    set(WINDOWS 1)
elseif(CMAKE_HOST_APPLE)
    set(MACOS 1)
elseif(CMAKE_HOST_UNIX)
    set(LINUX 1)
endif()

# Set RPATH
if(CMAKE_HOST_UNIX)
    if(CMAKE_HOST_APPLE)
        SET(CMAKE_CXX_FLAGS "-std=c++11 -O3 -Wl,-rpath,@loader_path")
        SET(CMAKE_INSTALL_RPATH "@loader_path")
    else()
        SET(CMAKE_CXX_FLAGS "-std=c++11 -O3 -Wl,-rpath=$ORIGIN")
        SET(CMAKE_INSTALL_RPATH "$ORIGIN")
    endif()
    SET(CMAKE_INSTALL_RPATH_USE_LINK_PATH TRUE)
endif()

# Add search path for include and lib files
if(WINDOWS)
    if(CMAKE_CXX_COMPILER_ID STREQUAL "GNU")
        link_directories("${PROJECT_SOURCE_DIR}/platforms/win/bin/" ${JNI_LIBRARIES}) 
    else()
        link_directories("${PROJECT_SOURCE_DIR}/platforms/win/lib/" ${JNI_LIBRARIES}) 
    endif()
elseif(LINUX)
    if (CMAKE_SYSTEM_PROCESSOR STREQUAL x86_64)
        MESSAGE( STATUS "Link directory: ${PROJECT_SOURCE_DIR}/platforms/linux/" )
        link_directories("${PROJECT_SOURCE_DIR}/platforms/linux/" ${JNI_LIBRARIES})
    elseif(CMAKE_SYSTEM_PROCESSOR STREQUAL armv7l OR ARM32_BUILD)
        MESSAGE( STATUS "Link directory: ${PROJECT_SOURCE_DIR}/platforms/arm32/" )
        link_directories("${PROJECT_SOURCE_DIR}/platforms/arm32/" ${JNI_LIBRARIES}) 
    elseif(CMAKE_SYSTEM_PROCESSOR STREQUAL aarch64) 
        MESSAGE( STATUS "Link directory: ${PROJECT_SOURCE_DIR}/platforms/aarch64/" )
        link_directories("${PROJECT_SOURCE_DIR}/platforms/aarch64/" ${JNI_LIBRARIES}) 
    endif()
elseif(MACOS)
    MESSAGE( STATUS "Link directory: ${PROJECT_SOURCE_DIR}/platforms/macos/" )
    link_directories("${PROJECT_SOURCE_DIR}/platforms/macos/" ${JNI_LIBRARIES}) 
endif()
include_directories("${PROJECT_BINARY_DIR}" "${PROJECT_SOURCE_DIR}/include/")


# Add the library
add_library(dbr SHARED NativeBarcodeReader.cxx)
if(MACOS)
    set_target_properties(dbr PROPERTIES SUFFIX ".jnilib")
endif()
if(WINDOWS)
    if(CMAKE_CXX_COMPILER_ID STREQUAL "GNU")
        target_link_libraries (${PROJECT_NAME} "DynamsoftBarcodeReaderx64")
    else()
        if(CMAKE_CL_64)
            target_link_libraries (${PROJECT_NAME} "DBRx64")
        else()
            target_link_libraries (${PROJECT_NAME} "DBRx86")
        endif()
    endif()
else()
    target_link_libraries (${PROJECT_NAME} "DynamsoftBarcodeReader" pthread)
endif()

if(WINDOWS)
    add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD 
    COMMAND ${CMAKE_COMMAND} -E copy_directory
    "${PROJECT_SOURCE_DIR}/platforms/win/bin/"      
    $<TARGET_FILE_DIR:dbr>)
endif()

# Set installation directory
set(CMAKE_INSTALL_PREFIX "${PROJECT_SOURCE_DIR}/../src/main/")
set(ECLIPSE_PATH "java/com/dynamsoft/barcode/native")
set(MAVEN_PATH "resources/com/dynamsoft/barcode/native")
if(WINDOWS)
    install (DIRECTORY "${PROJECT_SOURCE_DIR}/platforms/win/bin/" DESTINATION "${CMAKE_INSTALL_PREFIX}${ECLIPSE_PATH}/win")
    install (DIRECTORY "${PROJECT_SOURCE_DIR}/platforms/win/bin/" DESTINATION "${CMAKE_INSTALL_PREFIX}${MAVEN_PATH}/win")
    install (TARGETS dbr DESTINATION "${CMAKE_INSTALL_PREFIX}${ECLIPSE_PATH}/win")
    install (TARGETS dbr DESTINATION "${CMAKE_INSTALL_PREFIX}${MAVEN_PATH}/win")
elseif(LINUX)
    install (DIRECTORY "${PROJECT_SOURCE_DIR}/platforms/linux" DESTINATION "${CMAKE_INSTALL_PREFIX}${ECLIPSE_PATH}/linux")
    install (DIRECTORY "${PROJECT_SOURCE_DIR}/platforms/linux" DESTINATION "${CMAKE_INSTALL_PREFIX}${MAVEN_PATH}/linux")
    install (TARGETS dbr DESTINATION "${CMAKE_INSTALL_PREFIX}${ECLIPSE_PATH}/linux")
    install (TARGETS dbr DESTINATION "${CMAKE_INSTALL_PREFIX}${MAVEN_PATH}/linux")
elseif(MACOS)
    install (DIRECTORY "${PROJECT_SOURCE_DIR}/platforms/macos" DESTINATION "${CMAKE_INSTALL_PREFIX}${ECLIPSE_PATH}/macos")
    install (DIRECTORY "${PROJECT_SOURCE_DIR}/platforms/macos" DESTINATION "${CMAKE_INSTALL_PREFIX}${MAVEN_PATH}/macos")
    install (TARGETS dbr DESTINATION "${CMAKE_INSTALL_PREFIX}${ECLIPSE_PATH}/macos")
    install (TARGETS dbr DESTINATION "${CMAKE_INSTALL_PREFIX}${MAVEN_PATH}/macos")
endif()



