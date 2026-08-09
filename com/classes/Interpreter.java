package com.classes;

import com.classes.cp.CpInfo;
import com.classes.cp.IntegerInfo;
import com.classes.cp.MethodRefInfo;
import com.classes.cp.NameAndTypeInfo;
import com.classes.cp.StringInfo;
import com.classes.methods.CodeAttribute;
import com.classes.methods.MethodInfo;
import com.classes.cp.Utf8Info;
import com.classes.cp.ClassInfo;
import com.classes.Heap;

import java.util.Stack;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;

import com.classes.attr.AttributeInfo;

public class Interpreter {
    // Maintain a stack of frames which these frames would have their own intermediate stack, local variables and program counter
    private Stack<Frame> frames;
    private final Path classPath;
    private final Heap heap;
    Map<String, ClassfileRecord> loaded = new HashMap<>();

    public Interpreter(ClassfileRecord record) throws IOException {
        this.heap = new Heap();
        this.frames = new Stack<>();
        this.classPath = Path.of("."); // Current directory
        // A starting point is required, it would be the standard Java one which would be a main method
        // that accepts a parameter String[] args
        this.frames.push(new Frame(code(record, "main", "([Ljava/lang/String;)I"), record));
    }

    // Given a method name and its descriptor, we find the code related to it and store it in CodeAttribute 
    // this is limited to not care about classes for now
    public CodeAttribute code(ClassfileRecord record, String methodName, String descriptor) throws IOException {
        // Iterate over the methods stored in the record that was made during parsing
        for(MethodInfo method : record.methodInfo()) {
            System.out.println("Found methodName: " + record.utf8At(method.nameIndex));
            System.out.println("Found descriptor: " + record.utf8At(method.descriptorIndex));
            // Two checks to ensure that this method matches what we have stored
            if(!record.utf8At(method.nameIndex).equals(methodName)) continue;
            if(!record.utf8At(method.descriptorIndex).equals(descriptor)) continue;
            // Iterate over the attributes and we want to find the "Code" UTF8 pointer as that would contain
            // the actual bytecode for the method
            for(AttributeInfo attribute : method.attributes) {
                int attributeIndex = attribute.attributeNameIndex();
                if(record.utf8At(attributeIndex).equals("Code")) {
                    // Create stack and local array, javac provides this information
                    byte[] codeAttribute = attribute.info(); // Already at start of maxStack
                    ByteArrayInputStream byteStream = new ByteArrayInputStream(codeAttribute);
                    DataInputStream stream = new DataInputStream(byteStream);
                    int maxStack = stream.readUnsignedShort();
                    int maxLocal = stream.readUnsignedShort();
                    int codeLength = stream.readInt();
                    byte[] code = new byte[codeLength];
                    stream.readFully(code);
                    return new CodeAttribute(attribute.attributeNameIndex(), maxStack, maxLocal, code);
                }
            }
        }
        System.out.println("Could not find");
        return null;
    }

    // Pushes the integer provided into the operand stack
    private void bipush(Frame frame) {
        byte b = frame.code[frame.pc + 1];
        frame.stack.push((int) b);
        frame.pc+=2;
    }

    private void sipush(Frame frame) {
        // The idea here is that sipush is pushing a short, that has two bytes
        // so since our array is in bytes we need to make the code[frame.pc + 1] bytes
        // the high order byte since JVM works in big-endian, we shift it 8 bits
        // to the left to make it have its own byte and then OR it with the
        // low order byte and do 0xFF to make it unsigned which would output a sign
        // since we want our values to represent each unsigned byte xxxx yyyy
        int value = (short) (((frame.code[frame.pc + 1] & 0xFF) << 8)
                   | (frame.code[frame.pc + 2] & 0xFF));

        frame.stack.push(value);
        frame.pc += 3;
    }

    // Self-explantory
    private void iadd(int x, int y, Frame frame) {
        frame.stack.push(y + x);
        frame.pc++;
    }

    private void isub(int x, int y, Frame frame) {
        frame.stack.push(y - x);
        frame.pc++;
    }

    private void imul(int x, int y, Frame frame) {
        frame.stack.push(y * x);
        frame.pc++;
    }

    private void idiv(int x, int y, Frame frame) {
        frame.stack.push(y / x);
        frame.pc++;
    }

