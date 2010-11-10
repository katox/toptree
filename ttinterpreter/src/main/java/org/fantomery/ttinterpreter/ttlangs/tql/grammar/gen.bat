java -jar ..\..\..\lib\JFlex.jar -d ..\ -nobak TQL.flex
java -jar ..\..\..\lib\java-cup-11a.jar -destdir ..\ TQL.cup

@ECHO OFF

SET /P =Press ENTER
