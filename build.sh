#!/bin/sh
javac HTTPExtensionExample.java Arduino.java
jar -cfm HTTPExtensionExample.jar manifest.mf *.class
rm *.class 
