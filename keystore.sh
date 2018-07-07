#!/usr/bin/env bash

# Generate 4 keys with test0.example.com, test1...
for i in {0..3}; do
    keytool \
        -genkey \
        -storepass password \
        -alias test${i}.example.com \
        -keyalg RSA \
        -keysize 2048 \
        -keystore keys.jks \
        -dname "CN=test${i}.example.com,OU=Dept, O=CompanyName, L=CityName, ST=StateName, C=ISO-CountryCode"
done