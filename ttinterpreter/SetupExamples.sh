#!/bin/sh

mvn clean package
cp -r target/lib examples/lib
cp target/ttinterpreter-1.0-SNAPSHOT.jar examples
