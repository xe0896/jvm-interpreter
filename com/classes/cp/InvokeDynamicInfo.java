package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

public class InvokeDynamicInfo extends CpInfo {
    public final int bootstrapMethodAttributeIndex;
    public final int nameAndTypeIndex;

    InvokeDynamicInfo(DataInputStream stream) throws IOException {
        super(18);
        bootstrapMethodAttributeIndex = stream.readUnsignedShort();
        nameAndTypeIndex = stream.readUnsignedShort();
    }
}
