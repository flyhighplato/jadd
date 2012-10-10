package masg.dd.rules.operations;

import java.util.Collection;

import masg.dd.rules.DecisionRule;

public class DecisionRuleMaxOp extends
		AbstractDecisionRuleTwoCollectionsOperator {

	public DecisionRuleMaxOp(Collection<DecisionRule> rules1, Collection<DecisionRule> rules2) {
		super(rules1, rules2);
	}
	
	public DecisionRule executeOperation(DecisionRule ruleThis, DecisionRule ruleOther) throws Exception {
		DecisionRule resRule = DecisionRule.getIntersectionBitStringRule(ruleThis, ruleOther);
		resRule.value = Math.max(ruleThis.value, ruleOther.value);
		return resRule;
	}

}
