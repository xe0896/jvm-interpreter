package com.classes;
import java.util.HashMap;
import java.util.Map;

public class Heap {
    Map<Integer, ObjectInstance> objects = new HashMap<>();
    int nextId = 1;

    int allocate(String className) {
        int id = nextId++; // post incremented

        ObjectInstance obj = new ObjectInstance(className);
        objects.put(id, obj);

        return id;
    }
    
    class ObjectInstance {
        String className;
        Map<String, Object> fields;

        ObjectInstance(String className) {
            this.className = className;
            this.fields = new HashMap<>();
        }
    }
}
