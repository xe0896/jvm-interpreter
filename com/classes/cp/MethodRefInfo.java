package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;
import com.classes.ClassfileRecord;
import com.classes.methods.CodeAttribute;

public class MethodRefInfo extends CpInfo {
    public final int classIndex;
    public final int nameAndTypeIndex;
    public CodeAttribute resolvedCode;
    public ClassfileRecord resolvedRecord;
    public int paramCount;

    MethodRefInfo(DataInputStream stream) throws IOException {
        super(10);
        classIndex = stream.readUnsignedShort();
        nameAndTypeIndex = stream.readUnsignedShort();
    }
}
