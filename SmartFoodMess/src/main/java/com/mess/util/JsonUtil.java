package com.mess.util;

import java.util.Iterator;
import java.util.Map;
import java.lang.reflect.Array;

/** Writes the JSON types used by the application's read-only API. */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static String stringify(Object value) {
        StringBuilder json = new StringBuilder();
        appendValue(json, value);
        return json.toString();
    }

    private static void appendValue(StringBuilder json, Object value) {
        if (value == null) {
            json.append("null");
        } else if (value instanceof String || value instanceof Character
                || value instanceof java.time.temporal.TemporalAccessor) {
            appendString(json, value.toString());
        } else if (value instanceof Number || value instanceof Boolean) {
            json.append(value);
        } else if (value instanceof Map<?, ?>) {
            json.append('{');
            Iterator<? extends Map.Entry<?, ?>> entries = ((Map<?, ?>) value).entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<?, ?> entry = entries.next();
                appendString(json, String.valueOf(entry.getKey()));
                json.append(':');
                appendValue(json, entry.getValue());
                if (entries.hasNext()) {
                    json.append(',');
                }
            }
            json.append('}');
        } else if (value instanceof Iterable<?>) {
            json.append('[');
            Iterator<?> items = ((Iterable<?>) value).iterator();
            while (items.hasNext()) {
                appendValue(json, items.next());
                if (items.hasNext()) {
                    json.append(',');
                }
            }
            json.append(']');
        } else if (value.getClass().isArray()) {
            json.append('[');
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                appendValue(json, Array.get(value, i));
                if (i + 1 < length) {
                    json.append(',');
                }
            }
            json.append(']');
        } else {
            appendString(json, value.toString());
        }
    }

    private static void appendString(StringBuilder json, String value) {
        json.append('"');
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"':
                    json.append("\\\"");
                    break;
                case '\\':
                    json.append("\\\\");
                    break;
                case '\b':
                    json.append("\\b");
                    break;
                case '\f':
                    json.append("\\f");
                    break;
                case '\n':
                    json.append("\\n");
                    break;
                case '\r':
                    json.append("\\r");
                    break;
                case '\t':
                    json.append("\\t");
                    break;
                default:
                    if (character < 0x20) {
                        json.append(String.format("\\u%04x", (int) character));
                    } else {
                        json.append(character);
                    }
            }
        }
        json.append('"');
    }
}
