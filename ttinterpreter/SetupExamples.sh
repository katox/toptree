#!/bin/sh

mvn clean package
rm -f examples/lib/*.jar examples/ttinterpreter.jar
cp -r target/lib/*.jar examples/lib
cp target/ttinterpreter.jar examples
