/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.jsqlparser.test;

import net.sf.jsqlparser.JSqlParserException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.syntax.FromItems;
import org.junit.Test;

import java.io.StringReader;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author mg
 */
public class OperateTest extends GeneralTest{

    @Test
    public void absTest() throws JSqlParserException {
        String statement = "select f1, abs(F3, F4, 1) as f2 from t1";
        checkParseAndDeparse(statement);
    }

    @Test
    public void maxTest() throws JSqlParserException {
        String statement = "select f1, max(f2) as mf2 from t1";
        checkParseAndDeparse(statement);
    }

    @Test
    public void nvlTest() throws JSqlParserException {
        String statement = "select f1, nvl(f2, f3) as nvlf2 from t1";
        checkParseAndDeparse(statement);
    }

    @Test
    public void betweenTest() throws JSqlParserException {
        String statement = "select f1, f2 from t1 where t1.f3 BETWEEN 2 AND 8";
        checkParseAndDeparse(statement);
    }

    @Test
    public void subqueryTest() throws JSqlParserException {
        String statement = "SELECT T0.ORDER_NO, 'Some text' AS VALUE_FIELD_1, TABLE1.ID, TABLE1.F1, TABLE1.F3, T0.AMOUNT FROM TABLE1, TABLE2, (SELECT GOODORDER.ORDER_ID AS ORDER_NO, GOODORDER.AMOUNT, CUSTOMER.CUSTOMER_NAME AS CUSTOMER FROM GOODORDER INNER JOIN CUSTOMER ON (GOODORDER.CUSTOMER = CUSTOMER.CUSTOMER_ID) AND (GOODORDER.AMOUNT > CUSTOMER.CUSTOMER_NAME) WHERE :P4 = GOODORDER.GOOD) aS T0 WHERE ((TABLE2.FIELDA < TABLE1.F1) AND (:P2 = TABLE1.F3)) AND (:P3 = T0.AMOUNT)";
        checkParseAndDeparse(statement);
    }

    @Test
    public void asteriskTest() throws JSqlParserException {
        String statement = "SELECT * FROM TABLE1, TABLE2 where TABLE1.f1 is null";
        checkParseAndDeparse(statement);
    }

    @Test
    public void keywordsSubstitutingByTableAliasTest() throws JSqlParserException {
        String statementText = "SELECT * FROM TABLE1 where TABLE1.f1 is null";
        checkParseAndDeparse(statementText);
        Statement statement = parserManager.parse(new StringReader(statementText));
        Map<String, FromItem> fromItems = FromItems.find(FromItems.ToCase.UPPER, ((Select)statement).getSelectBody());
        assertEquals(1, fromItems.size());
        assertEquals("TABLE1", fromItems.keySet().iterator().next());
    }

    @Test
    public void keywordsSubstitutingByColumnAliasTest() throws JSqlParserException {
        // Where may be treated as alias.
        // Testing partial LL(2) grammar capability 
        String statement = "SELECT TABLE1.f1 FROM TABLE1 where TABLE1.f1 is null";
        checkParseAndDeparse(statement);
    }

    @Test
    public void tableAliasTest() throws JSqlParserException {
        String statement = "SELECT t1.f1 FROM TABLE1 as t1 where t1.f1 is null";
        checkParseAndDeparse(statement);
    }
    /*
    @Test
    public void singleLineCommentTest() throws JSqlParserException {
        String statement = "SELECT * FROM TABLE1, TABLE2\n --This is single line comment";
        checkParseAndDeparse(statement);
    }

    @Test
    public void multiLineCommentTest() throws JSqlParserException {
        String statement = "SELECT * FROM TABLE1, TABLE2\n"
                +"/*\n"
                + "This is multiline comment.\n"
                + "This is the second lime of multiline comment.\n"
                + "*/   /*";
        checkParseAndDeparse(statement);
    }
*/
    @Test
    public void deleteTest() throws JSqlParserException {
        String statement = "delete from MTD_ENTITIES  where MDENT_ID = :id";
        checkParseAndDeparse(statement);
    }

    @Test
    public void insert3ParamsTest() throws JSqlParserException {
        String statement = "insert into MTD_ENTITIES(MDENT_ID, MDENT_TYPE, MDENT_NAME) values (:id, :type, :name)";
        checkParseAndDeparse(statement);
    }

    @Test
    public void insert4ParamsTest() throws JSqlParserException {
        String statement = "insert into MTD_ENTITIES(MDENT_ID, MDENT_TYPE, MDENT_NAME, MDENT_CONTENT_TXT) values (:id, :type, :name, :content)";
        checkParseAndDeparse(statement);
    }
}
