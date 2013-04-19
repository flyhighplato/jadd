package masg.dd.operations;

public class MinOperation implements BinaryOperation {

	@Override
	public double invoke(Double val1, Double val2) {
		return Math.min(val1, val2);
	}

}
