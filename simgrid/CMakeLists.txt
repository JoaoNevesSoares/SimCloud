#Configurações básicas do cmake
cmake_minimum_required(VERSION 3.12)

message(STATUS "Cmake version ${CMAKE_MAJOR_VERSION}.${CMAKE_MINOR_VERSION}.${CMAKE_PATCH_VERSION}")

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${CMAKE_HOME_DIRECTORY}/tools/cmake/Modules)

project(simcloud)

## set path for input files
set(DEPLOY_INPUT "${CMAKE_HOME_DIRECTORY}/deployment")
# Find Simgrid library
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/Modules/")
find_package(SimGrid REQUIRED)
include_directories(${SimGrid_INCLUDE_DIR})

# Find Boost library
find_package(Boost REQUIRED)
include_directories(${Boost_INCLUDE_DIRS})

# Find nlohmann_json library
find_package(nlohmann_json)
include_directories(${nlohmann_json_INCLUDE_DIRS})

# Add a function to create executables from all .cpp files in the directory
function(add_executables_from_directory target_dir)
    file(GLOB cpp_files ${target_dir}/*.cpp)
    foreach(cpp_file ${cpp_files})
        get_filename_component(executable_name ${cpp_file} NAME_WE)
        add_executable(${executable_name} ${cpp_file})
        target_link_libraries(${executable_name} ${SimGrid_LIBRARY})
    endforeach()
endfunction()

# Call the function to create executables from .cpp files in the current directory
add_executables_from_directory(${CMAKE_CURRENT_SOURCE_DIR})

message(STATUS "CMAKE_HOME_DIRECTORY: ${CMAKE_HOME_DIRECTORY}")