package masg.dd.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class DecisionRuleCollectionIndex {
	
	int[] locMinIndex;
	int[] locMaxIndex;
	
	int maxValLen = 0;
	DecisionRuleCollectionIndex[] subIndex;
	
	
	static private double tolerance = 0.00001f;
	
	private final List<DecisionRule> rules;
	
	static int MAX_INDEX_VALUE_LEN = 32;
	
	private final int ruleIxStart;
	private final int ruleIxEnd;
	private final int bitIxStart;
	private final int parentVal;
	
	
	private DecisionRuleCollectionIndex(final List<DecisionRule> rules) {
		this.rules = rules;
		this.ruleIxStart = 0;
		this.ruleIxEnd = rules.size();
		this.bitIxStart = 0;
		this.parentVal = 0;
	}
	
	private DecisionRuleCollectionIndex(int ruleIxStart, int ruleIxEnd, int bitIxStart, int parentVal, final List<DecisionRule> rules) {
		this.rules = rules;
		this.ruleIxStart = ruleIxStart;
		this.ruleIxEnd = ruleIxEnd;
		this.bitIxStart = bitIxStart;
		this.parentVal = parentVal;
	}
	
	public final List<DecisionRule> getRules() {
		return rules;
		
	}
	
	public static DecisionRuleCollectionIndex index(List<DecisionRule> rules) throws Exception {
		return index(0,rules.size(),0,0,rules);
	}
	
	public static DecisionRuleCollectionIndex index(int ruleIxStart, int ruleIxEnd, int bitIxStart, int parentVal, List<DecisionRule> rules) throws Exception {
		
		
		int maxLen = getMaxIndexValueLength(ruleIxStart, ruleIxEnd, bitIxStart, rules);
		
		boolean isSorted = true;
		DecisionRule rPrev = null;
		
		boolean prevNeg = false;
		boolean firstComp = true;
		for(DecisionRule rCurr: rules) {
			if(rPrev!=null) {
				if(rCurr.compareTo(rPrev)>0) {
					
					if(!firstComp) {
						if(prevNeg==true) {
							isSorted = false;
							break;
						}
					}
					firstComp = false;
					prevNeg = false;
				}
				else if(rCurr.compareTo(rPrev)<0) {
					
					if(!firstComp) {
						if(prevNeg==false) {
							isSorted = false;
							break;
						}
					}
					firstComp = false;
					prevNeg = true;
				}
			}
			rPrev = rCurr;
		}
		
		if(!isSorted) {
			Collections.sort(rules);
		}
		
		

		DecisionRuleCollectionIndex index;
		if(maxLen==0) {
			return null;
		}
		else {
			index = new DecisionRuleCollectionIndex(ruleIxStart, ruleIxEnd, bitIxStart, parentVal, rules);
			
			index.maxValLen = maxLen;
			index.locMinIndex = new int[(int)Math.ceil(Math.pow(2, maxLen))];
			index.locMaxIndex = new int[(int)Math.ceil(Math.pow(2, maxLen))];
			
			Arrays.fill(index.locMinIndex, -1);
			Arrays.fill(index.locMaxIndex, -1);
			
			for(int i=ruleIxStart;i<ruleIxEnd;++i) {
				DecisionRule rule = rules.get(i);
				
				int ix = getIntValue(rule, bitIxStart, maxLen);
				
				//Rule blocks should be contiguous
				if(index.locMaxIndex[ix]!=-1 && index.locMaxIndex[ix]!=i-1) {
					throw new Exception("Index became corrupted");
				}
				
				index.locMaxIndex[ix] = i;
				
				//Should only have to set this once, if sorted correctly
				if(index.locMinIndex[ix]==-1) {
					index.locMinIndex[ix] = i;
					
				}
				else {
					if(index.locMinIndex[ix]>i || index.locMinIndex[ix]>index.locMaxIndex[ix] ) {
						throw new Exception("Index became corrupted");
					}
				}

			}
			
			
		}
		
		return index;
	}
	
	private static int getIntValue(DecisionRule rule, int startBitIx, int maxLen) throws Exception {
		if(maxLen == 0) 
			return 0;
		
		int ix = 0;
		for(int currBitIndex = startBitIx; currBitIndex<startBitIx + maxLen;++currBitIndex) {
			if(rule.getBit(currBitIndex)=='1')
				ix = ix | (1 << currBitIndex);
			else if (rule.getBit(currBitIndex)=='*') {
				throw new Exception("Cannot make integer value from rule: " + rule);
			}
		}
		
		return ix;
	}
	
	private static int getMaxIndexValueLength(int ruleIxStart, int ruleIxEnd, int bitIxStart, List<DecisionRule> rules) {
		int maxLen = MAX_INDEX_VALUE_LEN;
		
		if(rules==null || rules.size()<=0 || ruleIxEnd-ruleIxStart <= 0) {
			return 0;
		}
		
		if(rules.get(0).getNumBits()-bitIxStart > 7)
			maxLen = Math.min(maxLen, (rules.get(0).getNumBits() - bitIxStart) - 10);
		else
			return 0;
		
		for(int i=ruleIxStart;i<ruleIxEnd;++i) {
			DecisionRule rule = rules.get(i);
			
			for(int currBitIndex = maxLen + bitIxStart; currBitIndex>=bitIxStart;--currBitIndex) {
				if(rule.getBit(currBitIndex)=='*' && currBitIndex<maxLen + bitIxStart){
					maxLen = currBitIndex - bitIxStart;
				}
			}
			
			if(maxLen<=0)
				return 0;
		}
		
		return maxLen;
	}

	public ArrayList<List<DecisionRule>> getCandidateMatches(DecisionRule refRule) throws Exception {
		ArrayList<List<DecisionRule>> temp = new ArrayList<List<DecisionRule>>();
		for(int i=0;i<locMinIndex.length;++i) {
			
			if((locMinIndex[i]==-1) != (locMaxIndex[i]==-1)) {
				throw new Exception("Index is corrupted");
			}
			
			
			if(locMinIndex[i]!=-1) {
				boolean matches = true;
				for(int currBitIndex = 0; currBitIndex<maxValLen;++currBitIndex) {
					boolean setInIx = ((i & (1 << currBitIndex)) > 0);
					if( !( (setInIx && refRule.getBit(currBitIndex)=='1') || (!setInIx && refRule.getBit(currBitIndex)=='0') || refRule.getBit(currBitIndex)=='*') ) {
						matches = false;
						break;
					}
				}
				
				if(matches) {
					temp.add(rules.subList(locMinIndex[i], locMaxIndex[i]+1));
				}
			}
		}
		return temp;
	}
	
	public ArrayList<ArrayList<List<DecisionRule>>> getCandidateMatches(DecisionRuleCollectionIndex refIndex) throws Exception {
		ArrayList<ArrayList<List<DecisionRule>>> temp = new ArrayList<ArrayList<List<DecisionRule>>>();
		
		DecisionRuleCollectionIndex drIxLonger = refIndex;
		DecisionRuleCollectionIndex drIxShorter = this;
		
		if(maxValLen > refIndex.maxValLen) {
			drIxLonger = this;
			drIxShorter = refIndex;
		}
		
		for(int i=0;i<drIxLonger.locMinIndex.length;++i) {
			
			if(drIxLonger.locMinIndex[i]!=-1) {
				int j = 0;
				for(int currBitIndex = 0; currBitIndex<drIxShorter.maxValLen;++currBitIndex) {
					j = j | (i & (1 << currBitIndex));
				}
				
				if(drIxShorter.locMinIndex[j]!=-1) {
					ArrayList<List<DecisionRule>> matchTuple = new ArrayList<List<DecisionRule>>(2);
					matchTuple.add(drIxLonger.rules.subList(drIxLonger.locMinIndex[i], drIxLonger.locMaxIndex[i]+1));
					matchTuple.add(drIxShorter.rules.subList(drIxShorter.locMinIndex[j], drIxShorter.locMaxIndex[j]+1));
					
					temp.add(matchTuple);
				}
			}
		}
		return temp;
	}
}
