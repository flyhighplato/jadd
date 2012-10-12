package masg.dd.rules;

import java.util.List;

public class JoinResult {
	final protected List<DecisionRule> rulesLeft;
	final protected List<DecisionRule> rulesRight;
	
	public JoinResult(List<DecisionRule> rulesLeft, List<DecisionRule> rulesRight) {
		this.rulesLeft = rulesLeft;
		this.rulesRight = rulesRight;
	}
	
	public List<DecisionRule> getLeftRulesList() {
		return rulesLeft;
	}
	
	public List<DecisionRule> getRightRulesList() {
		return rulesRight;
	}
}
