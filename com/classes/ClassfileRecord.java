package com.classes;
import com.classes.cp.CpInfo;
import com.classes.cp.Utf8Info;
import com.classes.attr.AttributeInfo;
import com.classes.attr.FieldInfo;
import com.classes.methods.MethodInfo;

public record ClassfileRecord(Header header, CpInfo[] constantPool, int accessFlags, 
    int thisClass, int superClass, int[] interfaces, FieldInfo[] fieldInfo, 
    MethodInfo[] methodInfo, AttributeInfo[] attributeInfo) {
        public String utf8At(int index) {
            CpInfo entry = constantPool[index];
            if (entry instanceof Utf8Info u) {
                return u.value;
            }
            throw new IllegalStateException(
                "expected Utf8 at #" + index + ", got " + entry);
        }

        public CpInfo cpAt(int index) {  
            CpInfo entry = constantPool[index];
            return entry;
        }
    }
