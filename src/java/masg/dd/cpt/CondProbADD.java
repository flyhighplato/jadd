package masg.dd.cpt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import masg.dd.AlgebraicDecisionDiagram;
import masg.dd.DecisionRule;
import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;

public class CondProbADD extends AlgebraicDecisionDiagram {

	public CondProbADD(CondProbDDContext ctx) {
		super(ctx);
	}
	
	public double getValue(HashMap<DDVariable,Integer> varValues) throws Exception {
		CondProbDDContext cpContext = (CondProbDDContext) context;
		
		
		HashSet<DDVariable> inVariables = new HashSet<DDVariable>(cpContext.getInputVarSpace().getVariables());
		inVariables.retainAll(varValues.keySet());
		
		HashSet<DDVariable> outVariables = new HashSet<DDVariable>(cpContext.getOutputVarSpace().getVariables());
		
		outVariables.retainAll(varValues.keySet());
		
		if(!(outVariables.size()==0 && cpContext.getOutputVarSpace().getVariables().size()!=0) ) {
			@SuppressWarnings("unchecked")
			HashSet<DDVariable> opVariables = (HashSet<DDVariable>) inVariables.clone();
			
			opVariables.addAll(outVariables);
			
			
			AlgebraicDecisionDiagram dd = sumOutAllExcept(opVariables);
			
			HashMap<DDVariable,Integer> opVariableValues = new HashMap<DDVariable,Integer>();
			for(DDVariable var:opVariables) {
				opVariableValues.put(var, varValues.get(var));
			}
			
			DecisionRule r = dd.getContext().getVariableSpace().generateRule(opVariableValues, 1.0f);
			return dd.getValue(r);
		}
		
		
		return 1.0f;
	}
	
	public CondProbADD fix(HashMap<DDVariable,Integer> varInstances) throws Exception {
		AlgebraicDecisionDiagram summedOutDD = super.fix(varInstances);
		CondProbDDContext cpContext = (CondProbDDContext) context;
		
		@SuppressWarnings("unchecked")
		ArrayList<DDVariable> inVars = (ArrayList<DDVariable>) cpContext.getInputVarSpace().getVariables().clone();
		@SuppressWarnings("unchecked")
		ArrayList<DDVariable> outVars = (ArrayList<DDVariable>) cpContext.getOutputVarSpace().getVariables().clone();
		
		inVars.removeAll(varInstances.keySet());
		outVars.removeAll(varInstances.keySet());
		
		DDVariableSpace inVarSpace = new DDVariableSpace();
		inVarSpace.addVariables(inVars);
		
		DDVariableSpace outVarSpace = new DDVariableSpace();
		outVarSpace.addVariables(outVars);
		
		CondProbDDContext cpContextNew = new CondProbDDContext(inVarSpace,outVarSpace);
		
		CondProbADD cpDDNew = new CondProbADD(cpContextNew);
		cpDDNew.rules = summedOutDD.getRules();
		
		return cpDDNew;
	}
	
	public CondProbADD sumOut(Collection<DDVariable> sumOutVars) throws Exception {
		AlgebraicDecisionDiagram summedOutDD = super.sumOut(sumOutVars);
		CondProbDDContext cpContext = (CondProbDDContext) context;
		
		@SuppressWarnings("unchecked")
		ArrayList<DDVariable> inVars = (ArrayList<DDVariable>) cpContext.getInputVarSpace().getVariables().clone();
		@SuppressWarnings("unchecked")
		ArrayList<DDVariable> outVars = (ArrayList<DDVariable>) cpContext.getOutputVarSpace().getVariables().clone();
		
		inVars.removeAll(sumOutVars);
		outVars.removeAll(sumOutVars);
		
		DDVariableSpace inVarSpace = new DDVariableSpace();
		inVarSpace.addVariables(inVars);
		
		DDVariableSpace outVarSpace = new DDVariableSpace();
		outVarSpace.addVariables(outVars);
		
		CondProbDDContext cpContextNew = new CondProbDDContext(inVarSpace,outVarSpace);
		
		CondProbADD cpDDNew = new CondProbADD(cpContextNew);
		cpDDNew.rules = summedOutDD.getRules();
		
		return cpDDNew;
	}

}
