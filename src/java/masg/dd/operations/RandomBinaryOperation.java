package masg.dd.operations;

import java.util.Random;

public class RandomBinaryOperation implements UnaryOperation {
	Random r = new Random();
	
	@Override
	public double invoke(Double val) {
		assert val <= 1.0d;
		
		if(val > r.nextDouble()) {
			return 1.0d;
		}
		else {
			return 0.0d;
		}
	}

}
