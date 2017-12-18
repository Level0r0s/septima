package com.septima.sqldrivers;

import com.septima.jdbc.NamedJdbcValue;
import com.septima.metadata.*;
import com.septima.metadata.ForeignKey;
import com.septima.sqldrivers.resolvers.MsSqlTypesResolver;
import com.septima.sqldrivers.resolvers.TypesResolver;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mg
 */
public class MsSqlSqlDriver extends SqlDriver {

    private static final String MSSQL_DIALECT = "MsSql";
    private static final Character ESCAPE = '"';

    private static final String GET_SCHEMA_CLAUSE = "SELECT SCHEMA_NAME()";
    private static final String CREATE_SCHEMA_CLAUSE = "CREATE SCHEMA %s";
    private static final MsSqlTypesResolver resolver = new MsSqlTypesResolver();
    private static final String ADD_COLUMN_COMMENT_CLAUSE = ""
            + "begin "
            + "begin try "
            + "EXEC sys.sp_dropextendedproperty @name=N'MS_Description' , @level0type=N'SCHEMA',@level0name=N'%s', @level1type=N'TABLE',@level1name=N'%s', @level2type=N'COLUMN',@level2name=N'%s' "
            + "end try "
            + "begin catch "
            + "end catch "
            + "EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'%s' , @level0type=N'SCHEMA',@level0name=N'%s', @level1type=N'TABLE',@level1name=N'%s', @level2type=N'COLUMN',@level2name=N'%s' "
            + " commit "
            + "end ";
    private static final String ADD_TABLE_COMMENT_CLAUSE = ""
            + "begin "
            + "  begin try "
            + "    EXEC sys.sp_dropextendedproperty @name=N'MS_Description' , @level0type=N'SCHEMA',@level0name=N'%s', @level1type=N'TABLE',@level1name=N'%s'"
            + "  end try  "
            + "  begin catch "
            + "  end catch  "
            + "  EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'%s' , @level0type=N'SCHEMA',@level0name=N'%s', @level1type=N'TABLE',@level1name=N'%s' "
            + " commit "
            + "end ";
    private static final String ALTER_FIELD_SQL_PREFIX = "alter table %s alter column ";

    public MsSqlSqlDriver() {
        super();
    }

    @Override
    public String getDialect() {
        return MSSQL_DIALECT;
    }

    @Override
    public boolean is(String aJdbcUrl) {
        return aJdbcUrl.contains("jdbc:jtds:sqlserver");
    }

    @Override
    public String getSql4DropTable(String aSchemaName, String aTableName) {
        String fullName = makeFullName(aSchemaName, aTableName);
        return "drop table " + fullName;
    }

    @Override
    public String getSql4EmptyTableCreation(String aSchemaName, String aTableName, String aPkFieldName) {
        String fullName = makeFullName(aSchemaName, aTableName);
        return "CREATE TABLE " + fullName + " ("
                + escapeNameIfNeeded(aPkFieldName) + " NUMERIC(18, 0) NOT NULL,"
                + "CONSTRAINT " + escapeNameIfNeeded(generatePkName(aTableName, PKEY_NAME_SUFFIX)) + " PRIMARY KEY (" + escapeNameIfNeeded(aPkFieldName) + " ASC))";
    }

    @Override
    public String parseException(Exception ex) {
        return ex != null ? ex.getLocalizedMessage() : null;
    }

