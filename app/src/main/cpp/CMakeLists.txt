cmake_minimum_required(VERSION 3.10)
project(tmoe)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 20)

if ("${CMAKE_CXX_COMPILER_ID}" MATCHES "GNU")
    find_library( # Defines the name of the path variable that stores the
            # location of the NDK library.
            ANDROID_LIBS
            # Specifies the name of the NDK library that
            # CMake needs to locate.
            pthread)
else ()

    find_library( # Defines the name of the path variable that stores the
            # location of the NDK library.
            ANDROID_LIBS
            # Specifies the name of the NDK library that
            # CMake needs to locate.
            log)
endif ()

if ("${CMAKE_CXX_COMPILER_ID}" MATCHES "GNU")
    include_directories("/usr/lib/jvm/java-8-openjdk/include")
    include_directories("/usr/lib/jvm/java-8-openjdk/include/linux")
endif ()

if ("${CMAKE_CXX_COMPILER_ID}" MATCHES "Clang")
    SET(CLANG_CXX_EXTRA_OPT "-Werror=unknown-warning-option -Werror=format-invalid-specifier -Werror=call-to-pure-virtual-from-ctor-dtor")
    SET(CLANG_C_EXTRA_OPT "-Werror=format-invalid-specifier")
else ()
    SET(CLANG_CXX_EXTRA_OPT "")
    SET(CLANG_C_EXTRA_OPT "")
endif ()
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${CLANG_CXX_EXTRA_OPT} -fPIC -Werror=delete-non-virtual-dtor -Werror=return-type -Werror=non-virtual-dtor -Wno-invalid-offsetof")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${CLANG_C_EXTRA_OPT} -fPIC -Werror=return-type")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--no-allow-shlib-undefined,--no-undefined")

set(CMAKE_LINK_LIBRARY_FLAG " -Wl,-e,__libtmoe_main ${CMAKE_LINK_LIBRARY_FLAG}")

include_directories(.)

add_library(tmoe SHARED tmoe_core/version.c tmoe_core/NativeInit.cpp tmoe_core/TMoeJNI.cpp)
target_compile_definitions(tmoe PRIVATE TMOE_VERSION=\"${TMOE_VERSION}\")
set_target_properties(tmoe PROPERTIES
        CXX_EXTENSIONS OFF
        POSITION_INDEPENDENT_CODE ON
        )

add_subdirectory(utils)

target_link_libraries(tmoe c dl utils ${ANDROID_LIBS})
