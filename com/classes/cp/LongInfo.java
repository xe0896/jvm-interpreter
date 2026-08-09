package com.classes.cp;

import java.io.DataInputStream;
import java.io.IOException;

// JVM makers say that they regret this but a long and double takes up
// two indicies in the constant pool table:
//   #21 = Long               123456789l
//   #23 = Class              #24            // test/Main

// The index #22 was skipped, this means when we iterate to create this
// constant table if we are creating a double or long then we need to
// skip an iteration to reserve space, you may think we could just
// use the space if its not being used but then our name index of UTF-8
// and other things would be wrong, we are reading from a .class that
// adheres to that
public class LongInfo extends CpInfo {
    public final int highBytes;
    public final int lowBytes;

    LongInfo(DataInputStream stream) throws IOException {
        super(5);
        highBytes = stream.readInt();
        lowBytes = stream.readInt();
    }
}