    private void irem(int x, int y, Frame frame) {
        // or we could do frame.push(y % x)
        int res = y - (y / x) * x;
        frame.stack.push(res);
        frame.pc++;
    }

    private void iand(int x, int y, Frame frame) {
        frame.stack.push(y & x);
        frame.pc++;
    }

    private void ior(int x, int y, Frame frame) {
        frame.stack.push(y | x);
        frame.pc++;
    }

    private void ixor(int x, int y, Frame frame) {
        frame.stack.push(y ^ x);
        frame.pc++;
    }

    // Shift to the left
    private void ishl(int x, int y, Frame frame) {
        // low 5 bits: 00011111
        int s = x & 0x1F;
        frame.stack.push(y << s);
        frame.pc++;
    }

    // Shift to the right
    private void ishr(int x, int y, Frame frame) {
        // low 5 bits: 00011111
        int s = x & 0x1F;
        frame.stack.push(y >> s);
        frame.pc++;
    }

    // Unsigned shift to the right
    private void iushr(int x, int y, Frame frame) {
        int s = x & 0x1F;
        // triple > makes an unsigned shift, moves the sign bits
        frame.stack.push(y >>> s);
        frame.pc++;
    }

    private void ineg(int x, Frame frame) {
        frame.stack.push(-x);
        frame.pc++;
    }

    // ldc=Load Constant, from the constant pool load some value mainly used for Strings and for large integers
    // which are usually stored in the constant pool
    private void ldc(Frame frame) {
        int idx = frame.code[frame.pc + 1];
        CpInfo val = frame.record.cpAt(idx);
        if (val instanceof IntegerInfo i) {
            frame.stack.push(i.value);
        } else if (val instanceof StringInfo) {
            // Reject for now, we need a heap that we can index since
            // Strings can be shared and change meaning better to be in the heap.
        }
    }

    // Loads the variable stored at the provided index into the operand stack
    private void iload(Frame frame) {
        int idx = frame.code[frame.pc + 1] & 0xFF;
        int local = frame.locals[idx];
        frame.stack.push(local);
        frame.pc+=2;
    }

    private void iload_n(int n, Frame frame) {
        int local = frame.locals[n];
        frame.stack.push(local);
        frame.pc++;
    }

    // Stores the variable stored at the provided index into the local array
    private void istore(Frame frame) {
        int idx = frame.code[frame.pc + 1] & 0xFF;
        frame.locals[idx] = frame.stack.pop();
        frame.pc+=2;
    }

    private void istore_n(int n, Frame frame) {
        frame.locals[n] = frame.stack.pop();
        frame.pc++;
    }

    private void iconst_n(int n, Frame frame) {
        frame.stack.push(n);
        frame.pc++;
    }

    // Increments a value at index by some constant
    private void iinc(Frame frame) {
        int idx = frame.code[frame.pc + 1] & 0xFF;
        int constant = frame.code[frame.pc + 2];
        frame.locals[idx] += constant;
        frame.pc+=3;
    }

    // Duplicates what is at the top of the stack
    private void dup(int x, Frame frame) {
        frame.stack.push(x);
        frame.pc++;
    }

    private void pop(Frame frame) {
        frame.stack.pop();
        frame.pc++;
    }

    // Swaps the top two values at the top of the operand stack
    private void swap(Frame frame) {
        int x = frame.stack.pop();
        int y = frame.stack.pop();
        frame.stack.push(x);
        frame.stack.push(y);
        frame.pc++;   
    }

    // Calls branchIf as it is the same for most, just that if it needs to move to that
    // given its condition
    private void ifeq(Frame frame) {
        int comp = frame.stack.pop();
        branchIf(comp == 0, frame);
    }

    private void ifne(Frame frame) {
        int comp = frame.stack.pop();
        branchIf(comp != 0, frame);
    }

    private void ifit(Frame frame) {
        int comp = frame.stack.pop();
        branchIf(comp < 0, frame);
    }

    private void ifle(Frame frame) {
        int comp = frame.stack.pop();
        branchIf(comp <= 0, frame);
    }

    private void ifgt(Frame frame) {
        int comp = frame.stack.pop();
        branchIf(comp > 0, frame);
    }

    private void ifge(Frame frame) {
        int comp = frame.stack.pop();
        branchIf(comp >= 0, frame);
    }

