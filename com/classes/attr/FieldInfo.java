package com.classes.attr;
import java.io.DataInputStream;
import java.io.IOException;

import javax.management.Attribute;

import com.classes.ClassFileParser;

public class FieldInfo {
    public int fieldAccessFlags;
    public int nameIndex;
    public int descriptorIndex;

    public AttributeInfo[] attributes;

    public FieldInfo(int fieldAccessFlags, int nameIndex, int descriptorIndex, int attributeCount) {
        this.fieldAccessFlags = fieldAccessFlags;
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;
        this.attributes = new AttributeInfo[attributeCount];
    }

    public FieldInfo parse(DataInputStream stream) throws IOException {
        for(int i = 0; i < attributes.length; i++) {
            attributes[i] = ClassFileParser.parseAttributes(stream);
        }

        return this;
    }
}
