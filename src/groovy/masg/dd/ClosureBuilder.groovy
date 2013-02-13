package masg.dd

import masg.dd.variables.DDVariable

class ClosureBuilder {
	public static Closure<Double> buildClosure(HashMap< HashMap<DDVariable,Integer>, Double> ptValues, double valueIfMissing = 0.0d) {
		
		return { HashMap<DDVariable,Integer> pt ->
			
			if(ptValues.containsKey(pt)) {
				return ptValues[pt]
			}
			
			return valueIfMissing
		}
	}
}
