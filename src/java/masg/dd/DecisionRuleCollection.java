package masg.dd;

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
		int bitCol = size-1;
		
		ArrayList<DecisionRule> rulesCopy = new ArrayList<DecisionRule>(rules);
		
		ArrayList<DecisionRule> nextRules = new ArrayList<DecisionRule>();
		ArrayList<DecisionRule> leaveAloneRules = new ArrayList<DecisionRule>();
		
		DecisionRule currRule = rulesCopy.remove(0);
		
		int oldRulesSize = Integer.MAX_VALUE;
		
		while(rules.size() < oldRulesSize && rules.size()>1) {
			System.out.println("Start compression iteration");
			oldRulesSize = rules.size();
			for(bitCol = size-1; bitCol>=0; bitCol--) {
				//System.out.println("Working set:" + rulesCopy);
				for(DecisionRule otherRule:rulesCopy) {
					
					if(currRule.value-otherRule.value<0.001f) {
						
						//Is it the same rule
						if(currRule.equals(otherRule)) {
							System.out.println(currRule + " same as " + otherRule);
							continue;
						}
						//Does current rule already cover this rule (or vice-versa)?
						boolean covers = true;
						for(int currBitIx=0;currBitIx<size;currBitIx++) {
							if( (currRule.setValues.get(currBitIx) && !otherRule.setValues.get(currBitIx)) || 
								(currRule.setValues.get(currBitIx) && currRule.truthValues.get(currBitIx) != otherRule.truthValues.get(currBitIx))) {
								covers = false;
								break;
							}
						}
						
						if(covers) {
							System.out.println(currRule + " covers " + otherRule);
							continue;
						}
						
						covers = true;
						for(int currBitIx=0;currBitIx<size;currBitIx++) {
							if( (otherRule.setValues.get(currBitIx) && !currRule.setValues.get(currBitIx)) || 
								(otherRule.setValues.get(currBitIx) && otherRule.truthValues.get(currBitIx) != currRule.truthValues.get(currBitIx))) {
								covers = false;
								break;
							}
						}
						
						if(covers) {
							System.out.println(currRule + " covered by " + otherRule);
							currRule = otherRule;
							continue;
						}
						
						boolean prefixMatch = true;
						
						for(int currBitIx=0;currBitIx<bitCol;currBitIx++) {
							if( (otherRule.setValues.get(currBitIx) != currRule.setValues.get(currBitIx)) || (otherRule.truthValues.get(currBitIx) != currRule.truthValues.get(currBitIx))) { 
								prefixMatch = false;
								break;
							}
						}
						
						boolean suffixMatch = true;
						
						for(int currBitIx=bitCol+1;currBitIx<size;currBitIx++) {
							if( (otherRule.setValues.get(currBitIx) != currRule.setValues.get(currBitIx)) || (otherRule.truthValues.get(currBitIx) != currRule.truthValues.get(currBitIx))) { 
								suffixMatch = false;
								break;
							}
						}
						
						boolean bitColMismatch = (currRule.setValues.get(bitCol) && otherRule.setValues.get(bitCol)) && (currRule.truthValues.get(bitCol) != otherRule.truthValues.get(bitCol));
						
						//Are they different by one bit in the current column?
						if(prefixMatch && suffixMatch && bitColMismatch) {
							System.out.println(currRule + " prefix matches " + otherRule);
							BitMap newTruthValues = currRule.truthValues.clone();
							newTruthValues.clear(bitCol);
							
							BitMap newSetValues = currRule.setValues.clone();
							newSetValues.clear(bitCol);
							
							currRule = new DecisionRule(currRule.numBits,newTruthValues,newSetValues,currRule.value);
						}
						else {
							//System.out.println(currRule + " not prefix matches " + otherRule);
							
							if(!currRule.setValues.get(bitCol)) {
								nextRules.add(currRule);
							}
							else {
								leaveAloneRules.add(currRule);
							}
							currRule = otherRule;
						}
						
					}
					else{
						if(!currRule.setValues.get(bitCol)) {
							System.out.println(currRule + " merges with " + otherRule);
							nextRules.add(currRule);
						}
						else {
							leaveAloneRules.add(currRule);
						}
						currRule = otherRule;
						
						continue;
					}
				}
				if(currRule!=null) {
					if(!currRule.setValues.get(bitCol)) {
						nextRules.add(currRule);
					}
					else {
						leaveAloneRules.add(currRule);
					}
				}
				System.out.println("next:" + nextRules);
				//System.out.println("leaveAloneRules:" + leaveAloneRules);
				System.out.println();
				rulesCopy = nextRules;
				if(rulesCopy.size()>0) {
					currRule = rulesCopy.remove(0);
					nextRules = new ArrayList<DecisionRule>();
				}
				else {
					currRule = null;
					break;
				}
			}
			
			
			if(rulesCopy.size()>0 || leaveAloneRules.size()>0 || currRule!=null) {
				rules = new ConcurrentSkipListSet<DecisionRule>();
				if(currRule!=null)
					rules.add(currRule);
				rules.addAll(rulesCopy);
				rules.addAll(leaveAloneRules);
			}
			
			System.out.println(oldRulesSize + " > " + rules.size());
			
			leaveAloneRules = new ArrayList<DecisionRule>();
			
		}
		
		
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
