java -jar ..\..\..\lib\JFlex.jar -d ..\ -nobak TFL.flex 
java -jar ..\..\..\lib\java-cup-11a.jar -destdir ..\ TFL.cup

@ECHO OFF

SET /P =Press ENTER
