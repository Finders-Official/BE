package com.finders.api.util;

import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;

public final class EntityTestFactory {

    private EntityTestFactory() {}

    public static <T> T newInstance(Class<T> type) {
        try {
            Constructor<T> c = type.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot instantiate: " + type.getName(), e);
        }
    }

    public static <T> T setId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
