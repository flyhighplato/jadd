package masg.dd.representations.tables;

import java.util.ArrayList;
import java.util.HashMap;

import masg.dd.operations.BinaryOperation;
import masg.dd.representations.dag.ImmutableDDElement;
import masg.dd.variables.DDVariable;

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
