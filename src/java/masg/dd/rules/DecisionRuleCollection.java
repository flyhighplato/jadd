package masg.dd.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

import masg.util.BitMap;

public class DecisionRuleCollection implements Collection<DecisionRule> {
	
	ConcurrentSkipListSet<DecisionRule> rules = new ConcurrentSkipListSet<DecisionRule>();
	
	protected int size = 0;
	public DecisionRuleCollection(int size) {
		this.size = size;
	}
	
	public void compress() {
		
		int oldRuleSize = Integer.MAX_VALUE;
		
		while(oldRuleSize>rules.size()) {
			oldRuleSize = rules.size();
			
			for(int prefixLength = size-1;prefixLength > 0;prefixLength--) {
				DecisionRule currPrefixRule = null;
				
				ArrayList<DecisionRule> suffixRules = new ArrayList<DecisionRule>();	
				ArrayList<DecisionRule> rulesNew = new ArrayList<DecisionRule>();
				
				for(DecisionRule currRule:rules) {
					
					
					BitMap prefixTruthValues = currRule.truthValues.get(0, prefixLength);
					BitMap prefixSetValues = currRule.setValues.get(0, prefixLength);
					
					DecisionRule prefixRule = new DecisionRule(prefixLength, prefixTruthValues, prefixSetValues, currRule.value);
					
					DecisionRule oldPrefixRule = currPrefixRule;
					
					if(currPrefixRule != null)
						currPrefixRule = DecisionRule.getSupersetRule(currPrefixRule,prefixRule);
					if(currPrefixRule == null) {
						
						currPrefixRule = oldPrefixRule;
						
						Collection<DecisionRule> mergedRules = merge(currPrefixRule,suffixRules);
						
						rulesNew.addAll(mergedRules);
						
						suffixRules = new ArrayList<DecisionRule>();
						currPrefixRule = prefixRule;
					}
					
					BitMap suffixTruthValues = currRule.truthValues.get(prefixLength, size - prefixLength);
					BitMap suffixSetValues = currRule.setValues.get(prefixLength, size - prefixLength);
					
					DecisionRule suffixRule = new DecisionRule(size - prefixLength, suffixTruthValues, suffixSetValues, currRule.value);
					suffixRules.add(suffixRule);
				}
				
				rulesNew.addAll(merge(currPrefixRule,suffixRules));
				
				rules.clear();
				rules.addAll(rulesNew);
			}
			
			
			Collection<DecisionRule> compressedRules = compressOnFirstCol(rules);
			rules.clear();
			rules.addAll(compressedRules);
		}
	}
	
