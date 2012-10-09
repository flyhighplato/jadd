package masg.dd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import masg.dd.context.DecisionDiagramContext;
import masg.dd.rules.DecisionRule;
import masg.dd.rules.DecisionRuleCollection;
import masg.dd.rules.DecisionRuleCollectionIndex;
import masg.dd.rules.operations.AbstractDecisionRuleTwoCollectionsOperator;
import masg.dd.rules.operations.DecisionRuleAddOp;
import masg.dd.rules.operations.DecisionRuleMaxDiffOp;
import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;

public class AlgebraicDD extends AbstractDecisionDiagram {
	protected DecisionRuleCollection rules;

	protected static final ExecutorService execService = Executors.newCachedThreadPool();
	
	public AlgebraicDD(DecisionDiagramContext ctx) {
		super(ctx);
		rules = new DecisionRuleCollection(ctx.getVariableSpace().getBitCount());
	}

	public DecisionRuleCollection getRules() {
		return rules;
	}
	
	public synchronized void addRule(DecisionRule rule) throws Exception {
		rules.add(rule);

	}
	
	public synchronized void addRules(ArrayList<DecisionRule> rules) throws Exception {
		this.rules.addAll(rules);
	}

	public void compress() throws Exception {
		rules.compress();
	}

	public AlgebraicDD expandRules(Collection<DDVariable> newVars) throws Exception {
		ArrayList<DDVariable> oldVars = new ArrayList<DDVariable>(context.getVariableSpace().getVariables());
		newVars.removeAll(oldVars);
		oldVars.addAll(newVars);
		
		DecisionDiagramContext newContext = new DecisionDiagramContext(new DDVariableSpace(oldVars));
		AlgebraicDD newDD = new AlgebraicDD(newContext);
		
		ArrayList<DecisionRule> newRules = new ArrayList<DecisionRule>();
		for(DecisionRule oldRule:rules) {
			DecisionRule newRule = newContext.getVariableSpace().translateRule(oldRule, context.getVariableSpace());
			newRules.add(newRule);
		}
		
		newDD.addRules(newRules);
		return newDD;
	}
	
	public double getValue(HashMap<DDVariable,Integer> varValues) throws Exception {
		return getValue(context.getVariableSpace().generateRule(varValues, 0));
	}
	
	public double getValue(DecisionRule ruleOther) {
		for(DecisionRule ruleThis:rules) {
			if(ruleThis.matches(ruleOther)) {
				return ruleThis.value;
			}
		}
		
		return Double.NaN;
	}
	
	public double maxDiff(AlgebraicDD addOther) throws Exception {
		
		double maxDiff = 0.0f;
		
		DecisionRuleCollection rCollTrans = new DecisionRuleCollection(addOther.getContext().getVariableSpace().getBitCount());
		
		for(DecisionRule ruleOther:addOther.rules) {
			DecisionRule ruleOtherTrans = context.getVariableSpace().translateRule(ruleOther, addOther.context.getVariableSpace());
			rCollTrans.add(ruleOtherTrans);
		}
		

		int i = 0;
		ArrayList<DecisionRule> ruleSubset = new ArrayList<DecisionRule>();
		ArrayList<Future<DecisionRuleMaxDiffOp>> futures = new ArrayList<Future<DecisionRuleMaxDiffOp>>();
		
		DecisionRuleCollectionIndex rCollTransIdx = rCollTrans.getIndex();
		DecisionRuleCollectionIndex rThisIdx = getRules().getIndex();
		
		if(rCollTransIdx == null || rThisIdx == null) {
			
			for(DecisionRule ruleThis:rules) {
				i++;
				
				if(i%1000==0) {
					DecisionRuleMaxDiffOp addRunner = new DecisionRuleMaxDiffOp(ruleSubset,rCollTrans);
					Future<DecisionRuleMaxDiffOp> f = execService.submit(addRunner,addRunner);
	
					futures.add(f);
					ruleSubset = new ArrayList<DecisionRule>();
				}
				
				ruleSubset.add(ruleThis);
			}
			
			if(ruleSubset.size()>0) {
				DecisionRuleMaxDiffOp addRunner = new DecisionRuleMaxDiffOp(ruleSubset,rCollTrans);
				Future<DecisionRuleMaxDiffOp> f = execService.submit(addRunner,addRunner);
	
				futures.add(f);
				ruleSubset = null;
			}
		} else {
			for(List<List<DecisionRule>> matchTuple: rThisIdx.getCandidateMatches(rCollTransIdx)) {
				DecisionRuleMaxDiffOp addRunner = new DecisionRuleMaxDiffOp(matchTuple.get(0),matchTuple.get(1));
				Future<DecisionRuleMaxDiffOp> f = execService.submit(addRunner,addRunner);
	
				futures.add(f);
			}
		}
		
		
		while(!futures.isEmpty()) {
			if(futures.get(futures.size()-1).isDone()) {
				DecisionRuleMaxDiffOp addRunner = futures.get(futures.size()-1).get();
				if(addRunner.e!=null)
					throw addRunner.e;
				
				if(addRunner.maxDiff>maxDiff)
					maxDiff = addRunner.maxDiff;
				
				futures.remove(futures.size()-1);
			}
		}
		
		return maxDiff;
	}
	