    private void branchIf(boolean condition, Frame frame) {
        if(condition) {
            int branch1byte = frame.code[frame.pc + 1] & 0xFF;
            int branch2byte = frame.code[frame.pc + 2] & 0xFF;

            // docs say 16 bit offset so cast to short
            int offset = (short) ((branch1byte << 8) | branch2byte);
            frame.pc += offset;
        } else {
            frame.pc += 3;
        }
    }

     private int getIdx(Frame frame) {
        int index1byte = frame.code[frame.pc + 1] & 0xFF;
        int index2byte = frame.code[frame.pc + 2] & 0xFF;

        return (short) ((index1byte << 8) | index2byte);
    }

    // Counts how many integer parameters we have for a given descriptor
    private int paramIntegerCount_TEMP(String descriptor) {
        int paramCount = 0;
        for(int i = 1; descriptor.charAt(i) != ')'; i++) {
            if(descriptor.charAt(i) == 'I') {
                paramCount++;
            }
        }
        return paramCount;
    }

    // For a parameter, javac reads left to right so as it is reading it would push
    // the parameters onto the stack, so we want to provide the new method call's
    // parameters to them in order from left to right but as we push them into the stack
    // they would be in reverse order, so we would pop them and store them in reverse order
    // to counteract this and allocate the proper locals from left to right as they came
    private void staticParameters(Frame newFrame, Frame callerFrame, int paramCount) {
        for(int i = paramCount - 1; i >= 0; i--) {
            newFrame.locals[i] = callerFrame.stack.pop();
        }
    }

    private void specialParameters(Frame newFrame, Frame callerFrame, int paramCount) {
        for(int i = paramCount; i >= 1; i--) {
            System.out.println(callerFrame.stack.peek());
            newFrame.locals[i] = callerFrame.stack.pop();
        }
    }

    private ClassfileRecord loadClass(String className) throws IOException {
        ClassfileRecord cf  = loaded.get(className);
        if(cf != null) return cf;

        System.out.println(className + ".class");

        ClassFileParser parser = new ClassFileParser(classPath.resolve(className + ".class"));
        cf = parser.parse();

        loaded.put(className, cf);

        return cf;
    }

    // Calls the static method and allocates the parameters and creates a new frame for this
    // call and pushes it into our stack
    private void invokestatic(Frame frame) throws IOException {
        // idx represented by 16 bits so we create it by the provided bytes
        int idx = getIdx(frame);
        MethodRefInfo method = (MethodRefInfo) frame.record.cpAt(idx); // can assume its MethodRef

        // Cached branch, if we have already seen we don't have to loop again with code()
        if(method.resolvedCode != null) {
            CodeAttribute resolvedCode = method.resolvedCode;
            ClassfileRecord resolvedRecord = method.resolvedRecord;
            Frame newFrame = new Frame(resolvedCode, resolvedRecord);

            staticParameters(newFrame, frame, method.paramCount);

            frames.push(newFrame);
        } else {
            NameAndTypeInfo nameAndType = (NameAndTypeInfo) frame.record.cpAt(method.nameAndTypeIndex);
            String methodName = frame.record.utf8At(nameAndType.nameIndex);
            String descriptor = frame.record.utf8At(nameAndType.descriptorIndex);
            String className = frame.record.utf8At(method.classIndex);

            int paramCount = paramIntegerCount_TEMP(descriptor);

            ClassfileRecord foundRecord = loadClass(className);
            CodeAttribute foundCode = code(foundRecord, methodName, descriptor);

            method.resolvedRecord = foundRecord;
            method.resolvedCode = foundCode;
            method.paramCount = paramCount;

            Frame newFrame = new Frame(foundCode, foundRecord);
            staticParameters(newFrame, frame, paramCount);

            frames.push(newFrame);
        }
        frame.pc+=3;
    }

