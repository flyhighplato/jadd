package masg.dd;

import masg.util.BitMap;

public class DecisionRule implements Comparable<DecisionRule> {
	
	
	protected BitMap truthValues;
	protected BitMap setValues;
	protected String bitString = null;
	
	int numBits = 0;
	public double value = 0;
	
	
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
	
	public DecisionRule(DecisionRule r) {
		this.numBits = r.numBits;
		truthValues = r.truthValues.clone();
		setValues = r.setValues.clone();
		this.value = r.value;
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
	
	public char getBit(int ix) {
		if(setValues.get(ix)) {
			if(truthValues.get(ix))
				return '1';
			else
				return '0';
		}
		else {
			return '*';
		}
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
	
	public boolean matches(DecisionRule otherRule) {
		if(otherRule.numBits!=otherRule.numBits)
			return false;
		
		for(int ix = 0; ix < numBits; ix++) {
			if(otherRule.setValues.get(ix) && setValues.get(ix)) {
				if(otherRule.truthValues.get(ix)!=truthValues.get(ix))
					return false;
			}
		}
		
		return true;
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
	
	public static DecisionRule getSubsetRule(DecisionRule r1, DecisionRule r2) {
		DecisionRule r = getSupersetRule(r1,r2);
		if(r!=null) {
			if(r.equals(r1))
				return new DecisionRule(r2);
			else if (r.equals(r2))
				return new DecisionRule(r1);
		}
		
		return null;
	}
	
	public static DecisionRule getSupersetRule(DecisionRule r1, DecisionRule r2) {
		if((r1.value != r2.value))
			return null;
		
		
		return getSupersetBitStringRule(r1,r2);
	}
	
	public static DecisionRule getSubsetBitStringRule(DecisionRule r1, DecisionRule r2) {
		DecisionRule r = getSupersetBitStringRule(r1,r2);
		
		if(r!=null) {
			if(r.equals(r1))
				return new DecisionRule(r2);
			else if (r.equals(r2))
				return new DecisionRule(r1);
		}
		
		return null;
	}
	
	public static DecisionRule getSupersetBitStringRule(DecisionRule r1, DecisionRule r2) {
		int size = r1.numBits;
		if(r1.numBits!=r2.numBits)
			return null;
		
		boolean covers = true;
		for(int currBitIx=0;currBitIx<size;currBitIx++) {
			if( (r1.setValues.get(currBitIx) && !r2.setValues.get(currBitIx)) || 
				(r1.setValues.get(currBitIx) && r1.truthValues.get(currBitIx) != r2.truthValues.get(currBitIx))) {
				covers = false;
				break;
			}
		}
		
		if(covers)
			return r1;
		
		covers = true;
		for(int currBitIx=0;currBitIx<size;currBitIx++) {
			if( (r2.setValues.get(currBitIx) && !r1.setValues.get(currBitIx)) || 
				(r2.setValues.get(currBitIx) && r2.truthValues.get(currBitIx) != r1.truthValues.get(currBitIx))) {
				covers = false;
				break;
			}
		}
		
		if(covers)
			return new DecisionRule(r2);
		
		return null;
	}
	
	public static DecisionRule getIntersectionBitStringRule(DecisionRule r1, DecisionRule r2) throws Exception {
		int size = r1.numBits;
		if(r1.numBits!=r2.numBits)
			return null;
		
		DecisionRule rRes = getSubsetBitStringRule(r1,r2);
		if(rRes!=null)
			return rRes;
		else {
			rRes = new DecisionRule(size, Double.NaN);
			for(int currBitIx=0;currBitIx<size;currBitIx++) {
				
				if(!r1.setValues.get(currBitIx) && !r2.setValues.get(currBitIx)) {
					rRes.setBit(currBitIx, '*');
				}
				else if(r1.setValues.get(currBitIx) && r2.setValues.get(currBitIx)){
					if(r1.truthValues.get(currBitIx)!=r2.truthValues.get(currBitIx)) {
						return null;
					}
					else {
						if(r1.truthValues.get(currBitIx))
							rRes.setBit(currBitIx, '1');
						else
							rRes.setBit(currBitIx, '0');
					}
				}
				else if(r1.setValues.get(currBitIx) != r2.setValues.get(currBitIx)) {
					boolean setVal = r1.setValues.get(currBitIx)?r1.truthValues.get(currBitIx):r2.truthValues.get(currBitIx);
					
					if(setVal)
						rRes.setBit(currBitIx, '1');
					else
						rRes.setBit(currBitIx, '0');
				}
			}
		}
		
		return rRes;
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
		
		if(compRes == 0) {
			return Double.compare(value, o.value);
		}
		
		return compRes;
	}
	
}
