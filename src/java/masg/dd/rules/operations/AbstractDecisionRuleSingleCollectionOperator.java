package masg.dd.rules.operations;

import java.util.Collection;

import masg.dd.rules.DecisionRule;

public abstract class AbstractDecisionRuleSingleCollectionOperator implements Runnable {
	private Collection<DecisionRule> rules;
	public double result;
	public Exception e = null;
	
	public AbstractDecisionRuleSingleCollectionOperator(Collection<DecisionRule> rules) {
		this.rules = rules;
		result = Double.NaN;
	}
	
	@Override
	public void run() {
		for(DecisionRule ruleThis:rules) {
			try {
				result = executeOperation(ruleThis,result);
			} catch (Exception e1) {
				e = e1;
				return;
			}
			

		}
	}
	
	public double executeOperation(DecisionRule ruleThis, double oldResult) throws Exception {
		return Double.NaN;
	}
	
	
}
