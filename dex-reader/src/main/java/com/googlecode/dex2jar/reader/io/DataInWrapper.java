package com.googlecode.dex2jar.reader.io;


public class DataInWrapper implements DataIn {
    private DataIn in;

    public DataInWrapper(DataIn in) {
        super();
        this.in = in;
    }

    @Override
    public int getCurrentPosition() {
        return in.getCurrentPosition();
    }

    @Override
    public void move(int absOffset) {
        in.move(absOffset);
    }

    @Override
    public void pop() {
        in.pop();
    }

    @Override
    public void push() {
        in.push();
    }

    @Override
    public void pushMove(int absOffset) {
        in.pushMove(absOffset);
    }

    @Override
    public int readByte() {
        return in.readByte();
    }

    @Override
    public byte[] readBytes(int size) {
        return in.readBytes(size);
    }

    @Override
    public int readIntx() {
        return in.readIntx();
    }

    @Override
    public int readUIntx() {
        return in.readUIntx();
    }

    @Override
    public int readShortx() {
        return in.readShortx();
    }

    @Override
    public int readUShortx() {
        return in.readUShortx();
    }

    @Override
    public long readLeb128() {
        return in.readLeb128();
    }

    @Override
    public int readUByte() {
        return in.readUByte();
    }

    @Override
    public long readULeb128() {
        return in.readULeb128();
    }

    @Override
    public void skip(int bytes) {
        in.skip(bytes);
    }

}
