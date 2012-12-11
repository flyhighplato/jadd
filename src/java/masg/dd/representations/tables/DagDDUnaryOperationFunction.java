package masg.dd.representations.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.operations.UnaryOperation;
import masg.dd.representations.dag.BaseDDNode;
import masg.dd.representations.dag.ImmutableDDElement;
import masg.dd.representations.dag.ImmutableDDNode;
import masg.dd.variables.DDVariable;
import masg.util.BitMap;

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
