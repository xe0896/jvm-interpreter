package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

public class DynamicInfo extends CpInfo {
    public final int bootstrapMethodAttributeIndex;
    public final int nameAndTypeIndex;

    DynamicInfo(DataInputStream stream) throws IOException {
        super(17);
        bootstrapMethodAttributeIndex = stream.readUnsignedShort();
        nameAndTypeIndex = stream.readUnsignedShort();
    }
}
