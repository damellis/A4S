#!/bin/sh
javac HTTPExtensionExample.java
jar -cfm HTTPExtensionExample.jar manifest.mf *.class
rm *.class 