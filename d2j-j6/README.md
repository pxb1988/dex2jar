# j6 Generate dex2jar for jdk6
dex2jar 2.0 change the compile jdk to version 1.7, but there still requirement for running dex2jar on jdk6. this project is try make dex2jar runnable on jdk6.

## jdk7 vs jdk6

### the major version of class file is changed
jdk6 can not run class file build for jdk7

### new package java.nio.file
jdk7 add the java.nio.file package for file processing, and dex2jar heavy based on it, 

#### Related new Class
 * java.nio.file.\*\*

#### Related new method
 * File.toPath()


### try-with-resource
This is a good improvement for coding, i can' stop using it.
the AutoCloseable interface is used for close the resource, and addSuppressed/getSuppressed is Throwable is used for save the Exception during close resource.

#### Related new Class
 * java.lang.AutoCloseable

#### Related new method
 * Throwable.addSuppressed()
 * Throwable.getSuppressed()
 
### other improvement

#### Related new Class
 * java.nio.charset.StandardCharsets

## Solution
 * For each missing class, create a new class with prefixed 'pxb.' in this project.
 * For each missing method, create a static method in this project and use the d2j-jar-weave feature of dex2jar to static weave the code into the origianl jar.
```
r Ljava/io/File;.toPath=Lj6/Files;.toPath(Ljava/io/File;)Ljava/lang/Object;
r Ljava/lang/Throwable;.addSuppressed(Ljava/lang/Throwable;)=Lj6/Thro;.addSuppressed(Ljava/lang/Throwable;Ljava/lang/Throwable;)V;
r [Ljava/lang/Throwable;.getSuppressed()=Lj6/Thro;.getSuppressed(Ljava/lang/Throwable;)[Ljava/lang/Throwable;
```
 * Replace the following reference in original jar by the tool jarjar
```
rule java.nio.file.** pxb.@0
rule java.nio.charset.StandardCharsets pxb.java.nio.charset.StandardCharsets
rule java.lang.AutoCloseable java.io.Closeable
```
 * Modify the version of all .class file to java6
 
## Test
this the following VM have been tested (only the cmd d2j-dex2jar)

 * Oracle jdk 1.6.0_45, on 64bit linux
 * Dalvik VM on android 4.4.2 armv7a with '-Xmx512m' to increase memory


