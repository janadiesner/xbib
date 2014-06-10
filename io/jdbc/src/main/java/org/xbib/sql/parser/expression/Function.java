package org.xbib.sql.parser.expression;

import org.xbib.sql.parser.expression.operators.relational.ExpressionList;

public class Function implements Expression {

	private String name;
	private ExpressionList parameters;
	private boolean allColumns = false;
	private boolean distinct = false;
	private boolean isEscaped = false;
	
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String string) {
		name = string;
	}
	public boolean isAllColumns() {
		return allColumns;
	}

	public void setAllColumns(boolean b) {
		allColumns = b;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean b) {
		distinct = b;
	}
	public ExpressionList getParameters() {
		return parameters;
	}

	public void setParameters(ExpressionList list) {
		parameters = list;
	}

    public boolean isEscaped() {
        return isEscaped;
    }
    
    public void setEscaped(boolean isEscaped) {
        this.isEscaped = isEscaped;
    }

    public String toString() {
    	String params = "";
    	
    	if(allColumns) {
    		params = "(*)";
    	}
    	else if(parameters != null) {
			params = parameters.toString();
    		if (isDistinct()) {
    			params = params.replaceFirst("\\(", "(DISTINCT ");
    		} 
    	}
    	
    	String ans = name+""+params+"";
    	
    	if(isEscaped) {
    		ans = "{fn "+ans+"}"; 
    	}
    	
    	return ans;
    }
}
