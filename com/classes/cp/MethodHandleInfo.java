package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

public class MethodHandleInfo extends CpInfo {
    public final int referenceKind;
    public final int referenceIndex;

    MethodHandleInfo(DataInputStream stream) throws IOException {
        super(15);
        referenceKind = stream.readUnsignedByte();
        referenceIndex = stream.readUnsignedShort();
    }
}
