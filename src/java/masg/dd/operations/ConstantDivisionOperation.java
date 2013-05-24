package masg.dd.operations;

public class ConstantDivisionOperation implements UnaryOperation {
	final double constant;
	public ConstantDivisionOperation(double constant) {
		this.constant = constant;
	}
	
	@Override
	public double invoke(Double val) {
		return val/constant;
	}

}
