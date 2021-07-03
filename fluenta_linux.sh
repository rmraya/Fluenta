#!/bin/bash

cd "$(dirname "$0")/"

OPTIONS=" -Xmx1500m"
CLASSPATH="lib/dtd.jar:lib/fluenta.jar:lib/h2-1.4.200.jar:lib/json.jar:lib/jsoup.jar:lib/mapdb.jar:lib/openxliff.jar:lib/gtk64/swt.jar"
JAVA="$JAVA_HOME/bin/java"

${JAVA} ${OPTIONS} -cp ${CLASSPATH} com.maxprograms.fluenta.Fluenta