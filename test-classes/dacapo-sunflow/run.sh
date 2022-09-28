#!/bin/bash

\rm -rf p1-with-threading/ p2-with-threading/
echo "*********************** PASS 1 ************************"
java -jar vasco-1.1-SNAPSHOT-complete.jar -in boosted -out p1-with-threading Harness | tee out-p1

echo "*********************** PASS 2 ************************"
java -jar vasco-1.1-SNAPSHOT-complete.jar -in p1-with-threading/sootified -out p2-with-threading Harness | tee out-p2
echo "*********************** DONE * ************************"

