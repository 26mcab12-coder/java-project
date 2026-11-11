package com.smartfoodmess.util;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * A very small hand-written JSON helper so the project does not need an
 * extra JSON library.
 *
 * <p>Serv letss build responses like:</p>
 * <pre>
 *   JsonUtil.write(resp, 200, JsonUtil.object(
 *           JsonUtil.field("success", true),
 *           JsonUtil.field("message", "Menu item saved.")));
 * </pre>
 *
 * <p>{@link #field(String, Object)} automatically decides how to print the
 * value: numbers without quotes, booleans as true/false, everything else
 * as an escaped "string".</p>
 */
public final class JsonUtil {

    private JsonUtil() {
        // utility class - cannot be instantiated
    }

    /** Escapes a value so it can be placed safely inside a JSON string. */
    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 8);
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * Builds one {@code "key":value} pair.
     *
     * <ul>
     *   <li>{@code Number}  - printed as a plain number (120.5)</li>
     *   <li>{@code Boolean} - printed as true / false</li>
     *   <li>{@code null}    - printed as null</li>
     *   <li>anything else   - printed as an escaped "string"
     *       (dates use their ISO form, e.g. 2026-09-25)</li>
     * </ul>
     */
    public static String field(String key, Object value) {
        String name = "\"" + escape(key) + "\"";
        if (value == null) {
            return name + ":null";
        }
        if (value instanceof Boolean || value instanceof Integer
                || value instanceof Long || value instanceof Short || value instanceof Byte) {
            return name + ":" + value;
        }
        if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                return name + ":null"; // NaN is not valid JSON
            }
            return name + ":" + value;
        }
        return name + ":\"" + escape(value.toString()) + "\"";
    }

    /** Joins field(...) pairs into a JSON object: {@code {a:1,b:2}} */
    public static String object(String... fields) {
        return "{" + String.join(",", fields) + "}";
    }

    /** Joins already-built objects into a JSON array: {@code [{...},{...}]} */
    public static String array(List<String> objects) {
        return "[" + String.join(",", objects) + "]";
    }

    /** Sends a JSON response with the given HTTP status code. */
    public static void write(HttpServletResponse resp, int status, String json) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json);
    }
}
