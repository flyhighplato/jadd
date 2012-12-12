package masg.dd.representations.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import masg.dd.representations.dag.ImmutableDDElement;
import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;

public class DagDDElimFunction implements DDBuilderFunction {

	ArrayList<DDVariable> elimVars;
	ImmutableDDElement dag;
	DDVariableSpace elimVarSpace;
	
	public DagDDElimFunction(ImmutableDDElement dag, ArrayList<DDVariable> elimVars) {
		
		this.elimVars = new ArrayList<DDVariable>(elimVars);
		this.elimVars.retainAll(dag.getVariables());
		
		this.dag = dag;
		elimVarSpace = new DDVariableSpace(this.elimVars);
	}
	
	@Override
	public Double invoke(HashMap<DDVariable, Integer> varValues) {
		
		HashMap<DDVariable,Integer> varValuesAll = new HashMap<DDVariable,Integer>();
		varValuesAll.putAll(varValues);
		
		HashSet<DDVariable> uniqVars = new HashSet<DDVariable>(varValues.keySet());
		uniqVars.addAll(elimVars);
		
		double result = 0.0f;
		
		for(HashMap<DDVariable,Integer> elimVarSpacePt:elimVarSpace) {
			varValuesAll.putAll(elimVarSpacePt);
			
			result+=dag.getValue(varValuesAll);
		}
		
		return result;
	}

}
