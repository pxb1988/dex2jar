@echo off
set CLASSPATH=
FOR %%i IN ("lib\*.jar") DO CALL "setclasspath.bat" %%i
echo %CLASSPATH%
java -cp "%CLASSPATH%" pxb.android.dex2jar.dump.Dump %*
