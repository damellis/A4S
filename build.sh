#!/bin/sh
javac A4S.java Firmata.java
jar -cfm A4S.jar manifest.mf *.class
rm *.class 
