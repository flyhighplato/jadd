package masg.dd.rules.operations.refactored;

public class ConstantMultiplicationOperation implements UnaryOperation {
	final double constant;
	public ConstantMultiplicationOperation(double constant) {
		this.constant = constant;
	}
	
	@Override
	public double invoke(Double val) {
		return constant*val;
	}

}
