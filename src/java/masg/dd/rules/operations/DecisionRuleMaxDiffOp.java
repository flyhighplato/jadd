package masg.dd.rules.operations;

import java.util.Collection;

import masg.dd.rules.DecisionRule;

public class DecisionRuleMaxDiffOp extends
		AbstractDecisionRuleTwoCollectionsOperator {

	public double maxDiff = 0.0f;
	public DecisionRuleMaxDiffOp(Collection<DecisionRule> rules1,Collection<DecisionRule> rules2) {
		super(rules1, rules2);
	}

	public DecisionRule executeOperation(DecisionRule ruleThis, DecisionRule ruleOther) throws Exception {
		double diff = Math.abs(ruleThis.value - ruleOther.value);
		if(maxDiff<diff)
			maxDiff = diff;
		return ruleThis;
	}
}
