cmake_minimum_required(VERSION 3.22.1)
project("forestgame")

add_library(forestgame SHARED
    GameRenderer.cpp
    NativeBridge.cpp)

find_library(log_lib log REQUIRED)
find_library(android_lib android REQUIRED)
find_library(gles_lib GLESv2 REQUIRED)
find_library(cpp_shared_lib c++_shared REQUIRED)
find_library(atomic_lib atomic)

set(FORESTGAME_DEPENDENCIES
    ${log_lib}
    ${android_lib}
    ${gles_lib}
    ${cpp_shared_lib})

if (atomic_lib)
    list(APPEND FORESTGAME_DEPENDENCIES ${atomic_lib})
else()
    message(STATUS "libatomic not provided for ${ANDROID_ABI}; relying on toolchain-built atomics")
endif()

target_include_directories(forestgame
    PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR})

target_compile_features(forestgame PRIVATE cxx_std_17)

target_link_libraries(forestgame PRIVATE ${FORESTGAME_DEPENDENCIES})
