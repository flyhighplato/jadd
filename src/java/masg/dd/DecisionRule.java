package masg.dd;

//import java.util.BitSet;


import masg.util.BitMap;

public class DecisionRule implements Comparable<DecisionRule> {
	
	
	protected BitMap truthValues;
	protected BitMap setValues;
	protected String bitString = null;
	
	int numBits = 0;
	double value = 0;
	
	
	public DecisionRule(int numBits, double value) {
		this.numBits = numBits;
		truthValues = new BitMap(numBits);
		setValues = new BitMap(numBits);
		this.value = value;
	}
	
	public DecisionRule(int numBits, BitMap truthVals, BitMap setVals, double value) {
		this.numBits = numBits;
		truthValues = truthVals;
		setValues = setVals;
		this.value = value;
	}
	
	public DecisionRule(String bitAndValueString) throws Exception {
		String[] temp = bitAndValueString.split(":");
		String bitString = temp[0];
		double value = Double.parseDouble(temp[1]);
		
		truthValues = new BitMap(bitString.length());
		setValues = new BitMap(bitString.length());
		this.value = value;
		this.numBits = bitString.length();
		setBits(bitString);
		
	}
	public DecisionRule(String bitString, double value) throws Exception {
		truthValues = new BitMap(bitString.length());
		setValues = new BitMap(bitString.length());
		this.value = value;
		this.numBits = bitString.length();
		setBits(bitString);
	}
	
	public void setBits(String bitString) throws Exception {
		
		if(bitString.length() != numBits) {
			throw new Exception("String of bits isn't the right size. Should be " + truthValues.size() + " bits.");
		}
		
		for(int ix = 0; ix<numBits; ix++) {
			setBit(ix,bitString.charAt(ix));
		}
		
		bitString = null;
	}
	
	public void setBit(int ix, char c) throws Exception {
		if(c=='*') {
			setValues.clear(ix);
		}
		else {
			setValues.set(ix);
			
			if(c=='0') {
				truthValues.clear(ix);
			}
			else if(c=='1') {
				truthValues.set(ix);
			}
			else {
				throw new Exception("Unknown bit symbol (" + c + ")!");
			}
		}
		
		bitString = null;
	}
	
	public static DecisionRule mergeRules(DecisionRule rule1, DecisionRule rule2, double tolerance) throws Exception {
		
		
		if( Math.abs(rule2.value - rule1.value) < tolerance) {
			
			//Does rule1 already cover rule2?
			BitMap maskedRule1 = (BitMap) rule1.truthValues.clone();
			maskedRule1.and(rule1.setValues);
			
			BitMap maskedRule2 = (BitMap) rule2.truthValues.clone();
			maskedRule2.and(rule1.setValues);
			
			if(maskedRule1.equals(maskedRule2))
				return rule1;
			
			//Does rule2 already cover rule1?
			maskedRule1 = (BitMap) rule1.truthValues.clone();
			maskedRule1.and(rule2.setValues);
			
			maskedRule2 = (BitMap) rule2.truthValues.clone();
			maskedRule2.and(rule2.setValues);
			
			if(maskedRule1.equals(maskedRule2))
				return rule2;
			
			/*if(rule1.setValues.equals(rule2.setValues)) {
				BitMap setValues = (BitMap) rule1.setValues.clone();
				BitMap truthValues = (BitMap) rule1.truthValues.clone();
				
				truthValues.xor((BitMap) rule2.truthValues.clone());
				truthValues.not();
				setValues.and(truthValues);
				
				truthValues = (BitMap) rule1.truthValues.clone();
				truthValues.and(setValues);
				
				DecisionRule mergedRule = new DecisionRule(rule1.numBits,rule1.value);
				mergedRule.truthValues = truthValues;
				mergedRule.setValues = setValues;
				
				return mergedRule;
			}*/
			
			
			
			
			int ix1Wild = rule1.setValues.nextClearBit(0);
			int ix2Wild = rule2.setValues.nextClearBit(0);
			
			if(ix1Wild == ix2Wild) {
				int ixCompare = ix1Wild - 1;
				if(ix1Wild == -1)
					ixCompare = rule1.numBits - 1;
				
				//Different in one location before a series of wildcard values
				if(rule1.truthValues.get(ixCompare) != rule2.truthValues.get(ixCompare)) {
					//Prefix before difference location is the same
					BitMap prefix1 = rule1.truthValues.get(0, ixCompare);
					BitMap prefix2 = rule2.truthValues.get(0, ixCompare);
					if(prefix1.equals(prefix2)) {
						DecisionRule mergedRule = new DecisionRule(rule1.numBits,rule1.value);
						mergedRule.truthValues = (BitMap) rule1.truthValues.clone();
						mergedRule.setValues = (BitMap) rule1.setValues.clone();
						mergedRule.setValues.clear(ixCompare);
						return mergedRule;
					}
				}
			}
			
		}
		
		return null;
	}
	
