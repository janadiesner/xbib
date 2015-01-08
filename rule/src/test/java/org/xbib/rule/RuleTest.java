package org.xbib.rule;

import org.testng.annotations.Test;

public class RuleTest {

    @Test
    public void testRuleEngine() {
        StringBuilder sb = new StringBuilder();
        // create a singleton container for operations
        Operations operations = Operations.INSTANCE;
        // register new operations with the previously created container
        operations.registerOperation(new And());
        operations.registerOperation(new Equals());
        operations.registerOperation(new Not());
        // defines the triggers when a rule should fire
        Expression ex1 = ExpressionParser.parse("PATIENT_TYPE = 'A' AND ADMISSION_TYPE = 'O'");
        Expression ex2 = ExpressionParser.parse("PATIENT_TYPE = 'B'");
        Expression ex3 = ExpressionParser.parse("PATIENT_TYPE = 'A' AND NOT ADMISSION_TYPE = 'O'");
        // create the rules and link them to the expression and action
        Rule rule1 = new Rule.Builder()
                .with(ex1)
                .then((expr, binding) -> sb.append("patient out"))
                .build();
        Rule rule2 = new Rule.Builder()
                .with(ex2)
                .with(ex3)
                .then((expr, binding) -> sb.append("patient in"))
                .build();
        // add all rules to a single container
        RuleSet ruleSet = new RuleSet();
        ruleSet.add(rule1);
        ruleSet.add(rule2);
        // for test purpose define a variable binding ...
        Binding binding = new Binding();
        binding.put("PATIENT_TYPE", "'A'");
        binding.put("ADMISSION_TYPE", "'O'");
        // and evaluate the defined rules with the specified bindings
        boolean triggered = ruleSet.apply(null, binding);
        sb.append("Action triggered: " + triggered);
        System.err.println(sb.toString());
    }
}
