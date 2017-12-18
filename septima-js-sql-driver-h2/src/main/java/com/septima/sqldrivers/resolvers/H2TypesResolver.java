package com.septima.sqldrivers.resolvers;

import com.septima.application.ApplicationDataTypes;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author vv
 */
public class H2TypesResolver implements TypesResolver {

    private static final Map<String, String> rdbmsTypes2ApplicationTypes = new LinkedHashMap<>() {{
        put("VARCHAR", ApplicationDataTypes.STRING_TYPE_NAME);
        put("NUMERIC", ApplicationDataTypes.NUMBER_TYPE_NAME);
        put("DECIMAL", ApplicationDataTypes.NUMBER_TYPE_NAME);
        put("BOOLEAN", ApplicationDataTypes.BOOLEAN_TYPE_NAME);
        put("TIMESTAMP", ApplicationDataTypes.DATE_TYPE_NAME);
        put("TINYINT", ApplicationDataTypes.NUMBER_TYPE_NAME);
        put("BIGINT", ApplicationDataTypes.NUMBER_TYPE_NAME);
        put("IDENTITY", ApplicationDataTypes.NUMBER_TYPE_NAME);
        put("INTEGER", ApplicationDataTypes.NUMBER_TYPE_NAME);
        put("SMALLINT", ApplicationDataTypes.NUMBER_TYPE_NAME);
        put("FLOAT", ApplicationDataTypes.NUMBER_TYPE_NAME);
        put("REAL", ApplicationDataTypes.NUMBER_TYPE_NAME);
        put("DOUBLE", ApplicationDataTypes.NUMBER_TYPE_NAME);
        put("LONGVARCHAR", ApplicationDataTypes.STRING_TYPE_NAME);
        put("CHAR", ApplicationDataTypes.STRING_TYPE_NAME);
        put("VARCHAR_IGNORECASE", ApplicationDataTypes.STRING_TYPE_NAME);
        put("DATE", ApplicationDataTypes.DATE_TYPE_NAME);
        put("TIME", ApplicationDataTypes.DATE_TYPE_NAME);
        put("CLOB", ApplicationDataTypes.STRING_TYPE_NAME);
        put("LONGVARBINARY", null);
        put("VARBINARY", null);
        put("BINARY", null);
        put("UUID", null);
        put("OTHER", null);
        put("ARRAY", null);
        put("BLOB", null);
    }};
    private static final Set<String> jdbcTypesWithSize = new HashSet<>() {{
        add("LONGVARBINARY");
        add("VARBINARY");
        add("BINARY");
        add("UUID");
        add("LONGVARCHAR");
        add("CHAR");
        add("VARCHAR");
        add("VARCHAR_IGNORECASE");
        add("OTHER");
        add("BLOB");
        add("CLOB");
    }};
    private static final Set<String> jdbcTypesWithScale = new HashSet<>() {{
        add("NUMERIC");
        add("DECIMAL");
    }};
    private static final Map<String, Integer> jdbcTypesMaxSize = new HashMap<>() {{
        put("CHAR", 2147483647);
        put("VARCHAR", 2147483647);
        put("VARCHAR_IGNORECASE", 2147483647);
        put("BINARY", 2147483647);
        put("UUID", 2147483647);
        put("VARBINARY", 2147483647);
    }};
    private static final Map<String, Integer> jdbcTypesDefaultSize = new HashMap<>() {{
        put("CHAR", 1);
        put("VARCHAR", 200);
        put("VARCHAR_IGNORECASE", 200);
        put("BINARY", 1);
        put("UUID", 1);
        put("VARBINARY", 200);
    }};

    @Override
    public String toApplicationType(int aJdbcType, String aRdbmsTypeName) {
        return aRdbmsTypeName != null ? rdbmsTypes2ApplicationTypes.get(aRdbmsTypeName.toUpperCase()) : null;
    }

    @Override
    public Set<String> getSupportedTypes() {
        return Collections.unmodifiableSet(rdbmsTypes2ApplicationTypes.keySet());
    }

    @Override
    public boolean isSized(String aRdbmsTypeName) {
        return jdbcTypesWithSize.contains(aRdbmsTypeName.toUpperCase());
    }

    @Override
    public boolean isScaled(String aRdbmsTypeName) {
        return jdbcTypesWithScale.contains(aRdbmsTypeName.toUpperCase());
    }

    @Override
    public int resolveSize(String aRdbmsTypeName, int aSize) {
        if (aRdbmsTypeName != null) {
            // check on max size
            Integer maxSize = jdbcTypesMaxSize.getOrDefault(aRdbmsTypeName.toUpperCase(), Integer.MAX_VALUE);
            if (maxSize < aSize) {
                return maxSize;
            } else if (aSize <= 0 && jdbcTypesDefaultSize.containsKey(aRdbmsTypeName.toUpperCase())) {
                return jdbcTypesDefaultSize.get(aRdbmsTypeName.toUpperCase());
            } else {
                return aSize;
            }
        } else {
            return aSize;
        }
    }
}