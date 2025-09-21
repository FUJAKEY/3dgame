cmake_minimum_required(VERSION 3.22.1)
project("forestgame")

add_library(forestgame SHARED
    GameRenderer.cpp
    NativeBridge.cpp)

find_library(log-lib log)
find_library(android-lib android)
find_library(gles-lib GLESv2)

target_include_directories(forestgame
    PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR})

target_compile_features(forestgame PRIVATE cxx_std_17)

target_link_libraries(
    forestgame
    ${log-lib}
    ${android-lib}
    ${gles-lib})
