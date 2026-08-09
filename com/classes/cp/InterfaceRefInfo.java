package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

public class InterfaceRefInfo extends CpInfo {
    public final int classIndex;
    public final int nameAndTypeIndex;

    InterfaceRefInfo(DataInputStream stream) throws IOException {
        super(11);
        classIndex = stream.readUnsignedShort();
        nameAndTypeIndex = stream.readUnsignedShort();
    }
}
