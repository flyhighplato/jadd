package masg.dd.representations.tables;

import java.util.HashMap;

import masg.dd.representations.dag.ImmutableDDElement;
import masg.dd.variables.DDVariable;

public class DagDDRestrictFunction implements DDBuilderFunction {

	HashMap<DDVariable,Integer> restrictVarValues;
	ImmutableDDElement dag;
	
	public DagDDRestrictFunction(ImmutableDDElement dag, HashMap<DDVariable,Integer> restrictVarValues) {
		this.restrictVarValues = restrictVarValues;
		this.dag = dag;
	}
	
	@Override
	public Double invoke(HashMap<DDVariable, Integer> varValues) {
		
		HashMap<DDVariable,Integer> varValuesAll = new HashMap<DDVariable,Integer>();
		varValuesAll.putAll(varValues);
		varValuesAll.putAll(restrictVarValues);
		
		return dag.getValue(varValuesAll);
	}

	

}
