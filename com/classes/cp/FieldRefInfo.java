package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

public class FieldRefInfo extends CpInfo {
    public final int classIndex;
    public final int nameAndTypeIndex;

    FieldRefInfo(DataInputStream stream) throws IOException {
        super(9);
        classIndex = stream.readUnsignedShort();
        nameAndTypeIndex = stream.readUnsignedShort();
    }
}
