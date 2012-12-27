package masg.dd.representation.builder.buildfunctions;

import java.util.HashMap;

import masg.dd.representation.DDElement;
import masg.dd.variables.DDVariable;

public class DDBuilderRestrictFunction implements DDBuilderFunction {

	HashMap<DDVariable,Integer> restrictVarValues;
	DDElement dag;
	
	public DDBuilderRestrictFunction(DDElement dag, HashMap<DDVariable,Integer> restrictVarValues) {
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
