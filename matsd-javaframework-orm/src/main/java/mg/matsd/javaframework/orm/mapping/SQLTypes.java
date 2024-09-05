package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.annotations.Nullable;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public enum SQLTypes {
    INTEGER, BIGINT, SMALLINT, FLOAT, REAL, DOUBLE, DECIMAL, NUMERIC,
    CHAR, VARCHAR, TEXT,
    DATE, TIME, TIMESTAMP,
    BOOLEAN;

    private static final Map<Class<?>, Object> javaClassToSqlTypeMap = new HashMap<>();

    static {
        javaClassToSqlTypeMap.put(int.class, INTEGER);
        javaClassToSqlTypeMap.put(Integer.class, INTEGER);

        javaClassToSqlTypeMap.put(long.class, BIGINT);
        javaClassToSqlTypeMap.put(Long.class, BIGINT);

        javaClassToSqlTypeMap.put(short.class, SMALLINT);
        javaClassToSqlTypeMap.put(Short.class, SMALLINT);

        List<SQLTypes> sqlTypesForFloat = Arrays.asList(REAL, FLOAT);
        javaClassToSqlTypeMap.put(float.class, sqlTypesForFloat);
        javaClassToSqlTypeMap.put(Float.class, sqlTypesForFloat);

        List<SQLTypes> sqlTypesForDouble = Arrays.asList(DOUBLE, DECIMAL, NUMERIC);
        javaClassToSqlTypeMap.put(double.class, sqlTypesForDouble);
        javaClassToSqlTypeMap.put(Double.class, sqlTypesForDouble);

        javaClassToSqlTypeMap.put(char.class, CHAR);
        javaClassToSqlTypeMap.put(Character.class, CHAR);

        List<SQLTypes> sqlTypesForString = Arrays.asList(VARCHAR, TEXT);
        javaClassToSqlTypeMap.put(String.class, sqlTypesForString);

        javaClassToSqlTypeMap.put(Date.class, DATE);
        javaClassToSqlTypeMap.put(LocalDate.class, DATE);

        javaClassToSqlTypeMap.put(Time.class, TIME);
        javaClassToSqlTypeMap.put(LocalTime.class, TIME);

        javaClassToSqlTypeMap.put(Timestamp.class, TIMESTAMP);
        javaClassToSqlTypeMap.put(LocalDateTime.class, TIMESTAMP);

        javaClassToSqlTypeMap.put(boolean.class, BOOLEAN);
        javaClassToSqlTypeMap.put(Boolean.class, BOOLEAN);
    }

    @Nullable
    public static Object getCorrespondingSqlType(Class<?> clazz) {
        if (!javaClassToSqlTypeMap.containsKey(clazz))
            return null;

        return javaClassToSqlTypeMap.get(clazz);
    }
}