	protected Collection<DecisionRule> merge(DecisionRule prefixRule, Collection<DecisionRule> suffixRules ) {
		
		ArrayList<DecisionRule> mergedRules = new ArrayList<DecisionRule>();
				
		if(prefixRule==null)
			return mergedRules;
		
		suffixRules = new ArrayList<DecisionRule>(compressOnFirstCol(suffixRules));
		
		for(DecisionRule suffixRule:suffixRules) {
			BitMap newTruthValues = new BitMap(size);
			BitMap newSetValues = new BitMap(size);
			
			for(int ix=0;ix<size;ix++) {
				if(ix<prefixRule.numBits) {
					if(prefixRule.truthValues.get(ix)) {
						newTruthValues.set(ix);
					}
					
					if(prefixRule.setValues.get(ix)) {
						newSetValues.set(ix);
					}
				}
				else {
					int ixShifted = ix - prefixRule.numBits;
					
					if(suffixRule.truthValues.get(ixShifted)) {
						newTruthValues.set(ix);
					}
					
					if(suffixRule.setValues.get(ixShifted)) {
						newSetValues.set(ix);
					}
				}
			}
			
			double ruleValue = prefixRule.value;
			
			DecisionRule newRule = new DecisionRule(size,newTruthValues,newSetValues,ruleValue);
			mergedRules.add(newRule);
		}
		
		return mergedRules;
	}
	protected Collection<DecisionRule> compressOnFirstCol(Collection<DecisionRule> rules) {
		if(rules.size()<=0)
			return rules;
		
		ArrayList<DecisionRule> rulesCopy = new ArrayList<DecisionRule>(rules);
		ArrayList<DecisionRule> returnRules = new ArrayList<DecisionRule>();
		
	
		DecisionRule currRule = rulesCopy.remove(0);

		for(DecisionRule otherRule:rulesCopy) {
			boolean wildCardTail = true;
			for(int currBitIx=1;currBitIx<currRule.numBits;currBitIx++) {
				if(currRule.setValues.get(currBitIx)) {
					wildCardTail = false;
					break;
				}
			}
			
			//System.out.println("isWildCardValue:" + isWildCardValue);
			if(((currRule.value - otherRule.value)<0.001f) && wildCardTail) {
				
				double ruleVal = currRule.value;
				
				
				if(currRule.equals(otherRule)) {
					//System.out.println(currRule + " same as " + otherRule);
					continue;
				}
				
				DecisionRule supersetRule = DecisionRule.getSupersetRule(currRule,otherRule);
				if(supersetRule!=null) {
					//System.out.println(supersetRule + " covers " + (otherRule==supersetRule?currRule:otherRule));
					currRule = supersetRule;
					continue;
				}
				
				boolean suffixMatch = true;
				
				for(int currBitIx=1;currBitIx<currRule.numBits;currBitIx++) {
					if( (otherRule.setValues.get(currBitIx) != currRule.setValues.get(currBitIx)) || (otherRule.truthValues.get(currBitIx) != currRule.truthValues.get(currBitIx))) { 
						suffixMatch = false;
						break;
					}
				}
				
				boolean bitColMismatch = (currRule.setValues.get(0) && otherRule.setValues.get(0)) && (currRule.truthValues.get(0) != otherRule.truthValues.get(0));
				
				//Are they different by one bit in the current column?
				if(suffixMatch && bitColMismatch) {
					//System.out.println(currRule + " prefix matches " + otherRule);
					BitMap newTruthValues = currRule.truthValues.clone();
					newTruthValues.clear(0);
					
					BitMap newSetValues = currRule.setValues.clone();
					newSetValues.clear(0);
					
					currRule = new DecisionRule(currRule.numBits,newTruthValues,newSetValues,ruleVal);
				}
				else {
					returnRules.add(currRule);
					currRule = otherRule;
				}
			}
			else {
				returnRules.add(currRule);
				currRule = otherRule;
				continue;
			}
		}

		returnRules.add(currRule);
		//System.out.println("Compressed:" + returnRules);
		//System.out.println();
		return returnRules;
	}
	
	public double getRuleValueSum() {
		double sum = 0.0f;
		for(DecisionRule r: rules) {
			sum += r.value;
		}
		return sum;
	}
	
	@Override
	public boolean add(DecisionRule rule) {
		if(rule.numBits != size) {
			return false;
		}
		return rules.add(rule);
	}

	@Override
	public boolean addAll(Collection<? extends DecisionRule> otherRules) {
		return rules.addAll(otherRules);
	}

	@Override
	public void clear() {
		rules.clear();
	}

	@Override
	public boolean contains(Object o) {
		return rules.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> otherColl) {
		return rules.containsAll(otherColl);
	}

	@Override
	public boolean isEmpty() {
		return rules.isEmpty();
	}

	@Override
	public Iterator<DecisionRule> iterator() {
		return rules.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return rules.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> otherColl) {
		return rules.removeAll(otherColl);
	}

	@Override
	public boolean retainAll(Collection<?> otherColl) {
		return rules.retainAll(otherColl);
	}

	@Override
	public int size() {
		return rules.size();
	}

	@Override
	public Object[] toArray() {
		return rules.toArray();
	}

	@Override
	public <T> T[] toArray(T[] typedArr) {
		return rules.toArray(typedArr);
	}

	public String toString() {
		return rules.toString();
	}
	
}
