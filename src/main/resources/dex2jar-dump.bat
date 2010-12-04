@echo off
set CLASSPATH=

FOR %%i IN ("%~dp0lib\*.jar") DO CALL "%~dp0setclasspath.bat" %%i

java -cp "%CLASSPATH%" pxb.android.dex2jar.dump.Dump %*
