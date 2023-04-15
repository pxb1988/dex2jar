# dex2jar

<p align="center">
  <a href="https://www.travis-ci.com/github/ThexXTURBOXx/dex2jar"><img src="https://www.travis-ci.com/ThexXTURBOXx/dex2jar.svg?branch=develop" alt="Travis CI build status"></a>
</p>

This is [Nico Mexis'](https://github.com/ThexXTURBOXx) fork of the dex2jar project which aims to fix most issues.

## Fixed issues

* Fixed many `StringIndexOutOfBoundsException`s relating to signatures
* Fixed `TypeTransformer` related issues
* Fixed many `NullPointerException`s and other crashes
* Fixed grammar of error messages
* Update libraries (especially ASM)
* Library fat-jar can be used for other projects (Automatically produced
  by [GitHub Actions](https://github.com/ThexXTURBOXx/dex2jar/actions))
* Smali now outputs `.param` instead of the outdated `.parameter` syntax
* Reformatted and cleaned up most of the code

## Downloads

**To download the latest builds, head to the [Releases](https://github.com/ThexXTURBOXx/dex2jar/releases).**

**Note: the builds in [Releases](https://github.com/ThexXTURBOXx/dex2jar/releases) are automatically built by GitHub
Actions.**

Tools to work with android .dex and java .class files

1. dex-reader/writer:
   Read/write the Dalvik Executable (.dex) file. It features
   a [lightweight API similar with ASM](https://sourceforge.net/p/dex2jar/wiki/Faq/#want-to-read-dex-file-using-dex2jar)
   .
2. d2j-dex2jar:
   Convert .dex file to .class files (zipped as jar)
3. smali/baksmali:
   Disassemble dex to smali files and assemble dex from smali files. Different implementation
   to [smali/baksmali](http://code.google.com/p/smali), same syntax, but we support escape in type desc
   `"Lcom/dex2jar\t\u1234;"`
4. other tools:
   [d2j-decrypt-string](https://sourceforge.net/p/dex2jar/wiki/DecryptStrings)

## Usage

1. In the root directory run: `./gradlew distZip`
2. `cd dex-tools/build/distributions`
3. Unzip the file `dex-tools-2.1-SNAPSHOT.zip`
4. Run `d2j-dex2jar.sh` from the unzipped directory

### Example usage:

```shell
sh d2j-dex2jar.sh -f ~/path/to/apk_to_decompile.apk
```

And the output file will be `apk_to_decompile-dex2jar.jar`.

### Example Maven project:

See for example the infamous [Bytecode Viewer](https://github.com/Konloch/bytecode-viewer).

## Need help ?

Send an email to nico.mexis@kabelmail.de or post on the [issue tracker](https://github.com/ThexXTURBOXx/dex2jar/issues).

## License

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