	public static DecisionRule mergeBitMask(DecisionRule rule1, DecisionRule rule2) {
		
		int ix1Wild = rule1.setValues.nextClearBit(0);
		int ix2Wild = rule2.setValues.nextClearBit(0);
		
		if(ix1Wild == ix2Wild) {
			int ixCompare = ix1Wild - 1;
			if(ix1Wild == -1)
				ixCompare = rule1.numBits - 1;
			
			//Different in one location before a series of wildcard values
			if(rule1.truthValues.get(ixCompare) != rule2.truthValues.get(ixCompare)) {
				//Prefix before difference location is the same
				BitMap prefix1 = rule1.truthValues.get(0, ixCompare);
				BitMap prefix2 = rule2.truthValues.get(0, ixCompare);
				if(prefix1.equals(prefix2)) {
					DecisionRule mergedRule = new DecisionRule(rule1.numBits,rule1.value);
					mergedRule.truthValues = (BitMap) rule1.truthValues.clone();
					mergedRule.setValues = (BitMap) rule1.setValues.clone();
					mergedRule.setValues.clear(ixCompare);
					return mergedRule;
				}
			}
		}
		
		return null;
	}
	
	public DecisionRule getMatchingRule(DecisionRule otherRule) {
		
		BitMap maskedOther = (BitMap) otherRule.truthValues.clone();
		maskedOther.and(setValues);
		maskedOther.and(otherRule.setValues);
		
		BitMap maskedThis = (BitMap) truthValues.clone();
		maskedThis.and(setValues);	
		maskedThis.and(otherRule.setValues);
		
		if(!maskedThis.equals(maskedOther))
			return null;
		else {
			DecisionRule resultRule = new DecisionRule(otherRule.numBits,Double.NaN);
			resultRule.truthValues = (BitMap) truthValues.clone();
			resultRule.truthValues.and(setValues);
			
			BitMap temp = (BitMap) otherRule.truthValues.clone();
			temp.and(otherRule.setValues);
			
			resultRule.truthValues.or(temp);
			
			resultRule.setValues = (BitMap) setValues.clone();
			resultRule.setValues.or(otherRule.setValues);
			return resultRule;
		}
				
	}
	
	public String toString() {
		return toBitString() + ":" + value;
	}
	
	protected void updateBitString() {
		bitString = "";
		
		for(int ix=0;ix<numBits;ix++) {
			if(setValues.get(ix)) {
				if(truthValues.get(ix))
					bitString += "1";
				else
					bitString += "0";
			}
			else {
				bitString += "*";
			}
		}
	}
	
	public String toBitString() {
		if(bitString==null)
			updateBitString();
		return bitString;
	}
	
	public boolean equals(Object o) {
		
		if(o instanceof DecisionRule) {
			DecisionRule other = (DecisionRule) o;
			
			if(other.numBits == numBits && other.value == value && other.setValues.equals(setValues) && other.truthValues.equals(truthValues)) {
				
				return true;
			}
		}
		
		return false;
	}
	
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public int compareTo(DecisionRule o) {
		int compRes = 0;

		if(numBits == o.numBits) {
			BitMap ruleThis = truthValues.clone();
			ruleThis.and(setValues);
			BitMap ruleOther = o.truthValues.clone();
			ruleOther.and(o.setValues);
			
			for(int i=0;i<numBits;i++) {
				if(ruleThis.get(i) && !ruleOther.get(i)) {
					return -1;
				}
				else if(!ruleThis.get(i) && ruleOther.get(i)){
					return 1;
				}
			}
			
			for(int i=0;i<numBits;i++) {
				if(setValues.get(i) && !o.setValues.get(i)) {
					return -1;
				}
				else if(!setValues.get(i) && o.setValues.get(i)){
					return 1;
				}
			}
		}
		else {
			if(numBits > o.numBits) {
				return -1;
			}
			else {
				return 1;
			}
		}
		//for(int i=0;i<ruleThis)
		
		if(compRes == 0) {
			return Double.compare(value, o.value);
		}
		
		return compRes;
	}
	
}
