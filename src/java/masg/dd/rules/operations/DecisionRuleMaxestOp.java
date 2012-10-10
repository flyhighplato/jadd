package masg.dd.rules.operations;

import java.util.Collection;

import masg.dd.rules.DecisionRule;

public class DecisionRuleMaxestOp extends
		AbstractDecisionRuleSingleCollectionOperator {

	public DecisionRuleMaxestOp(Collection<DecisionRule> rules) {
		super(rules);
	}

	public double executeOperation(DecisionRule ruleThis, double oldResult) throws Exception {
		return ruleThis.value>oldResult?ruleThis.value:oldResult;
	}
}
