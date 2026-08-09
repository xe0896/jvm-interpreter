package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

public class StringInfo extends CpInfo {
    public final int nameIndex;

    StringInfo(DataInputStream stream) throws IOException {
        super(8);
        nameIndex = stream.readUnsignedShort();
    }
}
