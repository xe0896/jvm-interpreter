package com.classes.methods;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

import com.classes.ClassFileParser;
import com.classes.attr.AttributeInfo;

public class MethodInfo {
    public int methodAccessFlags;
    public int nameIndex; // Method name pointed to UTF-8
    public int descriptorIndex; // Type signature pointed to UTF-8
    
    public AttributeInfo[] attributes;

    public MethodInfo(int methodAccessFlags, int nameIndex, int descriptorIndex, int attributeCount) {
        this.methodAccessFlags = methodAccessFlags;
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;

        this.attributes = new AttributeInfo[attributeCount];
    }

    public MethodInfo parse(DataInputStream stream) throws IOException {
        for(int i = 0; i < attributes.length; i++) {
            attributes[i] = ClassFileParser.parseAttributes(stream);
        }

        return this;
    }

    @Override
    public String toString() {
        return "MethodInfo[accessFlags=" + methodAccessFlags
            + ", nameIndex=" + nameIndex
            + ", descriptorIndex=" + descriptorIndex
            + ", attributes=" + Arrays.toString(attributes) + "]";
    }
}
