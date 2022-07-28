package com.imyvm.hoki.nbt;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.*;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public interface NbtPersistent {
    @Nullable
    default NbtElement serialize() {
        return NbtPersistentHelper.serialize(this);
    }

    default void deserialize(@Nullable NbtElement element) {
        if (element != null) {
            assert element instanceof NbtCompound;
            NbtPersistentHelper.deserialize(this, (NbtCompound) element);
        }
    }
}

class NbtPersistentHelper {
    private static final Map<Class<?>, ValueType> simpleTypes = buildValueTypes();

    private static Map<Class<?>, ValueType> buildValueTypes() {
        ImmutableMap.Builder<Class<?>, ValueType> map = ImmutableMap.builder();

        TriConsumer<Class<?>, Class<?>, ValueType> putPrimitiveType = (primitive, wrapper, valueType) -> {
            map.put(primitive, valueType);
            map.put(wrapper, valueType);
        };
        putPrimitiveType.accept(byte.class, Byte.class, ValueType.of(NbtByte::of, (element) -> ((AbstractNbtNumber) element).byteValue()));
        putPrimitiveType.accept(short.class, Short.class, ValueType.of(NbtShort::of, (element) -> ((AbstractNbtNumber) element).shortValue()));
        putPrimitiveType.accept(char.class, Character.class, ValueType.of((c) -> NbtShort.of((short) (char) c), (element) -> (char) ((AbstractNbtNumber) element).shortValue()));
        putPrimitiveType.accept(int.class, Integer.class, ValueType.of(NbtInt::of, (element) -> ((AbstractNbtNumber) element).intValue()));
        putPrimitiveType.accept(long.class, Long.class, ValueType.of(NbtLong::of, (element) -> ((AbstractNbtNumber) element).longValue()));
        putPrimitiveType.accept(float.class, Float.class, ValueType.of(NbtFloat::of, (element) -> ((AbstractNbtNumber) element).floatValue()));
        putPrimitiveType.accept(double.class, Double.class, ValueType.of(NbtDouble::of, (element) -> ((AbstractNbtNumber) element).doubleValue()));
        putPrimitiveType.accept(boolean.class, Boolean.class, ValueType.of(NbtByte::of, (element) -> ((AbstractNbtNumber) element).byteValue() != 0));

        map.put(String.class, ValueType.of(NbtString::of, NbtElement::asString));
        map.put(UUID.class, ValueType.of(NbtHelper::fromUuid, NbtHelper::toUuid));
        map.put(int[].class, ValueType.of(NbtIntArray::new, (element) -> ((NbtIntArray) element).getIntArray()));
        map.put(long[].class, ValueType.of(NbtLongArray::new, (element) -> ((NbtLongArray) element).getLongArray()));
        map.put(byte[].class, ValueType.of(NbtByteArray::new, (element) -> ((NbtByteArray) element).getByteArray()));

        return map.build();
    }

    public static NbtCompound serialize(NbtPersistent obj) {
        NbtCompound nbt = new NbtCompound();

        for (Field field : obj.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(NbtPersistentValue.class))
                continue;

            Object rawValue = fieldGet(field, obj);
            if (rawValue == null)
                continue;

            String key = getKey(field);
            ValueType type = simpleTypes.get(field.getType());
            if (type != null) {
                NbtElement element = type.serializer.apply(rawValue);
                if (element != null)
                    nbt.put(key, element);
            }
            else if (NbtPersistent.class.isAssignableFrom(field.getType()))
                nbt.put(key, ((NbtPersistent) rawValue).serialize());
            else
                throw new RuntimeException("Cannot serialize field \"" + field.getType().getName() + " " + field.getName() + "\", in class \"" + obj.getClass().getName() + "\"");
        }

        return nbt;
    }

    public static void deserialize(NbtPersistent obj, NbtCompound nbt) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(NbtPersistentValue.class))
                continue;

            String key = getKey(field);
            NbtElement element = nbt.get(key);
            ValueType type = simpleTypes.get(field.getType());
            if (NbtPersistent.class.isAssignableFrom(field.getType())) {
                Object value = fieldGet(field, obj);
                if (value == null) {
                    if (element == null)
                        continue;
                    value = getEmptyInstance(field.getType());
                    fieldSet(field, obj, value);
                }
                ((NbtPersistent) value).deserialize(element);
            }
            else if (element != null && type != null) {
                Object value = type.deserializer.apply(element);
                fieldSet(field, obj, value);
            }
            else if (element != null)
                throw new RuntimeException("Cannot deserialize field \"" + field.getType().getName() + " " + field.getName() + "\", in class \"" + obj.getClass().getName() + "\"");
        }
    }

    private static String getKey(Field field) {
        String key = field.getAnnotation(NbtPersistentValue.class).key();
        return key.isEmpty() ? field.getName() : key;
    }

    private static Object fieldGet(Field field, Object obj) {
        boolean canAccess = field.canAccess(obj);
        field.setAccessible(true);
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(canAccess);
        }
    }

    private static void fieldSet(Field field, Object obj, Object value) {
        boolean canAccess = field.canAccess(obj);
        field.setAccessible(true);
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        field.setAccessible(canAccess);
    }

    private static Object getEmptyInstance(Class<?> cls) {
        try {
            Method method = cls.getMethod("emptyForDeserialize");
            return method.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private record ValueType(
        Function<Object, NbtElement> serializer,
        Function<NbtElement, Object> deserializer
    ) {
        @SuppressWarnings("unchecked")
        private static <T> ValueType of(Function<T, NbtElement> serializer, Function<NbtElement, T> deserializer) {
            return new ValueType((obj) -> serializer.apply((T) obj), deserializer::apply);
        }
    }
}
