package cn.welsione.jprompt.engine;

import cn.welsione.jprompt.util.TemplateUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

/**
 * 内置模板函数注册器。
 */
public final class BuiltinFunctions {

    private BuiltinFunctions() {
    }

    /**
     * 将所有内置函数注册到引擎。
     */
    public static void registerAll(TemplateEngine engine) {
        registerStringFunctions(engine);
        registerCollectionFunctions(engine);
        registerDateFunctions(engine);
        registerLogicFunctions(engine);
        registerMathFunctions(engine);
    }

    private static void registerStringFunctions(TemplateEngine engine) {
        engine.registerFunction("upperCase", args -> args.length > 0 && args[0] != null
                ? args[0].toString().toUpperCase() : "");
        engine.registerFunction("lowerCase", args -> args.length > 0 && args[0] != null
                ? args[0].toString().toLowerCase() : "");
        engine.registerFunction("capitalize", args -> args.length > 0 && args[0] != null
                ? capitalize(args[0].toString()) : "");
        engine.registerFunction("trim", args -> args.length > 0 && args[0] != null
                ? args[0].toString().trim() : "");
    }

    private static void registerCollectionFunctions(TemplateEngine engine) {
        engine.registerFunction("length", args -> {
            if (args.length > 0 && args[0] != null) {
                Object arg = args[0];
                if (arg instanceof Collection) {
                    return String.valueOf(((Collection<?>) arg).size());
                } else if (arg instanceof Map) {
                    return String.valueOf(((Map<?, ?>) arg).size());
                } else if (arg instanceof String) {
                    return String.valueOf(((String) arg).length());
                } else if (arg.getClass().isArray()) {
                    return String.valueOf(java.lang.reflect.Array.getLength(arg));
                }
            }
            return "0";
        });

        engine.registerFunction("join", args -> {
            if (args.length > 0 && args[0] instanceof Collection) {
                String separator = args.length > 1 ? args[1].toString() : ",";
                Collection<?> collection = (Collection<?>) args[0];
                return String.join(separator, collection.stream()
                        .map(Object::toString)
                        .toArray(String[]::new));
            }
            return args.length > 0 ? args[0].toString() : "";
        });
    }

    private static void registerDateFunctions(TemplateEngine engine) {
        engine.registerFunction("formatDate", args -> {
            if (args.length > 0 && args[0] != null) {
                String pattern = args.length > 1 ? args[1].toString() : "yyyy-MM-dd";
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                    if (args[0] instanceof LocalDateTime) {
                        return ((LocalDateTime) args[0]).format(formatter);
                    } else if (args[0] instanceof LocalDate) {
                        return ((LocalDate) args[0]).format(formatter);
                    } else if (args[0] instanceof java.util.Date date) {
                        LocalDateTime ldt = Instant.ofEpochMilli(date.getTime())
                                .atZone(ZoneId.systemDefault()).toLocalDateTime();
                        return ldt.format(formatter);
                    }
                } catch (Exception e) {
                    return args[0].toString();
                }
            }
            return "";
        });
    }

    private static void registerLogicFunctions(TemplateEngine engine) {
        engine.registerFunction("ternary", args -> args.length >= 3
                ? (TemplateUtils.isTruthy(args[0]) ? args[1] : args[2]).toString()
                : "");
        engine.registerFunction("default", args -> args.length > 0
                ? (TemplateUtils.isTruthy(args[0]) ? args[0] : args.length > 1 ? args[1] : "").toString()
                : "");
    }

    private static void registerMathFunctions(TemplateEngine engine) {
        engine.registerFunction("max", args -> {
            if (args.length >= 2) {
                double left = toDouble(args[0]);
                double right = toDouble(args[1]);
                return String.valueOf((int) Math.max(left, right));
            }
            return "0";
        });
        engine.registerFunction("min", args -> {
            if (args.length >= 2) {
                double left = toDouble(args[0]);
                double right = toDouble(args[1]);
                return String.valueOf((int) Math.min(left, right));
            }
            return "0";
        });
    }

    private static double toDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
