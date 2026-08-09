package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

public class NameAndTypeInfo extends CpInfo {
    public final int nameIndex;
    public final int descriptorIndex;

    NameAndTypeInfo(DataInputStream stream) throws IOException {
        super(12);
        nameIndex = stream.readUnsignedShort();
        descriptorIndex = stream.readUnsignedShort();
    }
}
