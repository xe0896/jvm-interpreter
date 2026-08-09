package com.classes;

import java.lang.reflect.Field;

public class ReflectionHelper {
    public static String toString(Object obj) {
        StringBuilder sb = new StringBuilder();

        sb.append(obj.getClass().getSimpleName());
        sb.append("{");

        for (Field field : obj.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);

                sb.append(field.getName())
                  .append("=")
                  .append(field.get(obj))
                  .append(", ");

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        sb.delete(sb.length() - 2, sb.length());

        sb.append("}");
        return sb.toString();
    }
}