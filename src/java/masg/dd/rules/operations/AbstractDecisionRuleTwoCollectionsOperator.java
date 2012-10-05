package masg.dd.rules.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import masg.dd.rules.DecisionRule;
import masg.dd.rules.DecisionRuleCollectionIndex;

public abstract class AbstractDecisionRuleTwoCollectionsOperator implements Runnable {
	private Collection<DecisionRule> rules1;
	private Collection<DecisionRule> rules2;
	public ArrayList<DecisionRule> resultRules;
	public Exception e = null;
	
	public AbstractDecisionRuleTwoCollectionsOperator(Collection<DecisionRule> rules1, Collection<DecisionRule> rules2) {
		this.rules1 = rules1;
		this.rules2 = rules2;
		resultRules = new ArrayList<DecisionRule>();
	}
	
	@Override
	public void run() {
		
		try {
			DecisionRuleCollectionIndex r2Index = DecisionRuleCollectionIndex.index(new ArrayList<DecisionRule>(rules2));
			
			if(r2Index==null) {
				for(DecisionRule ruleThis:rules1) {
					for(DecisionRule ruleOther:rules2) {
						if(ruleThis.matches(ruleOther)) {
							DecisionRule resRule;
							try {
								resRule = executeOperation(ruleThis,ruleOther);
								if(resRule==null)
									return;
								
								resultRules.add(resRule);
							} catch (Exception e1) {
								e = e1;
								return;
							}
							
						}
					}
				}
			}
			else {
				for(DecisionRule ruleThis:rules1) {
					for(List<DecisionRule> rules2Cand: r2Index.getCandidateMatches(ruleThis)) {
						for(DecisionRule ruleOther:rules2Cand) {
							if(ruleThis.matches(ruleOther)) {
								DecisionRule resRule;
								try {
									resRule = executeOperation(ruleThis,ruleOther);
									if(resRule==null)
										return;
									
									resultRules.add(resRule);
								} catch (Exception e1) {
									e = e1;
									return;
								}
								
							}
						}
					}
				}
			}
			
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e = e2;
			return;
		}
		
	}
	
	public DecisionRule executeOperation(DecisionRule ruleThis, DecisionRule ruleOther) throws Exception {
		return null;
	}
	
}
