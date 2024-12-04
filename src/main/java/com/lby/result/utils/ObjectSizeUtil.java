package com.lby.result.utils;


import cn.hutool.core.util.ClassUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class ObjectSizeUtil {
    /**
     * 对象嵌套深度最多进入n层
     */
    public static int MAX_DEPTH = 3;
    /**
     * 对象父类字段最多查找n层
     */
    public static int MAX_SUPER_CLASS_FIELD_DEPTH = 3;
    /**
     * 单位
     */
    public static final String[] UNIT_NAMES = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB"};
    /**
     * 字段缓存
     */
    private static final Map<Class<?>, Field[]> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取对象大小，接近实际内存大小，不完全等于实际大小，用于快速计算内存大小
     * 性能测试：三层带List的对象，百万次调用，耗时 196ms
     *
     * @param o
     * @return
     */
    public static long estimateObjectSize(Object o) {
        return estimateObjectSize(o, 0);
    }

    /**
     * 获取对象大小，接近实际内存大小，不完全等于实际大小，用于快速计算内存大小
     * 性能测试：三层带List的对象，百万次调用，耗时 196ms
     *
     * @param o
     * @param depth
     * @return
     */
    private static long estimateObjectSize(Object o, int depth) {
        if (o == null) {
            return 4L;
        }
        // 深度兜底
        if (depth >= MAX_DEPTH) {
            return 4L;
        }
        // 基本类型、基本类型包装类、枚举、数组、集合、Map、注解、其他类
        Class<?> cls = o.getClass();
        // 64位机器上每个对象至少有16字节的开销
        long size = 16L;
        // 基本类和包装对象 int 的class是java.lang.Integer
        if (ClassUtil.isBasicType(cls)) {
            if (cls == Integer.class || cls == Float.class) {
                size += 4L;
            } else if (cls == Long.class || cls == Double.class) {
                size += 8L;
            } else if (cls == Short.class || cls == Byte.class) {
                size += 2L;
            } else if (cls == Character.class || cls == Boolean.class) {
                size += 1L;
            }
        } else if (cls == String.class) {
            // 字符串，每个字符2字节
            size += 2L * ((String) o).length();
        } else if (cls.isEnum()) {
            // 枚举为常量，内存占用视为一个对象引用地址4字节
            size = 4L;
        } else if (cls.isAnnotation()) {
            // 注解 内存占用视为一个对象引用地址4字节
            size = 4L;
        } else if (cls.isArray()) {
            // 数组 遍历数组中的每个元素，递归调用estimateObjectSize方法,数组里每个对象指针4字节
            int len = Array.getLength(o);
            for (int i = 0; i < len; i++) {
                size += estimateObjectSize(Array.get(o, i), depth + 1) + 4;
            }
        } else if (o instanceof Collection) {
            // 集合 遍历集合中的每个元素，递归调用estimateObjectSize方法
            Collection<?> collection = (Collection<?>) o;
            if (!collection.isEmpty()) {
                for (Object obj : collection) {
                    // 集合里每个对象指针4字节
                    size += estimateObjectSize(obj, depth + 1) + 4;
                }
            }
        } else if (o instanceof Map) {
            // Map 遍历Map中的每个元素，递归调用estimateObjectSize方法
            Map<?, ?> map = (Map<?, ?>) o;
            if (!map.isEmpty()) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    // 集合里每个对象指针4字节 *2
                    size += 8;
                    size += estimateObjectSize(entry.getKey(), depth + 1);
                    size += estimateObjectSize(entry.getValue(), depth + 1);
                }
            }
        } else {
            // 其他类，遍历类中的每个字段，递归调用estimateObjectSize方法，计算字段的内存占用，field获取每次执行都会拷贝，所以用缓存
            Field[] declaredFields = FIELD_CACHE.computeIfAbsent(cls, k -> {
                // 获取类声明的字段，会拷贝Field[]
                Field[] fields = getDeclaredFields(cls);
                // 设置可访问
                for (Field field : fields) {
                    field.setAccessible(true);
                }
                return fields;
            });
            for (Field declaredField : declaredFields) {
                try {
                    // 静态的字段直接算一个指针的内存
                    if (Modifier.isStatic(declaredField.getModifiers())) {
                        size += 4L;
                    } else {
                        // 当前字段的这个指针的内存占用加指向的对象内存占用
                        Object object = declaredField.get(o);
                        size += estimateObjectSize(object, depth + 1) + 4;
                    }
                } catch (Exception e) {
                    log.error("获取对象大小异常", e);
                }
            }
        }
        return size;
    }

    /**
     * 获取类声明的字段
     *
     * @param cls
     * @return
     */
    private static Field[] getDeclaredFields(Class<?> cls) {
        Set<Field> set = new LinkedHashSet<>();
        Class<?> currentClass = cls;
        int depth = 0;
        while (!Object.class.equals(currentClass) && depth < MAX_SUPER_CLASS_FIELD_DEPTH) {
            set.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
            depth++;
        }
        return set.toArray(new Field[0]);
    }

    /**
     * 格式化字节数
     *
     * @param size
     * @return
     */
    public static String formatSize(long size) {
        if (size <= 0) {
            return "0";
        }
        int digitGroups = Math.min(UNIT_NAMES.length - 1, (int) (Math.log10(size) / Math.log10(1024)));
        return new DecimalFormat("#,##0.##")
                .format(size / Math.pow(1024, digitGroups)) + " " + UNIT_NAMES[digitGroups];
    }
}