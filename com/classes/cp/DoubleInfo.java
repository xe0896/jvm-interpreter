package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

public class DoubleInfo extends CpInfo {
    public final int highBytes;
    public final int lowBytes;

    DoubleInfo(DataInputStream stream) throws IOException {
        super(6);
        highBytes = stream.readInt();
        lowBytes = stream.readInt();
    }
}
