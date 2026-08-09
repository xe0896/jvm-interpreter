package com;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import com.classes.ClassFileParser;
import com.classes.ClassfileRecord;
import com.classes.Interpreter;

import com.classes.methods.CodeAttribute;

public class Parser {
    public static void main(String[] args) {
        try {
            ClassFileParser parser = new ClassFileParser(Path.of("test/M2.class"));
            ClassfileRecord record = parser.parse();
            Interpreter interpreter = new Interpreter(record);
            System.out.println(interpreter.run());
            //CodeAttribute method = interpreter.code();
            
            //Utf8Info utf = (Utf8Info) record.constantPool()[5];
            //System.out.println(utf.value);
            
        } catch (FileNotFoundException e) {
            System.err.println("Cannot read from file");
            return;
        } catch (IOException e) {
            System.err.println("Cannot read header: " + e.getLocalizedMessage());
            return;
        }
    }
}
