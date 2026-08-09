package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

public class Utf8Info extends CpInfo {
    public final String value;
    // UTF-8, a different one since the structure of this is:
    // CONSTANT_Utf8_info {
    //    u1 tag;
    //    u2 length;
    //    u1 bytes[length];
    //}
    // We read the length, then hop by the length, UTF-8 is 1 to 4 bytes
    // per character
    Utf8Info(DataInputStream stream) throws IOException {
        super(1);
        value = stream.readUTF(); // readUTF would read the 2 byte length then hop that
    }
}
