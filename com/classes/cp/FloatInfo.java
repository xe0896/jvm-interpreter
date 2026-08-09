package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

public class FloatInfo extends CpInfo {
    public final float value;

    FloatInfo(DataInputStream stream) throws IOException {
        super(4);
        value = stream.readFloat();
    }
}
