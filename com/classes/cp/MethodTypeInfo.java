package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

public class MethodTypeInfo extends CpInfo {
    public final int descriptorIndex;

    MethodTypeInfo(DataInputStream stream) throws IOException {
        super(16);
        descriptorIndex = stream.readUnsignedShort();
    }
}
