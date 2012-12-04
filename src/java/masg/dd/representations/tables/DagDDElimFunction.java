package masg.dd.representations.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import masg.dd.representations.dag.BaseDDNode;
import masg.dd.representations.dag.ImmutableDDElement;
import masg.dd.representations.dag.ImmutableDDNode;
import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;
import masg.util.BitMap;

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
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(uniqVars);
		double result = 0.0f;
		
		for(HashMap<DDVariable,Integer> elimVarSpacePt:elimVarSpace) {
			varValuesAll.putAll(elimVarSpacePt);
			
			BitMap prefix = null;
			for(Entry<DDVariable,Integer> e:varValuesAll.entrySet()) {
				prefix = BaseDDNode.joinKeys(prefix, BaseDDNode.variableValuetoBitMap(e.getKey(), e.getValue()));
			}
			
			result+=dag.getValue(vars, prefix);
		}
		
		return result;
	}

}
