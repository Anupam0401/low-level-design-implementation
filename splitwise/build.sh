#!/bin/bash

# Build and run script for Splitwise application

echo "Building Splitwise application..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "Build successful! Starting application..."
    java -jar target/splitwise-1.0-SNAPSHOT.jar
else
    echo "Build failed. Please check the errors above."
    exit 1
fi
