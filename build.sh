#!/bin/sh
javac -classpath RXTXcomm.jar A4S.java processing/src/Firmata.java
mkdir -p org/firmata
cp processing/src/*.class org/firmata/
jar -cfm A4S.jar manifest.mf *.class org/firmata/*.class
rm *.class org/firmata/*.class
