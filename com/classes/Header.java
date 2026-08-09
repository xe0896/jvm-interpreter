package com.classes;

public class Header {
    public final int minorVersion;
    public final int majorVersion;
    public final int constantPoolCount;

    public Header(int minorVersion, int majorVersion, int constantPoolCount) {
        this.minorVersion = minorVersion;
        this.majorVersion = majorVersion;
        this.constantPoolCount = constantPoolCount;
    }

    @Override
    public String toString() {
        return "[minorVersion=%d, majorVersion=%d, constantPoolCount=%d]".formatted(minorVersion,
            majorVersion, constantPoolCount
        );
    }
}
