/* ================================================================
 * JSQLParser : java based sql parser 
 * ================================================================
 *
 * Project Info:  http://jsqlparser.sourceforge.net
 * Project Lead:  Leonardo Francalanci (leoonardoo@yahoo.it);
 *
 * (C) Copyright 2004, by Leonardo Francalanci
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package net.sf.jsqlparser.statement.select;

import java.util.List;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Select implements Statement {

    private SelectBody selectBody;
    private List<WithItem> withItemsList;
    private String commentWith;
    private List<String> commentsComma;
    private String endComment = new String();

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public SelectBody getSelectBody() {
        return selectBody;
    }

    public void setSelectBody(SelectBody body) {
        selectBody = body;
    }

    @Override
    public String toString() {
        StringBuilder retval = new StringBuilder();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            retval.append(getCommentWith() != null ? getCommentWith() + " " : "");
            retval.append("WITH ");
            String tmp = PlainSelect.getStringListWithCommaComment(withItemsList, commentsComma, true, false, null);
            retval.append(tmp).append(" ");
        }
        retval.append(selectBody);
        retval.append(!"".equals(getEndComment()) ? " " + getEndComment() : "");
        return retval.toString();
    }

    public List<WithItem> getWithItemsList() {
        return withItemsList;
    }

    public void setWithItemsList(List<WithItem> aValue) {
        withItemsList = aValue;
    }

    @Override
    public void setEndComment(String endComment) {
        this.endComment += endComment;
    }

    @Override
    public String getEndComment() {
        return endComment;
    }

    /**
     * @return the commentWith
     */
    public String getCommentWith() {
        return commentWith;
    }

    /**
     * @param commentWith the commentWith to set
     */
    public void setCommentWith(String commentWith) {
        this.commentWith = commentWith;
    }

    /**
     * @return the commetsComma
     */
    public List<String> getCommentsComma() {
        return commentsComma;
    }

    /**
     * @param aValue the commetsComma to set
     */
    public void setCommentsComma(List<String> aValue) {
        commentsComma = aValue;
    }
}