    // A special invoke would be something that contains no override considerations, like
    // super.someMethod() or new Object(), these two clearly identify what method we want
    // so we do not need to go up some inheritence chain to find the correct one when it is
    // already provided to us
    private void invokespecial(Frame frame) throws IOException {
        int idx = getIdx(frame);
        MethodRefInfo method = (MethodRefInfo) frame.record.cpAt(idx);

        if(method.resolvedCode != null) {
            System.out.println("resolved special");
            CodeAttribute resolvedCode = method.resolvedCode;
            ClassfileRecord resolvedRecord = method.resolvedRecord;
            Frame newFrame = new Frame(resolvedCode, resolvedRecord);

            specialParameters(newFrame, frame, method.paramCount);

            frames.push(newFrame);
        } else {
            NameAndTypeInfo nameAndType = (NameAndTypeInfo) frame.record.cpAt(method.nameAndTypeIndex);
            String methodName = frame.record.utf8At(nameAndType.nameIndex);
            String descriptor = frame.record.utf8At(nameAndType.descriptorIndex);
            ClassInfo classInfo = (ClassInfo) frame.record.cpAt(method.classIndex);
            Utf8Info nameUtf8 = (Utf8Info) frame.record.cpAt(classInfo.nameIndex);
            String className = nameUtf8.value;

            if (className.equals("java/lang/Object") && methodName.equals("<init>")) {
                frame.stack.pop();   // consume the receiver
                frame.pc += 3;
                return;
            }

            System.out.println("Path: " + className + methodName + descriptor);
            int paramCount = paramIntegerCount_TEMP(descriptor);

            System.out.println("fousey: " + className);

            ClassfileRecord foundRecord = loadClass(className);
            CodeAttribute foundCode = code(foundRecord, methodName, descriptor);
            
            method.resolvedRecord = foundRecord;
            method.resolvedCode = foundCode;
            method.paramCount = paramCount;

            System.out.println(foundRecord);
            System.out.println(foundCode);

            Frame newFrame = new Frame(foundCode, foundRecord);

            // Dup makes the ref appear twice [ref, ref, args]
            specialParameters(newFrame, frame, paramCount);

            System.out.println("special: " + newFrame);

            frames.push(newFrame);
        }

        frame.pc+=3;

        //System.out.println(method);
    }

    private void invokevirtual(Frame frame) {
        System.out.println("Virtual some how reached");
    }

    private void putfield(Frame frame) {
        int idx = getIdx(frame);

        System.out.println("idx1: " + idx);

        frame.pc+=3;
    }

    // Creates a new reference of the object, in our case we just store
    // some ID that refers to it and store it in the heap, then after
    // this new (when it comes to objects atleast) it would duplicate to 
    // keep this reference then call the arguments then the constructor
    // where the constructor would pop ref + args but then the dup still
    // left a ref for future calls like obj.jump() after new Obj(3) 
    private void _new(Frame frame) {
        int idx = getIdx(frame);
        
        ClassInfo c = (ClassInfo) frame.record.cpAt(idx);
        String loc = frame.record.utf8At(c.nameIndex);

        int id = heap.allocate(loc);

        frame.stack.push(id);

        frame.pc +=3;
    }

    private void aload(Frame frame) {
        int idx = frame.code[frame.pc + 1] & 0xFF;

        int local = frame.locals[idx];

        frame.stack.push(local);
        frame.pc+=2;
    }

    private void aload_n(int n, Frame frame) {
        int local = frame.locals[n];
        frame.stack.push(local);
        frame.pc++;
    }

    private void astore(Frame frame) {
        int idx = frame.code[frame.pc + 1] & 0xFF;
        frame.locals[idx] = frame.stack.pop();
        frame.pc+=2;
    }

    private void astore_n(int n, Frame frame) {
        frame.locals[n] = frame.stack.pop();
        frame.pc++;
    }

    private void if_icmpeq(int x, int y, Frame frame) {branchIf(y == x, frame);}
    
    private void if_icmpne(int x, int y, Frame frame) {branchIf(y != x, frame);}

    private void if_icmplt(int x, int y, Frame frame) {branchIf(y < x, frame);}

    private void if_icmple(int x, int y, Frame frame) {branchIf(y <= x, frame);}

    private void if_icmpgt(int x, int y, Frame frame) {branchIf(y > x, frame);}

    private void if_icmpge(int x, int y, Frame frame) {branchIf(y >= x, frame);}
    
