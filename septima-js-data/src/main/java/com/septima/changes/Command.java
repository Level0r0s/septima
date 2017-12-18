package com.septima.changes;

import com.septima.NamedValue;

import java.util.List;

/**
 * @author mg
 */
public class Command extends Change implements Change.Applicable {

    /**
     * Compiled Sql clause with linear parameters in form of (?, ?, ?).
     */
    private final String clause;
    /**
     * Compiled and not unique collection of parameters.
     */
    private final List<NamedValue> parameters;

    public Command(String aEntityName, String aClause, List<NamedValue> aParameters) {
        super(aEntityName);
        clause = aClause;
        parameters = aParameters;
    }

    public String getCommand() {
        return clause;
    }

    @Override
    public void accept(ApplicableChangeVisitor aChangeVisitor) {
        aChangeVisitor.visit(this);
    }

    public List<NamedValue> getParameters() {
        return parameters;
    }

}