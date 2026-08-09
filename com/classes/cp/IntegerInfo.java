package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

public class IntegerInfo extends CpInfo {
    public final int value;

    IntegerInfo(DataInputStream stream) throws IOException {
        super(3);
        value = stream.readInt();
    }
}
