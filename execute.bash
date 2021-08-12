#!/bin/bash

java -jar CSGOCaseStatsViewer-1.0.5-jar-with-dependencies.jar
err_code=$?

if [ $err_code -eq 1 ]; then
    echo "It looks like you cloned the repository instead of downloading the newest release."
    echo "Download it from https://github.com/cantryDev/CSGOCaseStatsViewer/releases/latest"
    echo "You will be redirected to the download page in 10 seconds.."
    sleep 10
    xdg-open https://github.com/cantryDev/CSGOCaseStatsViewer/releases/latest
elif [ $err_code -eq 127 ]; then 
    # If the java command isn't found
    echo "You need to have Java 1.8 or higher installed on your system to use this program"
elif [ $err_code -eq 130 ]; then
    # If we kill the program with Ctrl + C
    echo ""
    echo "Thanks for using me ! :)"
fi