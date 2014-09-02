package com.googlecode.dex2jar.tools;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@BaseCmd.Syntax(cmd = "extract-odex-from-coredump", syntax = "<core.xxxx>", desc = "Extract odex from dalvik memery core dump")
public class ExtractOdexFromCoredumpCmd extends BaseCmd {
    public static void main(String... args) {
        new ExtractOdexFromCoredumpCmd().doMain(args);
    }

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length < 1) {
            throw new HelpException("<core.xxxx> is required.");
        }
        Path core = new File(remainingArgs[0]).toPath();
        try (SeekableByteChannel channel = FileChannel.open(core, StandardOpenOption.READ);) {
            List<Long> possibleOdexs = findPossibleOdexLocation(channel);
            extractDex(channel, possibleOdexs, core.getFileName().toString());
        }
    }

    private static void extractDex(SeekableByteChannel channel, List<Long> possibleOdexs, String namePrefix) throws IOException {
        int dexIndex = 0;
        ByteBuffer odexHead = ByteBuffer.allocate(0x28).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer copyBuff = ByteBuffer.allocate(512 * 1024).order(ByteOrder.LITTLE_ENDIAN);
        final int buffSize = 0x28 + 0x70;
        ByteBuffer head = ByteBuffer.allocate(buffSize).order(ByteOrder.LITTLE_ENDIAN); // odex+dex head
        for (long pos : possibleOdexs) {
            System.err.println(String.format(">> Check for %08x", pos));
            channel.position(pos);
            head.position(0);
            int c = channel.read(head);
            head.position(0);
            if (c == buffSize) {
                int version = head.getInt(4);
                if (version == 0x00363330 || version == 0x00353330) { // odexVersion
                    int dexOffset = head.getInt(8);
                    int dexLength = head.getInt(12);
                    int depsOffset = head.getInt(16);
                    int depsLength = head.getInt(20);
                    int optOffset = head.getInt(24);
                    int optLength = head.getInt(28);
                    int flags = head.getInt(32);
                    int checksum = head.getInt(36);
                    if (dexOffset != 0x28) {
                        System.err.println(String.format(">>> dex offset is not 0x28"));
                    } else {
                        int dexMagic = head.getInt(dexOffset + 0);
                        int dexVersion = head.getInt(dexOffset + 4);
                        if (dexMagic != 0x0a786564 || !(dexVersion == 0x00363330 || dexVersion == 0x00353330)) {
                            System.err.println(String.format(">>> dex magic is not dex.036 or dex.035: 0x%08x 0x%08x", dexMagic, dexVersion));
                        } else {
                            int fileSize = head.getInt(dexOffset + 32);
                            if (fileSize != dexLength) {
                                System.err.println(String.format(">>> dex file size is same with dexLength in odex %d vs %d", fileSize, dexLength));
                            } else {
                                int endian = head.getInt(dexOffset + 40);
                                if (endian != 0x12345678) {
                                    System.err.println(String.format(">>> dex endian is not 0x12345678"));
                                } else {
                                    // find new dex
                                    Path nFile = new File(String.format("%s-%02d.odex", namePrefix, dexIndex++)).toPath();
                                    System.out.println(String.format(">>>> extract 0x%08x to %s", pos, nFile));
                                    try (SeekableByteChannel channel2 = Files.newByteChannel(nFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);) {

                                        odexHead.rewind();
                                        odexHead.putInt(0x0a796564);// dey
                                        odexHead.putInt(0x00363330);// 036
                                        odexHead.putInt(0x28);
                                        odexHead.putInt(fileSize);
                                        int nDepsOffset = 0x28 + fileSize;
                                        int nDepsPadding = 0;
                                        if (nDepsOffset % 8 != 0) {
                                            nDepsPadding = 8 - (nDepsOffset % 8);
                                            nDepsOffset += nDepsPadding;
                                        }
                                        odexHead.putInt(nDepsOffset);
                                        odexHead.putInt(depsLength);
                                        int nOptOffset = nDepsOffset + depsLength;
                                        int nOptPadding = 0;
                                        if (nOptOffset % 8 != 0) {
                                            nOptPadding = 8 - (nOptOffset % 8);
                                            nOptOffset += nOptPadding;
                                        }
                                        odexHead.putInt(nOptOffset);
                                        odexHead.putInt(optLength);
                                        odexHead.putInt(flags);
                                        odexHead.putInt(checksum);
                                        odexHead.position(0);
                                        channel2.write(odexHead);

                                        // copy dex
                                        channel.position(pos + dexOffset);
                                        copy(channel, channel2, copyBuff, fileSize);

                                        if (nDepsPadding != 0) {
                                            channel2.write(ByteBuffer.allocate(nDepsPadding));
                                        }
                                        // copy deps
                                        channel.position(pos + depsOffset);
                                        copy(channel, channel2, copyBuff, depsLength);

                                        if (nOptPadding != 0) {
                                            channel2.write(ByteBuffer.allocate(nOptPadding));
                                        }
                                        // copy opts
                                        channel.position(pos + optOffset);
                                        copy(channel, channel2, copyBuff, optLength);
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private static void copy(SeekableByteChannel channel, SeekableByteChannel channel2, ByteBuffer copyBuff, int fileSize) throws IOException {
        int remain = fileSize;

        while (remain > 0) {
            copyBuff.rewind();
            copyBuff.limit(Math.min(remain, copyBuff.capacity()));
            int read = channel.read(copyBuff);
            copyBuff.position(0);
            channel2.write(copyBuff);
            remain -= read;
        }

    }

    private static List<Long> findPossibleOdexLocation(SeekableByteChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 512).order(ByteOrder.LITTLE_ENDIAN);
        IntBuffer intBuffer = buffer.asIntBuffer();
        List<Long> possibleOdexs = new ArrayList<>();
        while (true) {
            long position = channel.position();
            //System.out.printf("load @%x\n", position);
            int count = channel.read(buffer);
            if (count <= 0) {
                break;
            }
            int s = count / 4;
            for (int i = 0; i < s; i++) {
                int u4 = intBuffer.get(i);
                if (u4 == 0x0a796564) {// dey
                    if (i + 1 < s) {
                        int v4 = intBuffer.get(i + 1);
                        if (v4 == 0x00363330 || v4 == 0x00353330) {
                            possibleOdexs.add(position + 4 * i);
                            System.err.println(String.format("> Possible %08x | %08x %08x", position + i * 4, u4, v4));
                        }
                    } else {
                        possibleOdexs.add(position + 4 * i);
                        System.err.println(String.format("> Possible %08x | %08x", position + i * 4, u4));
                    }
                }
            }
            buffer.position(0);
            intBuffer.position(0);
        }
        return possibleOdexs;
    }
}
