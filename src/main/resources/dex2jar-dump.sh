#!/bin/sh

# copy from $Tomcat/bin/startup.sh
# resolve links - $0 may be a softlink
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`
#

_classpath="."
for k in $PRGDIR/lib/*.jar
do
 _classpath="${_classpath}:${k}"
done
java  -classpath "${_classpath}" "pxb.android.dex2jar.dump.Dump" $1 $2 $3 $4 $5 $6
