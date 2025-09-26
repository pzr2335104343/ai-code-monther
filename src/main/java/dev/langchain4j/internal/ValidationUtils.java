//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.langchain4j.internal;

import dev.langchain4j.Internal;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@Internal
public class ValidationUtils {
    private ValidationUtils() {
    }

    public static void ensureEq(Object lhs, Object rhs, String format, Object... args) {
        if (!Objects.equals(lhs, rhs)) {
            throw Exceptions.illegalArgument(format, args);
        }
    }

    public static <T> T ensureNotNull(T object, String name) {
        return (T)ensureNotNull(object, "%s cannot be null", name);
    }

    public static <T> T ensureNotNull(T object, String format, Object... args) {
        if (object == null) {
            throw Exceptions.illegalArgument(format, args);
        } else {
            return object;
        }
    }

    public static <T extends Collection<?>> T ensureNotEmpty(T collection, String name) {
        if (Utils.isNullOrEmpty(collection)) {
            throw Exceptions.illegalArgument("%s cannot be null or empty", new Object[]{name});
        } else {
            return collection;
        }
    }

    public static <T> T[] ensureNotEmpty(T[] array, String name) {
        return (T[])ensureNotEmpty(array, "%s cannot be null or empty", name);
    }

    public static <T> T[] ensureNotEmpty(T[] array, String format, Object... args) {
        if (array != null && array.length != 0) {
            return array;
        } else {
            throw Exceptions.illegalArgument(format, args);
        }
    }

    public static <K, V> Map<K, V> ensureNotEmpty(Map<K, V> map, String name) {
        if (Utils.isNullOrEmpty(map)) {
            throw Exceptions.illegalArgument("%s cannot be null or empty", new Object[]{name});
        } else {
            return map;
        }
    }

    public static String ensureNotEmpty(String string, String name) {
        return ensureNotEmpty(string, "%s cannot be null or empty", name);
    }

    public static String ensureNotEmpty(String string, String format, Object... args) {
        if (Utils.isNullOrEmpty(string)) {
            throw Exceptions.illegalArgument(format, args);
        } else {
            return string;
        }
    }

    public static String ensureNotBlank(String string, String name) {
        return ensureNotBlank(string, "%s cannot be null or blank", name);
    }

    public static String ensureNotBlank(String string, String format, Object... args) {
        if (Utils.isNullOrBlank(string)) {
            throw Exceptions.illegalArgument(format, args);
        } else {
            return string;
        }
    }

    public static void ensureTrue(boolean expression, String msg) {
        if (!expression) {
            throw Exceptions.illegalArgument(msg, new Object[0]);
        }
    }

    public static int ensureNotNegative(Integer i, String name) {
        if (i != null && i >= 0) {
            return i;
        } else {
            throw Exceptions.illegalArgument("%s must not be negative, but is: %s", new Object[]{name, i});
        }
    }

    public static int ensureGreaterThanZero(Integer i, String name) {
        if (i != null && i > 0) {
            return i;
        } else {
            throw Exceptions.illegalArgument("%s must be greater than zero, but is: %s", new Object[]{name, i});
        }
    }

    public static double ensureGreaterThanZero(Double i, String name) {
        if (i != null && !(i <= (double)0.0F)) {
            return i;
        } else {
            throw Exceptions.illegalArgument("%s must be greater than zero, but is: %s", new Object[]{name, i});
        }
    }

    public static double ensureBetween(Double d, double min, double max, String name) {
        if (d != null && !(d < min) && !(d > max)) {
            return d;
        } else {
            throw Exceptions.illegalArgument("%s must be between %s and %s, but is: %s", new Object[]{name, min, max, d});
        }
    }

    public static int ensureBetween(Integer i, int min, int max, String name) {
        if (i != null && i >= min && i <= max) {
            return i;
        } else {
            throw Exceptions.illegalArgument("%s must be between %s and %s, but is: %s", new Object[]{name, min, max, i});
        }
    }

    public static long ensureBetween(Long i, long min, long max, String name) {
        if (i != null && i >= min && i <= max) {
            return i;
        } else {
            throw Exceptions.illegalArgument("%s must be between %s and %s, but is: %s", new Object[]{name, min, max, i});
        }
    }
}
