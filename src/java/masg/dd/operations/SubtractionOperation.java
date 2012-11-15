package masg.dd.operations;

public class SubtractionOperation implements BinaryOperation {

	@Override
	public double invoke(Double val1, Double val2) {
		return val1-val2;
	}

}
