#!/bin/sh

_classpath="."
for k in lib/*.jar
do
 _classpath="${_classpath}:${k}"
done
java  -classpath "${_classpath}" "pxb.android.dex2jar.v3.Main" $1 $2 $3 $4 $5 $6
