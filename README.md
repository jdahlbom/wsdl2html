wsdl2html (revival version)
===========================

This is a minimally modernized version of the old wsdl2html found in Google Archive.

Updates include upgrading to the latest apache commons libraries,
switching Ant into Maven and managing dependencies and build via Maven POM.


How to build?
-------------
mvn compile test assemble:single

This should produce a "uberjar" into target/wsdl2html-VER-single-etc.jar

How to run?
-----------
java -jar target/wsdl2html-VER-single-etc.jar /your/path/to/wsdlfile.wsdl

This generates html output to outputs/TIMESTAMP/html/


