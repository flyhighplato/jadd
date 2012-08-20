package masg.dd.cpt;

import java.util.HashMap;
import java.util.HashSet;

import masg.dd.AlgebraicDecisionDiagram;
import masg.dd.DecisionRule;
import masg.dd.vars.DDVariable;

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
		
		System.out.println();
		
		if(outVariables.size()!=0) {
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
		
		
		System.out.println("Default value");
		return 1.0f;
	}

}
