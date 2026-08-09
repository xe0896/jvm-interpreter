package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

public class PackageInfo extends CpInfo {
    public final int nameIndex;

    PackageInfo(DataInputStream stream) throws IOException {
        super(20);
        nameIndex = stream.readUnsignedShort();
    }
}
