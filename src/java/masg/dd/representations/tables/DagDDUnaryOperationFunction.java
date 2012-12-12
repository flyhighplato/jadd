package masg.dd.representations.tables;

import java.util.HashMap;

import masg.dd.operations.UnaryOperation;
import masg.dd.representations.dag.ImmutableDDElement;
import masg.dd.variables.DDVariable;

public class DagDDUnaryOperationFunction implements DDBuilderFunction {
	ImmutableDDElement dag;
	UnaryOperation op;
	
	public DagDDUnaryOperationFunction(ImmutableDDElement dag, UnaryOperation op) {
		this.dag = dag;
		this.op = op;
	}
	
	@Override
	public Double invoke(HashMap<DDVariable, Integer> varValues) {
		return op.invoke(dag.getValue(varValues));
	}

}
