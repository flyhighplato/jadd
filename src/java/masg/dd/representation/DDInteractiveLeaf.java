package masg.dd.representation;

import java.util.HashMap;

public class DDInteractiveLeaf extends DDLeaf {

	final HashMap<DDElement, Double> interactiveFunctions;
	
	public DDInteractiveLeaf(DDInfo info, double value, HashMap<DDElement, Double> interactiveFunctions) {
		super(info, value);
		this.interactiveFunctions = interactiveFunctions;
	}
	
	public final HashMap<DDElement, Double> getFunctionDistribution() {
		return interactiveFunctions;
	}
	
	public int compareTo(DDLeaf l) {
		
			int valCompare = Double.compare(l.value, value);
		
			if ( valCompare == 0 ) {
				
				if(l instanceof DDInteractiveLeaf) {
					DDInteractiveLeaf iLeaf = (DDInteractiveLeaf) l;
					
					if(iLeaf.interactiveFunctions.equals(interactiveFunctions)) {
						return 0;
					}
					else {
						return (new Integer(iLeaf.hashCode())).compareTo( new Integer(hashCode()) );
					}
					
				}

				return -1;
			}
		
		return valCompare;
		
	}
	
	public boolean equals(Object o) {
	
		if(o instanceof DDInteractiveLeaf) {
			DDInteractiveLeaf otherLeaf = (DDInteractiveLeaf) o;
			
			if( Math.abs(otherLeaf.value - value) < Double.MIN_VALUE ) {
				return otherLeaf.interactiveFunctions.equals(interactiveFunctions);
			}
		}
		
		return false;
	}

}
