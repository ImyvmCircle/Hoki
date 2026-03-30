package com.imyvm.hoki.nbt;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.util.TriConsumer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

class NbtPersistentHelper {
    private static final Map<Class<?>, ValueType> simpleTypes = buildValueTypes();

    private NbtPersistentHelper() {
    }

    @SuppressWarnings("checkstyle:LineLength")
    private static Map<Class<?>, ValueType> buildValueTypes() {
        ImmutableMap.Builder<Class<?>, ValueType> map = ImmutableMap.builder();

        TriConsumer<Class<?>, Class<?>, ValueType> putPrimitiveType = (primitive, wrapper, valueType) -> {
            map.put(primitive, valueType);
            map.put(wrapper, valueType);
        };
        putPrimitiveType.accept(byte.class, Byte.class, ValueType.of(ByteTag::valueOf, (element) -> ((NumericTag) element).byteValue()));
        putPrimitiveType.accept(short.class, Short.class, ValueType.of(ShortTag::valueOf, (element) -> ((NumericTag) element).shortValue()));
        putPrimitiveType.accept(char.class, Character.class, ValueType.of((c) -> ShortTag.valueOf((short) (char) c), (element) -> (char) ((NumericTag) element).shortValue()));
        putPrimitiveType.accept(int.class, Integer.class, ValueType.of(IntTag::valueOf, (element) -> ((NumericTag) element).intValue()));
        putPrimitiveType.accept(long.class, Long.class, ValueType.of(LongTag::valueOf, (element) -> ((NumericTag) element).longValue()));
        putPrimitiveType.accept(float.class, Float.class, ValueType.of(FloatTag::valueOf, (element) -> ((NumericTag) element).floatValue()));
        putPrimitiveType.accept(double.class, Double.class, ValueType.of(DoubleTag::valueOf, (element) -> ((NumericTag) element).doubleValue()));
        putPrimitiveType.accept(boolean.class, Boolean.class, ValueType.of(ByteTag::valueOf, (element) -> ((NumericTag) element).byteValue() != 0));

        map.put(String.class, ValueType.of(StringTag::valueOf, tag -> ((StringTag) tag).value()));
        map.put(UUID.class, ValueType.of(uuid -> new IntArrayTag(UUIDUtil.uuidToIntArray((UUID) uuid)),
            tag -> UUIDUtil.uuidFromIntArray(((IntArrayTag) tag).getAsIntArray())));
        map.put(int[].class, ValueType.of(IntArrayTag::new, (element) -> ((IntArrayTag) element).getAsIntArray()));
        map.put(long[].class, ValueType.of(LongArrayTag::new, (element) -> ((LongArrayTag) element).getAsLongArray()));
        map.put(byte[].class, ValueType.of(ByteArrayTag::new, (element) -> ((ByteArrayTag) element).getAsByteArray()));

        return map.build();
    }

    public static CompoundTag serialize(NbtPersistent obj) {
        CompoundTag nbt = new CompoundTag();

        for (Field field : obj.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(NbtPersistentValue.class))
                continue;

            Object rawValue = fieldGet(field, obj);
            if (rawValue == null)
                continue;

            String key = getKey(field);
            ValueType type = simpleTypes.get(field.getType());

            Tag element;
            if (type != null)
                element = type.serializer.apply(rawValue);
            else if (NbtPersistent.class.isAssignableFrom(field.getType()))
                element = ((NbtPersistent) rawValue).serialize();
            else
                throw new RuntimeException("Cannot serialize field \"" + field.getType().getName() + " "
                    + field.getName() + "\", in class \"" + obj.getClass().getName() + "\"");
            nbt.put(key, element);
        }

        return nbt;
    }

    public static void deserialize(NbtPersistent obj, CompoundTag nbt) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(NbtPersistentValue.class))
                continue;

            String key = getKey(field);
            Tag element = nbt.get(key);
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
            } else if (element != null && type != null) {
                Object value = type.deserializer.apply(element);
                fieldSet(field, obj, value);
            } else if (element != null)
                throw new RuntimeException("Cannot deserialize field \"" + field.getType().getName() + " "
                    + field.getName() + "\", in class \"" + obj.getClass().getName() + "\"");
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
        Function<Object, Tag> serializer,
        Function<Tag, Object> deserializer
    ) {
        @SuppressWarnings("unchecked")
        private static <T> ValueType of(Function<T, Tag> serializer, Function<Tag, T> deserializer) {
            return new ValueType((obj) -> serializer.apply((T) obj), deserializer::apply);
        }
    }
}
