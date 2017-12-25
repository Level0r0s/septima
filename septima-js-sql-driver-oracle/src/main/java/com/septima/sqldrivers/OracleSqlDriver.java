package com.septima.sqldrivers;

import com.septima.metadata.ForeignKey;
import com.septima.metadata.JdbcColumn;
import com.septima.metadata.PrimaryKey;
import com.septima.sqldrivers.resolvers.OracleTypesResolver;
import com.septima.sqldrivers.resolvers.TypesResolver;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import oracle.jdbc.OracleConnection;
import oracle.sql.STRUCT;
import org.geotools.data.oracle.sdo.GeometryConverter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mg
 */
public class OracleSqlDriver extends SqlDriver {

    private static final String ORACLE_DIALECT = "Oracle";
    private static final char ESCAPE = '"';

    private static final OracleTypesResolver resolver = new OracleTypesResolver();
    private static final String GET_SCHEMA_CLAUSE = "SELECT sys_context('USERENV', 'CURRENT_SCHEMA') FROM DUAL";
    private static final String CREATE_SCHEMA_CLAUSE = "CREATE USER %s IDENTIFIED BY %s";
    private static final String RENAME_FIELD_SQL_PREFIX = "alter table %s rename column %s transform %s";
    private static final String MODIFY_FIELD_SQL_PREFIX = "alter table %s modify ";

    public OracleSqlDriver(){
        super();
    }

    @Override
    public String getDialect() {
        return ORACLE_DIALECT;
    }

    @Override
    public boolean is(String aJdbcUrl) {
        return aJdbcUrl.contains("jdbc:oracle");
    }

    @Override
    public String getSql4DropTable(String aSchemaName, String aTableName) {
        return "drop table " + makeFullName(aSchemaName, aTableName);
    }

    @Override
    public String getSql4EmptyTableCreation(String aSchemaName, String aTableName, String aPkFieldName) {
        String fullName = makeFullName(aSchemaName, aTableName);
        aPkFieldName = escapeNameIfNeeded(aPkFieldName);
        return "CREATE TABLE " + fullName + " ("
                + aPkFieldName + " NUMBER NOT NULL,"
                + "CONSTRAINT " + escapeNameIfNeeded(aTableName + PRIMARY_KEY_NAME_SUFFIX) + " PRIMARY KEY (" + aPkFieldName + "))";
    }

