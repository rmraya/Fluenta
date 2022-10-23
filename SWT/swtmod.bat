
jdeps --generate-module-info . .\swt.jar
javac --patch-module swt=swt.jar .\swt\module-info.java
cd swt
jar uf ..\swt.jar .\module-info.class