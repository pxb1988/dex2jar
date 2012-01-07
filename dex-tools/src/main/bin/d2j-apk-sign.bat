@echo off

REM
REM dex2jar - A tool for converting Android .dex format to Java .class format 
REM Copyright (c) 2009-2012 Panxiaobo
REM 
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with the License.
REM You may obtain a copy of the License at
REM 
REM      http://www.apache.org/licenses/LICENSE-2.0
REM 
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.
REM

set KS=%HOMEPATH%\.dex2jar.ks
set KPASS=android

if not exist "%1" goto apkNotFound
if not exist "%KS%" goto generateKS
goto sign

:generateKS
echo "key store %KS% not exist, try to create one."
keytool -genkeypair -alias androiddebugkey -keyalg RSA -keysize 2048 -dname "CN=android-debug" -validity 100000 -keystore "%KS%" -storepass %KPASS% -keypass %KPASS%


:sign
echo "sign apk using certificate in %KS%"
jarsigner -keystore "%KS%" -storepass %KPASS% -keypass %KPASS% %1 androiddebugkey
goto end

:apkNotFound
echo "apk %1 not found"

:end


