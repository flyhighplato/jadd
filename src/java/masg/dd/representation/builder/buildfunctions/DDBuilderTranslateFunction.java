package masg.dd.representation.builder.buildfunctions;

import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.representation.DDElement;
import masg.dd.representation.builder.DDBuilderFunction;
import masg.dd.variables.DDVariable;

public class DDBuilderTranslateFunction implements DDBuilderFunction {
	HashMap<DDVariable, DDVariable> varMap;
	DDElement dag;
	
	public DDBuilderTranslateFunction(DDElement dag, HashMap<DDVariable, DDVariable> varMap) {
		this.varMap = varMap;
		this.dag = dag;
	}
	
	@Override
	public Double invoke(HashMap<DDVariable, Integer> varValues) {
		HashMap<DDVariable, Integer> varValuesNew = new HashMap<DDVariable, Integer>();
		
		for(Entry<DDVariable, Integer> e: varValues.entrySet()) {
			varValuesNew.put(varMap.get(e.getKey()), e.getValue());
		}
		
		return dag.getValue(varValuesNew);
	}

}