	public boolean dominates(AlgebraicDD addOther, double tolerance) throws Exception {
		
		DecisionRuleCollection rCollTrans = new DecisionRuleCollection(addOther.getContext().getVariableSpace().getBitCount());
		
		for(DecisionRule ruleOther:addOther.rules) {
			DecisionRule ruleOtherTrans = context.getVariableSpace().translateRule(ruleOther, addOther.context.getVariableSpace());
			rCollTrans.add(ruleOtherTrans);
		}
		
		
		for(DecisionRule ruleThis:rules) {
			for(DecisionRule ruleOther:rCollTrans) {
				if(ruleThis.matches(ruleOther)) {
					double diff = ruleOther.value - ruleThis.value ;
					
					if(diff > tolerance)
						return false;
				}
			}
		}
		
		return true;
	}
	
	public AlgebraicDD plus(AlgebraicDD addOther) throws Exception {
		AlgebraicDD addNew = new AlgebraicDD(context);
		
		DecisionRuleCollection rCollTrans = new DecisionRuleCollection(addOther.getContext().getVariableSpace().getBitCount());
		
		for(DecisionRule ruleOther:addOther.rules) {
			DecisionRule ruleOtherTrans = context.getVariableSpace().translateRule(ruleOther, addOther.context.getVariableSpace());
			rCollTrans.add(ruleOtherTrans);
		}
		
		int i = 0;
		ArrayList<DecisionRule> ruleSubset = new ArrayList<DecisionRule>(1000);
		ArrayList<Future<AbstractDecisionRuleTwoCollectionsOperator>> futures = new ArrayList<Future<AbstractDecisionRuleTwoCollectionsOperator>>();
		
		DecisionRuleCollectionIndex rCollTransIdx = rCollTrans.getIndex();
		DecisionRuleCollectionIndex rThisIdx = getRules().getIndex();
		
		if(rCollTransIdx==null || rThisIdx == null) {
			for(DecisionRule ruleThis:rules) {
				i++;
				
				if(i%1000==0) {
					DecisionRuleAddOp addRunner = new DecisionRuleAddOp(ruleSubset,rCollTrans);
					Future<AbstractDecisionRuleTwoCollectionsOperator> f = execService.submit(addRunner,(AbstractDecisionRuleTwoCollectionsOperator)addRunner);
	
					futures.add(f);
					ruleSubset = new ArrayList<DecisionRule>(1000);
				}
				
				ruleSubset.add(ruleThis);
			}
			
			if(ruleSubset.size()>0) {
				DecisionRuleAddOp addRunner = new DecisionRuleAddOp(ruleSubset,rCollTrans);
				Future<AbstractDecisionRuleTwoCollectionsOperator> f = execService.submit(addRunner,(AbstractDecisionRuleTwoCollectionsOperator)addRunner);

				futures.add(f);
				ruleSubset = null;
			}
		}
		else {
			for(List<List<DecisionRule>> matchTuple: rThisIdx.getCandidateMatches(rCollTransIdx)) {
				DecisionRuleAddOp addRunner = new DecisionRuleAddOp(matchTuple.get(0),matchTuple.get(1));
				Future<AbstractDecisionRuleTwoCollectionsOperator> f = execService.submit(addRunner,(AbstractDecisionRuleTwoCollectionsOperator)addRunner);
	
				futures.add(f);
			}
		}
		
		while(!futures.isEmpty()) {
			if(futures.get(futures.size()-1).isDone()) {
				AbstractDecisionRuleTwoCollectionsOperator addRunner = futures.get(futures.size()-1).get();
				if(addRunner.e!=null)
					throw addRunner.e;
				addNew.addRules(addRunner.resultRules);
				futures.remove(futures.size()-1);
			}
		}
		
		return addNew;
	}
		
	public AlgebraicDD restrict(HashMap<DDVariable,Integer> varInstances) throws Exception {
		
		boolean willChange = false;
		
		for(DDVariable var:context.getVariableSpace().getVariables()) {
			if(varInstances.containsKey(var)) {
				willChange = true;
				break;
			}
		}
		
		AlgebraicDD addNew = new AlgebraicDD(context);
		ArrayList<DecisionRule> fixedRules = new ArrayList<DecisionRule>();
		
		if(willChange) {
			DecisionRule r = context.getVariableSpace().generateRule(varInstances, 0.0f);
			
			for(DecisionRule ruleThis:rules) {
				if(r.matches(ruleThis)) {
					fixedRules.add(ruleThis);
				}
			}
			addNew.addRules(fixedRules);
		}
		else {
			fixedRules.addAll(rules);
			addNew.rules.addAll(fixedRules);
		}

		return addNew;
	}
	
