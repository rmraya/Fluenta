#!/bin/sh
cd "$(dirname "$0")/"
bin/java -cp "lib/h2-1.4.200.jar" --module-path lib -m fluenta/com.maxprograms.fluenta.CLI $@