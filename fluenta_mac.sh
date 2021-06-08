#!/bin/sh

cd "$(dirname "$0")/"

OPTIONS=" -Xmx1500m -Xdock:name=Fluenta -XstartOnFirstThread "
CLASSPATH="lib/dtd.jar:lib/fluenta.jar:lib/h2-1.4.200.jar:lib/json.jar:lib/jsoup.jar:lib/mapdb.jar:lib/openxliff.jar:lib/tmengine.jar:lib/widgets.jar:lib/mac64/swt.jar"
JAVA="$JAVA_HOME/bin/java"

${JAVA} ${OPTIONS} -cp ${CLASSPATH} com.maxprograms.fluenta.Fluenta