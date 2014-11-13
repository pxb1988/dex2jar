**Project move to [SourceForge](https://sourceforge.net/p/dex2jar) and [Bitbucket](https://bitbucket.org/pxb1988/dex2jar)**

My googlecode account is banned, and seams it is not possible to recover.

| _ | Home | Wiki | Downloads | Issues |
|--:|:-----|:----:|:---------:|:------:|
| sf | https://sourceforge.net/p/dex2jar | [Wiki](https://sourceforge.net/p/dex2jar/wiki) | [Downloads](https://sourceforge.net/projects/dex2jar/files/) | [Tickets](https://sourceforge.net/p/dex2jar/tickets/) |
| bb | https://bitbucket.org/pxb1988/dex2jar |[Wiki](https://bitbucket.org/pxb1988/dex2jar/wiki)| [Downloads](https://bitbucket.org/pxb1988/dex2jar/downloads) | |
| gc | https://code.google.com/p/dex2jar | [old](http://code.google.com/p/dex2jar/w/list) | [old](http://code.google.com/p/dex2jar/downloads/list) | [old](http://code.google.com/p/dex2jar/issues/list)|


#dex2jar
Tools to work with android .dex and java .class files

1. dex-reader/writer:
    Read/write the Dalvik Executable (.dex) file. It has a [light weight API similar with ASM](Faq#markdown-header-want-to-read-dex-file-using-dex2jar).
2. d2j-dex2jar:
    Convert .dex file to .class files (zipped as jar)
3. smali/baksmali:
    disassemble dex to smali files and assemble dex from smali files. different implementation to [smali/baksmali](http://code.google.com/p/smali), same syntax, but we support escape in type desc "Lcom/dex2jar\t\u1234;"
4. other tools:
    [d2j-decrypt-string](DecryptStrings)

## Need help ?
send email to dex2jar@googlegroup.com 

or post at https://sourceforge.net/p/dex2jar/tickets/

## License
[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

