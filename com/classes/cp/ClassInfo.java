package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

public class ClassInfo extends CpInfo {
    public final int nameIndex;

    ClassInfo(DataInputStream stream) throws IOException {
        super(7);
        nameIndex = stream.readUnsignedShort();
    }
}
