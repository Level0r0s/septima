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
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Relation;

/**
 * A "BETWEEN" expr1 expr2 statement
 */
public class Between implements Relation {

    private Expression leftExpression;
    private boolean not = false;
    private Expression betweenExpressionStart;
    private Expression betweenExpressionEnd;
    private String commentBetween;
    private String commentNot;
    private String commentAnd;

    public Expression getBetweenExpressionEnd() {
        return betweenExpressionEnd;
    }

    public Expression getBetweenExpressionStart() {
        return betweenExpressionStart;
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean b) {
        not = b;
    }

    public void setBetweenExpressionEnd(Expression expression) {
        betweenExpressionEnd = expression;
    }

    public void setBetweenExpressionStart(Expression expression) {
        betweenExpressionStart = expression;
    }

    public void setLeftExpression(Expression expression) {
        leftExpression = expression;
    }

    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        return leftExpression + " " + (not ? (getCommentNot() != null ? getCommentNot()+" " : "") + "NOT " : "") 
               + (getCommentBetween() != null ? getCommentBetween()+" " : "") + "BETWEEN "
               + betweenExpressionStart + (getCommentAnd() != null ? " "+getCommentAnd() : "") + " AND " + betweenExpressionEnd;
    }

    /**
     * @return the commentBetween
     */
    public String getCommentBetween() {
        return commentBetween;
    }

    /**
     * @param commentBetween the commentBetween to set
     */
    public void setCommentBetween(String commentBetween) {
        this.commentBetween = commentBetween;
    }

    /**
     * @return the commentNot
     */
    public String getCommentNot() {
        return commentNot;
    }

    /**
     * @param commentNot the commentNot to set
     */
    public void setCommentNot(String commentNot) {
        this.commentNot = commentNot;
    }

    /**
     * @return the commentAnd
     */
    public String getCommentAnd() {
        return commentAnd;
    }

    /**
     * @param commentAnd the commentAnd to set
     */
    public void setCommentAnd(String commentAnd) {
        this.commentAnd = commentAnd;
    }
   
}
