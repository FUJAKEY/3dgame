cmake_minimum_required(VERSION 3.22.1)
project("forestgame")

add_library(forestgame SHARED
    GameRenderer.cpp
    NativeBridge.cpp)

find_library(log-lib log)
find_library(android-lib android)
find_library(gles-lib GLESv2)
find_library(atomic-lib atomic)
find_library(cpp-shared-lib c++_shared)

if (NOT atomic-lib)
    message(FATAL_ERROR "Failed to locate libatomic; required for std::atomic operations")
endif()

if (NOT cpp-shared-lib)
    message(FATAL_ERROR "Failed to locate libc++_shared; ensure the NDK is installed correctly")
endif()

set(FORESTGAME_DEPENDENCIES
    ${log-lib}
    ${android-lib}
    ${gles-lib})

list(APPEND FORESTGAME_DEPENDENCIES ${atomic-lib})
list(APPEND FORESTGAME_DEPENDENCIES ${cpp-shared-lib})

target_include_directories(forestgame
    PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR})

target_compile_features(forestgame PRIVATE cxx_std_17)

target_link_libraries(forestgame PRIVATE ${FORESTGAME_DEPENDENCIES})
