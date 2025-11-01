package com.example.demo.mapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Mapper {
    public static Object map(Object source, Class<?> destination) throws Exception {
        Object dto = destination.getDeclaredConstructor().newInstance();
        for (Field dtoField : destination.getDeclaredFields()) {
            dtoField.setAccessible(true);
            try {
                Field entityField = source.getClass().getDeclaredField(dtoField.getName());
                entityField.setAccessible(true);
                dtoField.set(dto, entityField.get(source));
            } catch (NoSuchFieldException ignored) {
            }
        }
        return dto;
    }

    public static List<Object> mapList(List<Object> entities, Class<?> dtoClass) throws Exception {
        List<Object> dtoList = new ArrayList<>();
        for (Object e : entities) {
            dtoList.add(map(e, dtoClass));
        }
        return dtoList;
    }
}
