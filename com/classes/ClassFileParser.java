package com.classes;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import com.classes.cp.CpInfo;
import com.classes.cp.DoubleInfo;
import com.classes.cp.LongInfo;
import com.classes.methods.MethodInfo;
import com.classes.attr.AttributeInfo;
import com.classes.attr.FieldInfo;

public class ClassFileParser {
    private final DataInputStream stream;
    private CpInfo[] constantPool;

    public ClassFileParser(Path path) throws FileNotFoundException {
        // FileInputStream reads bytes from a given file, and opens the file
        // DataInputStream does not know how to do that but knows how to read
        // primitives from FileInputStream, like reading a full int rather than
        // just reading byte by byte and construct the primitive

        // Try-catch on a constructor won't work, just create it and since
        // its ancestor is Exception then we must declare a throw in the
        // function decl
        stream = new DataInputStream(new FileInputStream(path.toFile()));
    }

    public void close() throws IOException {
        stream.close();
    }

    public Header parseHeader() throws IOException {
        // magic is 4 bytes so an int, each digit from CAFEBABE is 4 bits so 4x8=32
        int magic = stream.readInt();
        if (magic != 0xCAFEBABE) {
            throw new IOException("Not a class file: magic = " + Integer.toHexString(magic));
        }

        // https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html
        // unsigned in the docs, int does not have unsigned and its always gonna be CAFEBABE
        int minorVersion = stream.readUnsignedShort();
        int majorVersion = stream.readUnsignedShort();
        int constantPoolCount = stream.readUnsignedShort();

        System.out.println("magic: %s, minorVersion: %d, majorVersion: %d, constantPoolCount: %d"
                .formatted(Integer.toHexString(magic), minorVersion, majorVersion, constantPoolCount-1));

        return new Header(minorVersion, majorVersion, constantPoolCount);
    }

    public static AttributeInfo parseAttributes(DataInputStream stream) throws IOException {
        int attributeNameIndex = stream.readUnsignedShort();
        int attributeLength = stream.readInt();

        byte[] bytes = new byte[attributeLength];
        stream.readFully(bytes);

        return new AttributeInfo(attributeNameIndex, bytes);
    }

    public ClassfileRecord parse() throws IOException {
        int constantPoolCount = parseHeader().constantPoolCount;
        constantPool = new CpInfo[constantPoolCount];
        for(int i = 1; i < constantPoolCount; i++) {
            constantPool[i] = CpInfo.read(stream);
            if(constantPool[i] instanceof LongInfo || constantPool[i] instanceof DoubleInfo) {
                i++; // Skip to reserve space
            }
        }
        
        int accessFlags = stream.readUnsignedShort();
        int thisClass = stream.readUnsignedShort();
        int superClass = stream.readUnsignedShort();
        int interfaceCount = stream.readUnsignedShort();

        // In the docs it says u2 interfaces[interfaces_count];
        // so it is just an array of 2 bytes
        int[] interfaces = new int[interfaceCount];
        for(int i = 0; i < interfaceCount; i++) {
            interfaces[i] = stream.readUnsignedShort();
        }
        
        int fieldCount = stream.readUnsignedShort();
        FieldInfo[] fields = new FieldInfo[fieldCount];
        for(int i = 0; i < fieldCount; i++) {
            int fieldAccessFlags = stream.readUnsignedShort();
            int nameIndex = stream.readUnsignedShort();
            int descriptorIndex = stream.readUnsignedShort();
            int attributeCount = stream.readUnsignedShort();

            fields[i] = new FieldInfo(fieldAccessFlags, nameIndex, descriptorIndex, attributeCount).parse(stream);
        }

        int methodCount = stream.readUnsignedShort();
        MethodInfo[] methods = new MethodInfo[methodCount];
        for(int i = 0; i < methodCount; i++) {
            int methodAccessFlags = stream.readUnsignedShort();
            int nameIndex = stream.readUnsignedShort();
            int descriptorIndex = stream.readUnsignedShort();
            int attributeCount = stream.readUnsignedShort();

            methods[i] = new MethodInfo(methodAccessFlags, nameIndex, descriptorIndex, attributeCount).parse(stream);
        }
        
        int attributeCount = stream.readUnsignedShort();
        
        AttributeInfo[] attributes = new AttributeInfo[attributeCount];
        for(int i = 0; i < attributeCount; i++) {
            attributes[i] = parseAttributes(stream);
        }
    
        return new ClassfileRecord(new Header(methodCount, attributeCount, constantPoolCount), 
            constantPool, accessFlags, thisClass, superClass, interfaces, fields, methods, attributes);
    }
}
