package masg.dd.representations.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.operations.BinaryOperation;
import masg.dd.representations.dag.BaseDDNode;
import masg.dd.representations.dag.ImmutableDDElement;
import masg.dd.representations.dag.ImmutableDDNode;
import masg.dd.variables.DDVariable;
import masg.util.BitMap;

public class DagDDBinaryOperationFunction implements DDBuilderFunction {

	ArrayList<ImmutableDDElement> dags = new ArrayList<ImmutableDDElement>();
	BinaryOperation op;
	public DagDDBinaryOperationFunction(ArrayList<ImmutableDDElement> dags, BinaryOperation op) {
		this.dags = dags;
		this.op = op;
	}
	
	@Override
	public Double invoke(HashMap<DDVariable, Integer> varValues) {
		
		double result = dags.get(0).getValue(varValues);
		
		for(int i=1;i<dags.size();i++) {
			result = op.invoke(result, dags.get(i).getValue(varValues));
		}
		
		return result;
	}

}
