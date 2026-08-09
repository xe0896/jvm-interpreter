package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

import com.classes.ReflectionHelper;

public abstract class CpInfo {
    public final int tag;

    protected CpInfo(int tag) {
        this.tag = tag;
    }

    public static CpInfo read(DataInputStream stream) throws IOException {
        int tag = stream.readUnsignedByte();
        return switch (tag) {
            case 1 -> new Utf8Info(stream);
            case 3 -> new IntegerInfo(stream);
            case 4 -> new FloatInfo(stream);
            case 5 -> new LongInfo(stream);
            case 6 -> new DoubleInfo(stream);
            case 7 -> new ClassInfo(stream);
            case 8 -> new StringInfo(stream);
            case 9 -> new FieldRefInfo(stream);
            case 10 -> new MethodRefInfo(stream);
            case 11 -> new InterfaceRefInfo(stream);
            case 12 -> new NameAndTypeInfo(stream);
            case 15 -> new MethodHandleInfo(stream);
            case 16 -> new MethodTypeInfo(stream);
            case 17 -> new DynamicInfo(stream);
            case 18 -> new InvokeDynamicInfo(stream);
            case 19 -> new ModuleInfo(stream);
            case 20 -> new PackageInfo(stream);
            default -> new ErrorInfo();
        };
    }

    @Override
    public String toString() {
        return ReflectionHelper.toString(this);
    }
}
