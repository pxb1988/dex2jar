#!/bin/sh

#
# dex2jar - A tool for converting Android .dex format to Java .class format 
# Copyright (c) 2009-2012 Panxiaobo
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

KS=~/.dex2jar.ks
KPASS=android

if [ $# -eq 0 ]; then
#    echo "Sign an android apk file use a test certificate."
    echo "Usage:  $0 /file/to.apk "
    exit 1
fi

# check if there are more than 1 arguments
if [ $# -ne 1 ]; then
   echo  "only one argument to $0 is required"
   exit 1
fi

if which jarsigner >/dev/null; then
    if [ ! -f $KS ]; then
       if which keytool >/dev/null; then
           echo "keystore $KS not exist, try to create one."
           keytool -genkeypair -alias androiddebugkey -keyalg RSA -keysize 2048 -dname "CN=android-debug" -validity 100000 -keystore $KS -storepass $KPASS -keypass $KPASS
       else
           echo "can't find keytool in your PATH"
           exit 1
       fi
    fi
    echo "sign apk using certificate in $KS"
    jarsigner -keystore $KS -storepass $KPASS -keypass $KPASS $1 androiddebugkey
else
    echo "can't find jarsigner in your PATH"
fi
