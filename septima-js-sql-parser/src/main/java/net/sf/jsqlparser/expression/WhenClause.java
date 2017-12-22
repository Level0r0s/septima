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
package net.sf.jsqlparser.expression;

/**
 * A clause of following syntax: WHEN condition THEN expression. Which is part
 * of a CaseExpression.
 *
 * @author Havard Rast Blok
 */
public class WhenClause implements Expression {

    private Expression whenExpression;
    private Expression thenExpression;
    private String commentWhen;
    private String commentThen;

    /* (non-Javadoc)
     * @see net.sf.jsqlparser.expression.Expression#accept(net.sf.jsqlparser.expression.ExpressionVisitor)
     */
    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    /**
     * @return Returns the thenExpression.
     */
    public Expression getThenExpression() {
        return thenExpression;
    }

    /**
     * @param thenExpression The thenExpression transform set.
     */
    public void setThenExpression(Expression thenExpression) {
        this.thenExpression = thenExpression;
    }

    /**
     * @return Returns the whenExpression.
     */
    public Expression getWhenExpression() {
        return whenExpression;
    }

    /**
     * @param whenExpression The whenExpression transform set.
     */
    public void setWhenExpression(Expression whenExpression) {
        this.whenExpression = whenExpression;
    }

    @Override
    public String toString() {
        return (getCommentWhen() != null ? getCommentWhen() + " " : "") + "WHEN " + whenExpression + (getCommentThen() != null ? " " + getCommentThen() : "") + " THEN " + thenExpression;
    }

    /**
     * @return the commentWhen
     */
    public String getCommentWhen() {
        return commentWhen;
    }

    /**
     * @param commentWhen the commentWhen transform set
     */
    public void setCommentWhen(String commentWhen) {
        this.commentWhen = commentWhen;
    }

    /**
     * @return the commentThen
     */
    public String getCommentThen() {
        return commentThen;
    }

    /**
     * @param commentThen the commentThen transform set
     */
    public void setCommentThen(String commentThen) {
        this.commentThen = commentThen;
    }
}
