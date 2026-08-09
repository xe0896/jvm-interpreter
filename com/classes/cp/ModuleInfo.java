package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

public class ModuleInfo extends CpInfo {
    public final int nameIndex;

    ModuleInfo(DataInputStream stream) throws IOException {
        super(19);
        nameIndex = stream.readUnsignedShort();
    }
}
