#!/bin/sh
java -jar ~/.m2/repository/de/jflex/jflex/1.4.3/jflex-1.4.3.jar -d .. TFL.flex
java -jar ~/.m2/repository/net/sf/squirrel-sql/thirdparty-non-maven/java-cup/0.11a/java-cup-0.11a.jar -destdir .. TFL.cup
