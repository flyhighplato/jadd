package masg.dd.representations.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.representations.dag.BaseDDNode;
import masg.dd.representations.dag.ImmutableDDElement;
import masg.dd.variables.DDVariable;
import masg.util.BitMap;

public class DagDDTranslateFunction implements DDBuilderFunction {
	HashMap<DDVariable, DDVariable> varMap;
	ImmutableDDElement dag;
	
	public DagDDTranslateFunction(ImmutableDDElement dag, HashMap<DDVariable, DDVariable> varMap) {
		this.varMap = varMap;
		this.dag = dag;
	}
	
	@Override
	public Double invoke(HashMap<DDVariable, Integer> varValues) {
		HashMap<DDVariable, Integer> varValuesNew = new HashMap<DDVariable, Integer>();
		
		for(Entry<DDVariable, Integer> e: varValues.entrySet()) {
			varValuesNew.put(varMap.get(e.getKey()), e.getValue());
		}
		
		BitMap prefix = null;
		for(Entry<DDVariable,Integer> e:varValuesNew.entrySet()) {
			prefix = BaseDDNode.joinKeys(prefix, BaseDDNode.variableValuetoBitMap(e.getKey(), e.getValue()));
		}
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(varValuesNew.keySet());
		
		
		return dag.getValue(vars, prefix);
	}

}