    public int run() throws IOException {
        Frame init = frames.peek();
        init.locals[0] = 5;
    
        while(frames.size() != 0) {
            // 0xFF turns it into unsigned allowing 0xAC to not need to be casted
            Frame frame = frames.peek();
            int opcode = frame.code[frame.pc] & 0xFF;
            System.out.println(frames);
            System.out.printf("opcode: %02X%n", opcode);
            switch(opcode) {
                case Instruction.ILOAD -> iload(frame);
                case Instruction.ILOAD_0 -> iload_n(0, frame);
                case Instruction.ILOAD_1 -> iload_n(1, frame);
                case Instruction.ILOAD_2 -> iload_n(2, frame);
                case Instruction.ILOAD_3 -> iload_n(3, frame);
                case Instruction.ISTORE -> istore(frame);
                case Instruction.ISTORE_0 -> istore_n(0, frame);
                case Instruction.ISTORE_1 -> istore_n(1, frame);
                case Instruction.ISTORE_2 -> istore_n(2, frame);
                case Instruction.ISTORE_3 -> istore_n(3, frame);
                case Instruction.IINC -> iinc(frame);
                case Instruction.ICONST_M1 -> iconst_n(-1, frame);
                case Instruction.ICONST_0 -> iconst_n(0, frame);
                case Instruction.ICONST_1 -> iconst_n(1, frame);
                case Instruction.ICONST_2 -> iconst_n(2, frame);
                case Instruction.ICONST_3 -> iconst_n(3, frame);
                case Instruction.ICONST_4 -> iconst_n(4, frame);
                case Instruction.ICONST_5 -> iconst_n(5, frame);
                case Instruction.BIPUSH -> bipush(frame);
                case Instruction.SIPUSH -> sipush(frame);
                case Instruction.LDC -> ldc(frame);
                case Instruction.IADD -> iadd(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.ISUB -> isub(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.IMUL -> imul(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.IDIV -> idiv(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.IREM -> irem(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.INEG -> ineg(frame.stack.pop(), frame);
                case Instruction.IAND -> iand(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.IOR -> ior(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.IXOR -> ixor(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.ISHL -> ishl(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.ISHR -> ishr(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.IUSHR -> iushr(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.DUP -> dup(frame.stack.peek(), frame);
                case Instruction.POP -> {System.out.println("H"); pop(frame);}
                case Instruction.SWAP -> swap(frame);
                case Instruction.IFEQ -> ifeq(frame);
                case Instruction.IFNE -> ifne(frame);
                case Instruction.IFLT -> ifit(frame);
                case Instruction.IFGE -> ifge(frame);
                case Instruction.IFGT -> ifgt(frame);
                case Instruction.IFLE -> ifle(frame);
                case Instruction.IF_ICMPEQ -> if_icmpeq(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.IF_ICMPNE -> if_icmpne(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.IF_ICMPLT -> if_icmplt(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.IF_ICMPGE -> if_icmpge(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.IF_ICMPGT -> if_icmpgt(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.IF_ICMPLE -> if_icmple(frame.stack.pop(), frame.stack.pop(), frame);
                case Instruction.GOTO -> branchIf(true, frame); // goto
                case Instruction.RETURN -> frames.pop(); // void
                case Instruction.IRETURN -> {
                    // Transfer the value at the top of the poppedFrame stack to the frame beeath it
                    // via push, but if this is the last frame then it would return its value
                    Frame poppedFrame = frames.pop();
                
                    int result = poppedFrame.stack.pop();
                    if(frames.size() == 0) {
                        return result;
                    } else {
                        frames.peek().stack.push(result);
                    }
                }
                case Instruction.INVOKEVIRTUAL -> invokevirtual(frame);
                case Instruction.INVOKESTATIC -> invokestatic(frame);
                case Instruction.INVOKESPECIAL -> invokespecial(frame);
                case Instruction.PUTFIELD -> putfield(frame);
                case Instruction.NEW -> _new(frame);
                case Instruction.ALOAD -> aload(frame);
                case Instruction.ALOAD_0 -> aload_n(0, frame);
                case Instruction.ALOAD_1 -> aload_n(1, frame);
                case Instruction.ALOAD_2 -> aload_n(2, frame);
                case Instruction.ALOAD_3 -> aload_n(3, frame);
                case Instruction.ASTORE -> astore(frame);
                case Instruction.ASTORE_0 -> astore_n(0, frame);
                case Instruction.ASTORE_1 -> astore_n(1, frame);
                case Instruction.ASTORE_2 -> astore_n(2, frame);
                case Instruction.ASTORE_3-> astore_n(3, frame);
                default -> {
                    System.out.println(frame);
                    System.out.printf("caught opcode: %02X%n", opcode);
                    return 0;
                }
            }
        }

        throw new IllegalStateException("Method ended without a return opcode");
    }
}
