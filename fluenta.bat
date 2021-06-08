@echo off

set CLASSPATH="lib\dtd.jar;lib\fluenta.jar;lib\h2-1.4.200.jar;lib\json.jar;lib\jsoup.jar;lib\mapdb.jar;lib\openxliff.jar;lib\tmengine.jar;lib\widgets.jar;lib\win64\swt.jar"

start javaw.exe  -cp %CLASSPATH% com.maxprograms.fluenta.Fluenta