	public AlgebraicDD sumOutAllExcept(Collection<DDVariable> values) throws Exception {
		ArrayList<DDVariable> sumOutValues = new ArrayList<DDVariable>();
		
		ArrayList<DDVariable> currVariables = context.getVariableSpace().getVariables();
		for(DDVariable val:currVariables) {
			if(!values.contains(val)) {
				sumOutValues.add(val);
			}
		}
		
		return sumOut(sumOutValues);
	}
	
	public AlgebraicDD sumOut(Collection<DDVariable> sumOutVars) throws Exception {
		DDVariableSpace newVarSpace = new DDVariableSpace();
		
		DDVariableSpace ignoreVarSpace = new DDVariableSpace();
		
		ArrayList<DDVariable> currVariables = context.getVariableSpace().getVariables();
		for(DDVariable var:currVariables) {
			if(!sumOutVars.contains(var)) {
				newVarSpace.addVariable(var);
			}
			else {
				ignoreVarSpace.addVariable(var);
			}
		}
		
		if(ignoreVarSpace.getVariables().size()<=0)
			return this;
		
		DecisionDiagramContext newCtx = new DecisionDiagramContext(newVarSpace);
		ProbDD resultDD = new ProbDD(newCtx);
		
		HashMap<String,Double> newRuleNonnormValues = new HashMap<String,Double>();
		
		if(newVarSpace.getVariableCount()>0) {
			for(DecisionRule ruleThis:rules) {
				DecisionRule rNew = newCtx.getVariableSpace().translateRule(ruleThis, context.getVariableSpace());
				
				String newRuleStr = rNew.toBitString();
				if(newRuleNonnormValues.containsKey(newRuleStr)) {
					newRuleNonnormValues.put(newRuleStr, newRuleNonnormValues.get(newRuleStr) + ruleThis.value);
				}
				else {
					newRuleNonnormValues.put(newRuleStr, ruleThis.value);
				}
			}
			
		}
		else {
			for(DecisionRule ruleThis:rules) {

				DecisionRule rNew = new DecisionRule(0, ruleThis.value);
				
				String newRuleStr = rNew.toBitString();
				if(newRuleNonnormValues.containsKey(newRuleStr)) {
					newRuleNonnormValues.put(newRuleStr, newRuleNonnormValues.get(newRuleStr) + ruleThis.value);
				}
				else {
					newRuleNonnormValues.put(newRuleStr, ruleThis.value);
				}
				
			}
		}
		
		
		ArrayList<DecisionRule> newRules = new ArrayList<DecisionRule>();
		
		for(Entry<String,Double> newRuleValueEntry:newRuleNonnormValues.entrySet()) {
			DecisionRule r = new DecisionRule(newRuleValueEntry.getKey(),newRuleValueEntry.getValue());
			newRules.add(r);
		}
		
		resultDD.addRules(newRules);

		return resultDD;
	}
	
	public AlgebraicDD times(double value) throws Exception {
		AlgebraicDD addNew = new AlgebraicDD(context);
		
		for(DecisionRule ruleThis:rules) {
			DecisionRule resRule = new DecisionRule(ruleThis);
			resRule.value = ruleThis.value * value;
			addNew.addRule(resRule);
		}
		
		return addNew;
	}
	
	public AlgebraicDD times(AlgebraicDD addOther) throws Exception {
		AlgebraicDD addNew = new AlgebraicDD(context);
		
		ArrayList<DecisionRule> rRules = new ArrayList<DecisionRule>();
		for(DecisionRule ruleThis:rules) {
			for(DecisionRule ruleOther:addOther.rules) {
				ruleOther = context.getVariableSpace().translateRule(ruleOther, addOther.context.getVariableSpace());
				if(ruleThis.matches(ruleOther)) {
					DecisionRule resRule = DecisionRule.getIntersectionBitStringRule(ruleThis, ruleOther);
					resRule.value = ruleThis.value * ruleOther.value;
					rRules.add(resRule);
				}
			}
		}
		
		addNew.addRules(rRules);
		return addNew;
	}
	
	public void normalize() {
		double totalWeight = 0.0f;
		
		for(DecisionRule ruleThis:rules) {
			totalWeight  += ruleThis.value;
		}
		
		if(totalWeight>0.0f) {
			for(DecisionRule ruleThis:rules) {
				ruleThis.value = ruleThis.value/totalWeight;
			}
		}
	}
	
	public void prime() throws Exception {
		context = new DecisionDiagramContext(new DDVariableSpace(context.getVariableSpace().getVariables()));
		context.getVariableSpace().prime();
	}
	
	public void unprime() throws Exception {
		context = new DecisionDiagramContext(new DDVariableSpace(context.getVariableSpace().getVariables()));
		context.getVariableSpace().unprime();
	}
	
	public String toString() {
		String str = "";
		str += context.getVariableSpace().getVariables() + "\n";
		str += "(" + rules.size() + ")\n";
		
		int i = 0;
		for(DecisionRule rule:rules) {
			i++;
			str += rule + "\n";
			if(i>1000)
				break;
		}
		return str;
	}
}
