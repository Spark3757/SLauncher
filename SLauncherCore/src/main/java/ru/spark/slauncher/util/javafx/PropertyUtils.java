package ru.spark.slauncher.util.javafx;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class PropertyUtils {
    private PropertyUtils() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map<String, Function<Object, PropertyHandle>> getPropertyHandleFactories(Class<?> type) {
        Map<String, Method> collectionGetMethods = new LinkedHashMap<>();
        Map<String, Method> propertyMethods = new LinkedHashMap<>();
        for (Method method : type.getMethods()) {
            Class<?> returnType = method.getReturnType();
            if (method.getParameterCount() == 0
                    && !returnType.equals(void.class)) {
                String name = method.getName();
                if (name.endsWith("Property")) {
                    String propertyName = name.substring(0, name.length() - "Property".length());
                    if (!propertyName.isEmpty() && Property.class.isAssignableFrom(returnType)) {
                        propertyMethods.put(propertyName, method);
                    }
                } else if (name.startsWith("get")) {
                    String propertyName = name.substring("get".length());
                    if (!propertyName.isEmpty() &&
                            (ObservableList.class.isAssignableFrom(returnType)
                                    || ObservableSet.class.isAssignableFrom(returnType)
                                    || ObservableMap.class.isAssignableFrom(returnType))) {
                        propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
                        collectionGetMethods.put(propertyName, method);
                    }
                }
            }
        }
        propertyMethods.keySet().forEach(collectionGetMethods::remove);

        Map<String, Function<Object, PropertyHandle>> result = new LinkedHashMap<>();
        propertyMethods.forEach((propertyName, method) -> {
            result.put(propertyName, instance -> {
                Property returnValue;
                try {
                    returnValue = (Property<?>) method.invoke(instance);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(e);
                }
                return new PropertyHandle(returnValue, returnValue);
            });
        });

        collectionGetMethods.forEach((propertyName, method) -> {
            result.put(propertyName, instance -> {
                Object returnValue;
                try {
                    returnValue = method.invoke(instance);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(e);
                }
                WritableValue<Object> accessor;
                if (returnValue instanceof ObservableList) {
                    accessor = new WritableValue<Object>() {
                        @Override
                        public Object getValue() {
                            return returnValue;
                        }

                        @Override
                        public void setValue(Object value) {
                            ((ObservableList) returnValue).setAll((List) value);
                        }
                    };
                } else if (returnValue instanceof ObservableSet) {
                    accessor = new WritableValue<Object>() {
                        @Override
                        public Object getValue() {
                            return returnValue;
                        }

                        @Override
                        public void setValue(Object value) {
                            ObservableSet target = (ObservableSet) returnValue;
                            target.clear();
                            target.addAll((Set) value);
                        }
                    };
                } else if (returnValue instanceof ObservableMap) {
                    accessor = new WritableValue<Object>() {
                        @Override
                        public Object getValue() {
                            return returnValue;
                        }

                        @Override
                        public void setValue(Object value) {
                            ObservableMap target = (ObservableMap) returnValue;
                            target.clear();
                            target.putAll((Map) value);
                        }
                    };
                } else {
                    throw new IllegalStateException();
                }
                return new PropertyHandle(accessor, (Observable) returnValue);
            });
        });
        return result;
    }

    public static void copyProperties(Object from, Object to) {
        Class<?> type = from.getClass();
        while (!type.isInstance(to))
            type = type.getSuperclass();

        getPropertyHandleFactories(type)
                .forEach((name, factory) -> {
                    PropertyHandle src = factory.apply(from);
                    PropertyHandle target = factory.apply(to);
                    target.accessor.setValue(src.accessor.getValue());
                });
    }

    public static void attachListener(Object instance, InvalidationListener listener) {
        getPropertyHandleFactories(instance.getClass())
                .forEach((name, factory) -> {
                    factory.apply(instance).observable.addListener(listener);
                });
    }

    public static class PropertyHandle {
        public final WritableValue<Object> accessor;
        public final Observable observable;

        public PropertyHandle(WritableValue<Object> accessor, Observable observable) {
            this.accessor = accessor;
            this.observable = observable;
        }
    }
}
