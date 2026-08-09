package com.classes;

import com.classes.methods.CodeAttribute;
import com.classes.ClassfileRecord;

import java.util.Stack;

class Frame {
    public int pc = 0;
    public int[] locals;
    public final Stack<Integer> stack;
    public final byte[] code;
    public ClassfileRecord record;

    public Frame(CodeAttribute code, ClassfileRecord record) {
        this.locals = new int[code.maxLocals()];
        this.record = record;
        this.stack = new Stack<>();
        this.code = code.code();
    }
}
