**Project move to [SourceForge](https://sourceforge.net/p/dex2jar) and [Bitbucket](https://bitbucket.org/pxb1988/dex2jar) and [Github](https://github.com/pxb1988/dex2jar)**

| _ | Mirror | Wiki | Downloads | Issues |
|--:|:-----|:----:|:---------:|:------:|
| sf | https://sourceforge.net/p/dex2jar | [Wiki](https://sourceforge.net/p/dex2jar/wiki) | [Downloads](https://sourceforge.net/projects/dex2jar/files/) | [Tickets](https://sourceforge.net/p/dex2jar/tickets/) |
| bb | https://bitbucket.org/pxb1988/dex2jar |[Wiki](https://bitbucket.org/pxb1988/dex2jar/wiki)| [Downloads](https://bitbucket.org/pxb1988/dex2jar/downloads) | [Issues](https://bitbucket.org/pxb1988/dex2jar/issues) |
| gh | https://github.com/pxb1988/dex2jar | | | [Issues](https://github.com/pxb1988/dex2jar/issues) |
| gc | https://code.google.com/p/dex2jar | [old](http://code.google.com/p/dex2jar/w/list) | [old](http://code.google.com/p/dex2jar/downloads/list) | [old](http://code.google.com/p/dex2jar/issues/list)|


#dex2jar
Tools to work with android .dex and java .class files

1. dex-reader/writer:
    Read/write the Dalvik Executable (.dex) file. It has a [light weight API similar with ASM](https://sourceforge.net/p/dex2jar/wiki/Faq#markdown-header-want-to-read-dex-file-using-dex2jar).
2. d2j-dex2jar:
    Convert .dex file to .class files (zipped as jar)
3. smali/baksmali:
    disassemble dex to smali files and assemble dex from smali files. different implementation to [smali/baksmali](http://code.google.com/p/smali), same syntax, but we support escape in type desc "Lcom/dex2jar\t\u1234;"
4. other tools:
    [d2j-decrypt-string](https://sourceforge.net/p/dex2jar/wiki/DecryptStrings)

## Need help ?
send email to dex2jar@googlegroups.com 

or post on issue trackers list above.

## License
[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

