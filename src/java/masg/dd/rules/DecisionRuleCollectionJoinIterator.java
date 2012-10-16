package masg.dd.rules;

import java.util.Iterator;
import java.util.List;

public class DecisionRuleCollectionJoinIterator implements Iterator<JoinResult>, Iterable<JoinResult> {

	private JoinResult current;
	private JoinResult next;
	
	private DecisionRuleCollectionIndex indexLonger;
	int indexLongerPos = 0;
	private DecisionRuleCollectionIndex indexShorter;
	
	public DecisionRuleCollectionJoinIterator(DecisionRuleCollectionIndex index1, DecisionRuleCollectionIndex index2) throws Exception {
		this.indexLonger = index1;
		this.indexShorter = index2;
		
		if(index2.maxValLen > index1.maxValLen) {
			this.indexLonger = index2;
			this.indexShorter = index1;
		}
		
		//Sets "next"
		setNext();
		//Sets "current"
		setNext();
	}
	
	private void setNext() throws Exception {
		
		while(indexLongerPos<indexLonger.locMinIndex.length){
			
			if(indexLonger.locMinIndex[indexLongerPos]!=-1) {
				int j = 0;
				for(int currBitIndex = 0; currBitIndex<indexShorter.maxValLen;++currBitIndex) {
					j = j | (indexLongerPos & (1 << currBitIndex));
				}
				
				if(indexShorter.locMinIndex[j]!=-1) {
					List<DecisionRule> rulesLeft = indexLonger.getRules().subList(indexLonger.locMinIndex[indexLongerPos], indexLonger.locMaxIndex[indexLongerPos]+1);
					List<DecisionRule> rulesRight = indexShorter.getRules().subList(indexShorter.locMinIndex[j], indexShorter.locMaxIndex[j]+1);
					
					current = next;
					next = new JoinResult(rulesLeft,rulesRight);
					++indexLongerPos;
					return;
				}
				else {
					throw new Exception("Could not fully join indices");
				}
			}
			
			++indexLongerPos;
		}
		
		current = next;
		next = null;
	}

	@Override
	public boolean hasNext() {
		return current!=null;
	}

	@Override
	public JoinResult next() {
		JoinResult temp = current;
		try {
			setNext();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return temp;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove not supported");
	}

	@Override
	public Iterator<JoinResult> iterator() {
		return this;
	}

}
