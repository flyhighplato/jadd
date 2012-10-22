package masg.dd.rules.operations;

public class MultiplicationOperation implements BinaryOperation{
	public double invoke(Double val1, Double val2) {
		return val1*val2;
	}
}
