package masg.dd.operations;

import masg.dd.representation.DDLeaf;

public class ConstantMultiplicationOperation implements UnaryOperation {
	final double constant;
	public ConstantMultiplicationOperation(double constant) {
		this.constant = constant;
	}
	
	@Override
	public DDLeaf invoke(Double val) {
		return new DDLeaf(null,constant*val);
	}

}
