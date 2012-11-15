package masg.dd.operations;

public class ConstantAdditionOperation implements UnaryOperation {

	final double constant;
	public ConstantAdditionOperation(double constant) {
		this.constant = constant;
	}
	
	@Override
	public double invoke(Double val1) {
		return val1 + constant;
	}

}
