package masg.dd.operations;

import masg.dd.representation.DDLeaf;

public class ConstantAdditionOperation implements UnaryOperation {

	final double constant;
	public ConstantAdditionOperation(double constant) {
		this.constant = constant;
	}
	
	@Override
	public DDLeaf invoke(Double val1) {
		return new DDLeaf(null,val1 + constant);
	}

}
