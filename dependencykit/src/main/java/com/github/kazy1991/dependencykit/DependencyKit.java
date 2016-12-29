package com.github.kazy1991.dependencykit;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class DependencyKit {

    private static Map<Class, Class> rootBindingMap = new HashMap<>();

    public static void configure(Module... modules) {
        for (Module module : modules) {
            DependencyKit.rootBindingMap.putAll(module.getBindingMap());
        }
    }

    @SuppressWarnings("Since15")
    public static void inject(Object instance) {
        final Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getDeclaredAnnotation(Inject.class) != null) {
                try {
                    field.setAccessible(true);
                    field.set(instance, get(field.getType()));
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Could not inject field");
                }
            }
        }
    }

    @SuppressWarnings("Since15")
    private static Object get(Class<?> type) {
        Class<?> implType = rootBindingMap.get(type);
        for (Constructor constructor : implType.getDeclaredConstructors()) {
            if (constructor.getDeclaredAnnotation(Inject.class) != null) {
                Object[] args = new Object[0];
                try {
                    return constructor.newInstance(args);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException("Could not create instance");
                }
            }
        }
        throw new IllegalStateException("Cound not find @Inject constructor");
    }
}
