@echo off
set CLASSPATH=
FOR %%i IN ("%~dp0lib\*.jar") DO CALL "%~dp0setclasspath.bat" %%i
echo %CLASSPATH%
java -cp "%CLASSPATH%" pxb.android.dex2jar.v3.Main %*
