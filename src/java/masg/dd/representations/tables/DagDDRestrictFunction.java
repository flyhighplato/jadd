package masg.dd.representations.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.representations.dag.BaseDDNode;
import masg.dd.representations.dag.ImmutableDDElement;
import masg.dd.representations.dag.ImmutableDDNode;
import masg.dd.variables.DDVariable;
import masg.util.BitMap;

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
		
		BitMap prefix = null;
		for(Entry<DDVariable,Integer> e:varValuesAll.entrySet()) {
			prefix = BaseDDNode.joinKeys(prefix, BaseDDNode.variableValuetoBitMap(e.getKey(), e.getValue()));
		}
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(varValuesAll.keySet());
		
		return dag.getValue(vars, prefix);
	}

	

}