    private String getFieldTypeDefinition(JdbcColumn aField) {
        String typeDefine = "";
        String sqlTypeName = aField.getType().toUpperCase();
        typeDefine += sqlTypeName;
        // field length
        int size = aField.getSize();
        if (size > 0) {
            int scale = aField.getScale();
            if (resolver.isScaled(sqlTypeName) && resolver.isSized(sqlTypeName)) {
                typeDefine += "(" + String.valueOf(size) + "," + String.valueOf(scale) + ")";
            } else if (resolver.isSized(sqlTypeName)) {
                typeDefine += "(" + String.valueOf(size) + ")";
            }
        }
        return typeDefine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSql4FieldDefinition(JdbcColumn aField) {
        String fieldDefinition = escapeNameIfNeeded(aField.getName()) + " " + getFieldTypeDefinition(aField);

        if (aField.isNullable()) {
            fieldDefinition += " null";
        } else {
            fieldDefinition += " not null";
        }
        return fieldDefinition;
    }

    @Override
    public String getSql4DropFkConstraint(String aSchemaName, ForeignKey aFk) {
        String constraintName = escapeNameIfNeeded(aFk.getCName());
        String tableName = makeFullName(aSchemaName, aFk.getTable());
        return "alter table " + tableName + " drop constraint " + constraintName;
    }

    @Override
    public String getSql4GetSchema() {
        return GET_SCHEMA_CLAUSE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getSqls4FieldModify(String aSchemaName, String aTableName, JdbcColumn aOldFieldMd, JdbcColumn aNewField) {
        List<String> sqls = new ArrayList<>();
        String fullTableName = makeFullName(aSchemaName, aTableName);
        String updateDefinition = String.format(MODIFY_FIELD_SQL_PREFIX, fullTableName) + escapeNameIfNeeded(aOldFieldMd.getName()) + " ";
        String fieldDefination = getFieldTypeDefinition(aNewField);

        String newSqlTypeName = aNewField.getType();
        if (newSqlTypeName == null) {
            newSqlTypeName = "";
        }
        int newScale = aNewField.getScale();
        int newSize = aNewField.getSize();
        boolean newNullable = aNewField.isNullable();

        String oldSqlTypeName = aOldFieldMd.getType();
        if (oldSqlTypeName == null) {
            oldSqlTypeName = "";
        }
        int oldScale = aOldFieldMd.getScale();
        int oldSize = aOldFieldMd.getSize();
        boolean oldNullable = aOldFieldMd.isNullable();

        if (!oldSqlTypeName.equalsIgnoreCase(newSqlTypeName)
                || (resolver.isSized(newSqlTypeName) && newSize != oldSize)
                || (resolver.isScaled(newSqlTypeName) && newScale != oldScale)) {
            sqls.add(updateDefinition + fieldDefination);
        }
        if (oldNullable != newNullable) {
            sqls.add(updateDefinition + (newNullable ? " null" : " not null"));
        }
        return sqls.toArray(new String[sqls.size()]);
    }

    @Override
    public String[] getSqls4FieldRename(String aSchemaName, String aTableName, String aOldFieldName, JdbcColumn aNewFieldMd) {
        String fullTableName = makeFullName(aSchemaName, aTableName);
        String sqlText = String.format(RENAME_FIELD_SQL_PREFIX, fullTableName, escapeNameIfNeeded(aOldFieldName), escapeNameIfNeeded(aNewFieldMd.getName()));
        return new String[]{
                sqlText
        };
    }

    @Override
    public String[] getSqls4CreateColumnComment(String aOwnerName, String aTableName, String aFieldName, String aDescription) {
        String ownerName = escapeNameIfNeeded(aOwnerName);
        String tableName = escapeNameIfNeeded(aTableName);
        String fieldName = escapeNameIfNeeded(aFieldName);
        String sqlText = ownerName == null ? String.join(".", tableName, fieldName) : String.join(".", ownerName, tableName, fieldName);
        if (aDescription == null) {
            aDescription = "";
        }
        return new String[]{"comment on column " + sqlText + " is '" + aDescription.replaceAll("'", "''") + "'"};
    }

    @Override
    public String getSql4CreateTableComment(String aOwnerName, String aTableName, String aDescription) {
        String sqlText = String.join(".", escapeNameIfNeeded(aOwnerName), escapeNameIfNeeded(aTableName));
        if (aDescription == null) {
            aDescription = "";
        }
        return "comment on table " + sqlText + " is '" + aDescription.replaceAll("'", "''") + "'";
    }

    @Override
    public TypesResolver getTypesResolver() {
        return resolver;
    }

    @Override
    public String getSql4CreateSchema(String aSchemaName, String aPassword) {
        if (aSchemaName == null || aSchemaName.isEmpty()) {
            throw new IllegalArgumentException("Schema name is null or empty.");
        }
        if (aPassword == null || aPassword.isEmpty()) {
            throw new IllegalArgumentException("Schema owner password is null or empty.");
        }
        return String.format(CREATE_SCHEMA_CLAUSE, aSchemaName, "");
    }

    @Override
    public String getSql4DropPkConstraint(String aSchemaName, PrimaryKey aPk) {
        String tableName = makeFullName(aSchemaName, aPk.getTable());
        return "alter table " + tableName + " drop primary key";
    }

    @Override
    public String getSql4CreateFkConstraint(String aSchemaName, List<ForeignKey> listFk) {
        if (listFk != null && listFk.size() > 0) {
            ForeignKey fk = listFk.get(0);
            PrimaryKey pk = fk.getReferee();
            String fkTableName = makeFullName(aSchemaName, fk.getTable());
            String pkTableName = makeFullName(aSchemaName, pk.getTable());
            String fkName = fk.getCName();

            // String pkSchemaName = pk.getSchema();
            StringBuilder fkColumnName = new StringBuilder(escapeNameIfNeeded(fk.getField()));
            StringBuilder pkColumnName = new StringBuilder(escapeNameIfNeeded(pk.getField()));

            for (int i = 1; i < listFk.size(); i++) {
                fk = listFk.get(i);
                pk = fk.getReferee();
                fkColumnName.append(", ").append(escapeNameIfNeeded(fk.getField()));
                pkColumnName.append(", ").append(escapeNameIfNeeded(pk.getField()));
            }

            String fkRule = "";
            switch (fk.getDeleteRule()) {
                case CASCADE:
                    fkRule += " ON DELETE CASCADE ";
                    break;
                case NO_ACTION:
                case SET_DEFAULT:
//                    fkRule += " ON DELETE NO ACTION ";
                    break;
//                case SET_DEFAULT:
//                    break;
                case SET_NULL:
                    fkRule += " ON DELETE SET NULL ";
                    break;
            }
            if (fk.isDeferrable()) {
                fkRule += " DEFERRABLE INITIALLY DEFERRED";
            }
            return String.format("ALTER TABLE %s ADD (CONSTRAINT %s"
                    + " FOREIGN KEY (%s) REFERENCES %s (%s) %s)", fkTableName, fkName.isEmpty() ? "" : escapeNameIfNeeded(fkName), fkColumnName.toString(), pkTableName, pkColumnName.toString(), fkRule);
        }
        return null;
    }

    @Override
    public String[] getSqls4CreatePkConstraint(String aSchemaName, List<PrimaryKey> listPk) {
        if (listPk != null && listPk.size() > 0) {
            PrimaryKey pk = listPk.get(0);
            String tableName = pk.getTable();
            String pkName = escapeNameIfNeeded(tableName + PRIMARY_KEY_NAME_SUFFIX);
            StringBuilder pkColumnName = new StringBuilder(escapeNameIfNeeded(pk.getField()));
            for (int i = 1; i < listPk.size(); i++) {
                pk = listPk.get(i);
                pkColumnName.append(", ").append(escapeNameIfNeeded(pk.getField()));
            }
            return new String[]{
                    String.format("ALTER TABLE %s ADD CONSTRAINT %s PRIMARY KEY (%s)", makeFullName(aSchemaName, tableName), pkName, pkColumnName.toString())
            };
        }
        return null;
    }

    @Override
    public boolean isConstraintsDeferrable() {
        return true;
    }

    @Override
    public String[] getSqls4FieldAdd(String aSchemaName, String aTableName, JdbcColumn aField) {
        return new String[]{
                String.format(SqlDriver.ADD_FIELD_SQL_PREFIX, makeFullName(aSchemaName, aTableName)) +
                        getSql4FieldDefinition(aField)
        };
    }

    @Override
    public char getEscape() {
        return ESCAPE;
    }

    @Override
    public NamedJdbcValue geometryFromWkt(String aName, String aValue, Connection aConnection) throws SQLException {
        try {
            return new NamedJdbcValue(
                    aName,
                    aValue != null ?
                            new GeometryConverter(!(aConnection instanceof OracleConnection) ? aConnection.unwrap(OracleConnection.class) : (OracleConnection) aConnection)
                                    .toSDO(new WKTReader().read(aValue)) :
                            null,
                    Types.STRUCT,
                    "MDSYS.SDO_GEOMETRY");
        } catch (ParseException ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public String geometryToWkt(Wrapper aRs, int aColumnIndex, Connection aConnection) throws SQLException {
        Object read = aRs instanceof ResultSet ? ((ResultSet) aRs).getObject(aColumnIndex) : ((CallableStatement) aRs).getObject(aColumnIndex);
        boolean wasNull = aRs instanceof ResultSet ? ((ResultSet) aRs).wasNull() : ((CallableStatement) aRs).wasNull();
        if (wasNull) {
            return null;
        } else {
            if (read instanceof STRUCT) {
                STRUCT struct = (STRUCT) read;
                GeometryConverter reader = new GeometryConverter(struct.getInternalConnection());
                Geometry geometry = reader.asGeometry(struct);
                WKTWriter writer = new WKTWriter();
                return writer.write(geometry);
            } else {
                return null;
            }
        }
    }
}
