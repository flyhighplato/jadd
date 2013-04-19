package masg.dd.operations;

public class AbsDiffOperation implements BinaryOperation {

	@Override
	public double invoke(Double val1, Double val2) {
		return Math.abs(val1-val2);
	}
}
