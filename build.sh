#!/bin/bash
cd source
./mvnw clean package
cd ../transformer
./mvnw clean package
cd ../sink
./mvnw clean package
cd ../tap
./mvnw clean package
cd ..