    private String getFieldTypeDefinition(JdbcColumn aField) {
        String typeDefine = "";
        String sqlTypeName = aField.getType().toLowerCase();
        typeDefine += sqlTypeName;
        // field length
        int size = aField.getSize();
        int scale = aField.getScale();

        if (resolver.isScaled(sqlTypeName) && resolver.isSized(sqlTypeName) && size > 0) {
            typeDefine += "(" + String.valueOf(size) + "," + String.valueOf(scale) + ")";
        } else {
            if (resolver.isSized(sqlTypeName) && size > 0) {
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
        String fieldName = escapeNameIfNeeded(aField.getName());
        String fieldDefinition = fieldName + " " + getFieldTypeDefinition(aField);

        if (!aField.isNullable()) {
            fieldDefinition += " not null";
        } else {
            fieldDefinition += " null";
        }
        return fieldDefinition;
    }

    @Override
    public String getSql4DropFkConstraint(String aSchemaName, ForeignKey aFk) {
        String tableName = makeFullName(aSchemaName, aFk.getTable());
        return "ALTER TABLE " + tableName + " DROP CONSTRAINT " + escapeNameIfNeeded(aFk.getCName());
    }

    @Override
    public String getSql4CreateFkConstraint(String aSchemaName, ForeignKey aFk) {
        List<ForeignKey> fkList = new ArrayList<>();
        fkList.add(aFk);
        return getSql4CreateFkConstraint(aSchemaName, fkList);
    }

    @Override
    public String getSql4GetSchema() {
        return GET_SCHEMA_CLAUSE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getSqls4ModifyingField(String aSchemaName, String aTableName, JdbcColumn aOldFieldMd, JdbcColumn aNewFieldMd) {
        String fullTableName = makeFullName(aSchemaName, aTableName);
        String alterFieldSql = String.format(ALTER_FIELD_SQL_PREFIX, fullTableName);
        return new String[]{alterFieldSql + getSql4FieldDefinition(aNewFieldMd)};
    }

    @Override
    public String[] getSqls4RenamingField(String aSchemaName, String aTableName, String aOldFieldName, JdbcColumn aNewFieldMd) {
        String fullTableName = makeFullName(aSchemaName, aTableName);
        String sql = String.format("EXEC sp_rename '%s.%s','%s','COLUMN'", fullTableName, aOldFieldName, aNewFieldMd.getName());
        return new String[]{sql};
    }

    @Override
    public String[] getSql4CreateColumnComment(String aOwnerName, String aTableName, String aFieldName, String aDescription) {
        if (aDescription == null) {
            aDescription = "";
        }
        return new String[]{String.format(ADD_COLUMN_COMMENT_CLAUSE, unescapeNameIfNeeded(aOwnerName), unescapeNameIfNeeded(aTableName), unescapeNameIfNeeded(aFieldName), aDescription, unescapeNameIfNeeded(aOwnerName), unescapeNameIfNeeded(aTableName), unescapeNameIfNeeded(aFieldName))};
    }

    @Override
    public String getSql4CreateTableComment(String aOwnerName, String aTableName, String aDescription) {
        if (aDescription == null) {
            aDescription = "";
        }
        return String.format(ADD_TABLE_COMMENT_CLAUSE, unescapeNameIfNeeded(aOwnerName), unescapeNameIfNeeded(aTableName), aDescription, unescapeNameIfNeeded(aOwnerName), unescapeNameIfNeeded(aTableName));
    }

    @Override
    public TypesResolver getTypesResolver() {
        return resolver;
    }

    @Override
    public String getSql4DropIndex(String aSchemaName, String aTableName, String aIndexName) {
        aTableName = makeFullName(aSchemaName, aTableName);
        return "drop index " + escapeNameIfNeeded(aIndexName) + " on " + aTableName;
    }

    @Override
    public String getSql4CreateIndex(String aSchemaName, String aTableName, TableIndex aIndex) {
        assert aIndex.getColumns().size() > 0 : "index definition must consist of at least 1 column";
        String indexName = escapeNameIfNeeded(aIndex.getName());
        String tableName = makeFullName(aSchemaName, aTableName);
        String modifier = "";
        /*
         * if(aIndex.isClustered()) modifier = "clustered"; else
         */
        if (aIndex.isUnique()) {
            modifier = "unique";
        }
        modifier += " nonclustered";

        String fieldsList = aIndex.getColumns().stream()
                .map(column -> new StringBuilder(escapeNameIfNeeded(column.getColumnName()))
                        .append(" asc"))
                .reduce((s1, s2) -> new StringBuilder()
                        .append(s1)
                        .append(", ")
                        .append(s2))
                .map(StringBuilder::toString)
                .orElse("");
        return "create " + modifier + " index " + indexName + " on " + tableName + "( " + fieldsList + " )";
    }

    @Override
    public String getSql4CreateSchema(String aSchemaName, String aPassword) {
        if (aSchemaName != null && !aSchemaName.isEmpty()) {
            return String.format(CREATE_SCHEMA_CLAUSE, aSchemaName);
        } else {
            throw new IllegalArgumentException("Schema name is null or empty.");
        }
    }

    @Override
    public String getSql4DropPkConstraint(String aSchemaName, PrimaryKey aPk) {
        String constraintName = escapeNameIfNeeded(aPk.getCName());
        String tableName = makeFullName(aSchemaName, aPk.getTable());
        return "alter table " + tableName + " drop constraint " + constraintName;
    }

    @Override
    public String getSql4CreateFkConstraint(String aSchemaName, List<ForeignKey> listFk) {
        if (listFk != null && listFk.size() > 0) {
            ForeignKey fk = listFk.get(0);
            String fkTableName = makeFullName(aSchemaName, fk.getTable());
            String fkName = fk.getCName();
            String fkColumnName = escapeNameIfNeeded(fk.getField());

            PrimaryKey pk = fk.getReferee();
            String pkSchemaName = pk.getSchema();
            String pkTableName = makeFullName(aSchemaName, pk.getTable());
            String pkColumnName = escapeNameIfNeeded(pk.getField());

            for (int i = 1; i < listFk.size(); i++) {
                fk = listFk.get(i);
                pk = fk.getReferee();
                fkColumnName += ", " + escapeNameIfNeeded(fk.getField());
                pkColumnName += ", " + escapeNameIfNeeded(pk.getField());
            }

            String fkRule = "";
            switch (fk.getDeleteRule()) {
                case CASCADE:
                    fkRule += " ON DELETE CASCADE ";
                    break;
                case NOACTION:
                    fkRule += " ON DELETE NO ACTION ";
                    break;
                case SETDEFAULT:
                    fkRule += " ON DELETE SET DEFAULT ";
                    break;
                case SETNULL:
                    fkRule += " ON DELETE set null ";
                    break;
            }
            switch (fk.getUpdateRule()) {
                case CASCADE:
                    fkRule += " ON UPDATE CASCADE ";
                    break;
                case NOACTION:
                    fkRule += " ON UPDATE NO ACTION ";
                    break;
                case SETDEFAULT:
                    fkRule += " ON UPDATE SET DEFAULT ";
                    break;
                case SETNULL:
                    fkRule += " ON UPDATE set null ";
                    break;
            }
            return String.format("ALTER TABLE %s ADD CONSTRAINT %s"
                    + " FOREIGN KEY (%s) REFERENCES %s (%s) %s", fkTableName, fkName.isEmpty() ? "" : escapeNameIfNeeded(fkName), fkColumnName, pkTableName, pkColumnName, fkRule);
        }
        return null;
    }

    @Override
    public String[] getSql4CreatePkConstraint(String aSchemaName, List<PrimaryKey> listPk) {

        if (listPk != null && listPk.size() > 0) {
            PrimaryKey pk = listPk.get(0);
            String tableName = pk.getTable();
            String pkTableName = makeFullName(aSchemaName, tableName);
            String pkName = escapeNameIfNeeded(generatePkName(tableName, PKEY_NAME_SUFFIX));
            String pkColumnName = escapeNameIfNeeded(pk.getField());
            for (int i = 1; i < listPk.size(); i++) {
                pk = listPk.get(i);
                pkColumnName += ", " + escapeNameIfNeeded(pk.getField());
            }
            return new String[]{
                String.format("ALTER TABLE %s ADD CONSTRAINT %s PRIMARY KEY (%s)", pkTableName, pkName, pkColumnName)
            };
        }
        return null;
    }

    @Override
    public boolean isConstraintsDeferrable() {
        return false;
    }

    @Override
    public String[] getSqls4AddingField(String aSchemaName, String aTableName, JdbcColumn aField) {
        String fullTableName = makeFullName(aSchemaName, aTableName);
        return new String[]{
            String.format(SqlDriver.ADD_FIELD_SQL_PREFIX, fullTableName) + getSql4FieldDefinition(aField)
        };
    }

    @Override
    public Character getEscape() {
        return ESCAPE;
    }

    @Override
    public NamedJdbcValue convertGeometry(String aValue, Connection aConnection) throws SQLException {
        return null;
    }

    @Override
    public String readGeometry(Wrapper aRs, int aColumnIndex, Connection aConnection) throws SQLException {
        return null;
    }
}