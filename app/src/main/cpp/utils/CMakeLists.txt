cmake_minimum_required(VERSION 3.10)

project(utils)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)

add_library(utils STATIC
        elfsym/ElfView.cpp elfsym/ProcessView.cpp log/Log.cpp log/AndroidLog.cpp
        auto_close_fd.cpp FileMemMap.cpp io_utils.cpp ProcessUtils.cpp shared_memory.cpp TextUtils.cpp Uuid.cpp)

if ("${CMAKE_CXX_COMPILER_ID}" MATCHES "Clang")
    SET(CLANG_CXX_EXTRA_OPT "-Werror=unknown-warning-option -Werror=format-invalid-specifier -Werror=call-to-pure-virtual-from-ctor-dtor")
    SET(CLANG_C_EXTRA_OPT "-Werror=format-invalid-specifier")
else ()
    SET(CLANG_CXX_EXTRA_OPT "")
    SET(CLANG_C_EXTRA_OPT "")
endif ()
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${CLANG_CXX_EXTRA_OPT} -fPIC -Werror=delete-non-virtual-dtor -Werror=return-type -Werror=non-virtual-dtor -Wno-invalid-offsetof")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${CLANG_C_EXTRA_OPT} -fPIC -Werror=return-type")

set_target_properties(utils PROPERTIES
        CXX_EXTENSIONS OFF
        POSITION_INDEPENDENT_CODE ON
        )

target_link_libraries(utils)
