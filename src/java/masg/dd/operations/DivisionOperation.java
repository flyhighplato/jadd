package masg.dd.operations;

public class DivisionOperation implements BinaryOperation {

	@Override
	public double invoke(Double val1, Double val2) {
		if(val1==0)
			return 0.0f;
		return val1/val2;
	}

}
