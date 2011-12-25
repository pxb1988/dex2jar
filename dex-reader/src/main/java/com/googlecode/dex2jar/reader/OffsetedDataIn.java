package com.googlecode.dex2jar.reader;

import com.googlecode.dex2jar.reader.io.DataIn;
import com.googlecode.dex2jar.reader.io.DataInWrapper;

public class OffsetedDataIn extends DataInWrapper {

    private int offset;

    public OffsetedDataIn(DataIn in, int offset) {
        super(in);
        super.move(offset);
        this.offset = offset;
    }

    @Override
    public int getCurrentPosition() {
        return super.getCurrentPosition() - offset;
    }

    @Override
    public void move(int absOffset) {
        super.move(absOffset + offset);
    }

    @Override
    public void pop() {
        super.pop();
    }

    @Override
    public void push() {
        super.push();
    }

    @Override
    public void pushMove(int absOffset) {
        super.pushMove(absOffset + offset);
    }

}
