package masg.dd.pomdp.agent.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.AlgebraicDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.alphavector.AlphaVector;
import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.pomdp.agent.belief.Belief;
import masg.dd.representation.DDElement;
import masg.dd.representation.DDLeaf;
import masg.dd.representation.builder.DDBuilder;
import masg.dd.variables.DDVariable;

public class BeliefAlphaVectorPolicy implements AlphaVectorPolicy {
	private ArrayList<AlphaVector> alphaVectors = new ArrayList<AlphaVector>();
	private ArrayList<DDElement> alphaVectorElems = new ArrayList<DDElement>();
	private ArrayList<DDLeaf> maxValues = new ArrayList<DDLeaf>();
	private ArrayList<DDLeaf> minValues = new ArrayList<DDLeaf>();
	private ArrayList<HashMap<DDLeaf,DDLeaf>> alphaIndices = new ArrayList<HashMap<DDLeaf,DDLeaf>>();
	
	private double maxLeafValue = -Double.MAX_VALUE;
	private double minLeafValue = Double.MAX_VALUE;
	
	public BeliefAlphaVectorPolicy() {
		
	}
	
	public BeliefAlphaVectorPolicy(ArrayList<BeliefAlphaVector> alphaVectors) {
		for(int i=0;i<alphaVectors.size();++i) {
			if(this.alphaVectors.size()%10 == 0) {
				System.out.println("Added " + this.alphaVectors.size() + " alpha vectors");
			}
			addAlphaVector(alphaVectors.get(i));
		}
	}
	
	public HashMap<HashMap<DDVariable, Integer>, Double> getActionDistribution(FactoredCondProbDD b) {
		
		
		double totalWeight = 0.0d;
		HashMap<AlphaVector,Double> alphaDist = new HashMap<AlphaVector,Double>();
		for(AlphaVector alpha:alphaVectors) {
			
			AlgebraicDD temp = alpha.getValueFunction().plus(-minLeafValue);
			temp = temp.div(maxLeafValue-minLeafValue);
			double prob = b.dotProduct(temp);
			
			if(prob<0.0d || prob>1.0d || Double.isNaN(prob)) {
				System.out.println("IMPROBABLE!");
			}
			totalWeight += prob;
			if(!alphaDist.containsKey(alpha.getAction())) {
				alphaDist.put(alpha, prob);
			}
			else {
				alphaDist.put(alpha, alphaDist.get(alpha) + prob);
			}
		}
		
		HashMap<AlphaVector,Double> normAlphaDist = new HashMap<AlphaVector,Double>();
		for(Entry<AlphaVector,Double> e: alphaDist.entrySet()) {
			normAlphaDist.put(e.getKey(), e.getValue()/totalWeight);
		}
		
		alphaDist = normAlphaDist;
		normAlphaDist = new HashMap<AlphaVector,Double>();
		totalWeight = 0.0d;
		for(Entry<AlphaVector,Double> e1: alphaDist.entrySet()) {
			double probWin = e1.getValue();
			
			if(e1.getValue()<0.0d || e1.getValue()>1.0d || Double.isNaN(e1.getValue())) {
				System.out.println("IMPROBABLE!");
			}
			
			for(Entry<AlphaVector,Double> e2: alphaDist.entrySet()) {
				if(e2 != e1) {
					probWin*=(1-e2.getValue());
				}
			}
			
			totalWeight+=probWin;
			
			normAlphaDist.put(e1.getKey(), probWin);
		}
		alphaDist = normAlphaDist;
		
		HashMap<HashMap<DDVariable,Integer>,Double> actDist = new HashMap<HashMap<DDVariable,Integer>,Double>();
		
		for(Entry<AlphaVector,Double> e1: alphaDist.entrySet()) {
			if(!actDist.containsKey(e1.getKey().getAction())) {
				actDist.put(e1.getKey().getAction(), e1.getValue()/totalWeight);
			}
			else {
				actDist.put(e1.getKey().getAction(), actDist.get(e1.getKey().getAction()) + e1.getValue()/totalWeight);
			}
		}
		
		return actDist;
	}
	
	public final boolean addAlphaVector(AlphaVector alphaVector) {
		
		boolean dominated = false;
		for(AlphaVector oldAlpha:alphaVectors) {
			if(DDBuilder.findMaxLeaf(alphaVector.getValueFunction().minus(oldAlpha.getValueFunction()).getFunction()).getValue() < 0.0001d) {
				dominated = true;
				break;
			}
		}
		
		if(!dominated) {
		
			DDElement alpha = alphaVector.getValueFunction().getFunction();
			
			DDLeaf maxLeaf = DDBuilder.findMaxLeaf(alpha);
			DDLeaf minLeaf = DDBuilder.findMinLeaf(alpha);
			
			if( maxLeaf.getValue() > maxLeafValue ) {
				maxLeafValue = maxLeaf.getValue();
			}
			
			if( minLeaf.getValue() < minLeafValue ) {
				minLeafValue = minLeaf.getValue();
			}
			
			maxValues.add(maxLeaf);
			minValues.add(minLeaf);
			alphaVectorElems.add(alpha);
			alphaVectors.add(alphaVector);
			
			HashMap<DDLeaf,DDLeaf> index = new HashMap<DDLeaf,DDLeaf>();
			DDBuilder.indexAlphaVector(index, alpha);
			alphaIndices.add(index);
			
			
			return true;
		}
		
		return false;
	}
	
	public final ArrayList<AlphaVector> getAlphaVectors() {
		return new ArrayList<AlphaVector>(alphaVectors);
	}
	
	@Override
	public HashMap<DDVariable, Integer> getAction(Belief belief) {
		AlphaVector bestAlpha = getAlphaVector(belief);
		
		if(bestAlpha!=null)
			return bestAlpha.getAction();
		
		return null;
	}

	public HashMap<DDVariable, Integer> getAction(FactoredCondProbDD beliefFn) {
		AlphaVector bestAlpha = getAlphaVector(beliefFn);
		
		if(bestAlpha!=null)
			return bestAlpha.getAction();
		
		return null;
	}
	
	@Override
	public AlphaVector getAlphaVector(Belief belief) {
		return getAlphaVector(belief.getBeliefFunction());
	}
	
	public AlphaVector getAlphaVector(FactoredCondProbDD beliefFn) {
		ArrayList<DDElement> probElems = new ArrayList<DDElement>();
		ArrayList<DDElement> realElems = new ArrayList<DDElement>(alphaVectorElems);
		ArrayList<DDLeaf> realMaxElems = new ArrayList<DDLeaf>(maxValues);
		ArrayList<DDLeaf> realMinElems = new ArrayList<DDLeaf>(minValues);
		
		
		for(int i=0;i<beliefFn.getFunctions().size();++i) {
			probElems.add(beliefFn.getFunctions().get(i).getFunction().getFunction());
		}
		
		ArrayList<HashMap<DDLeaf,DDLeaf>> leafCountsCopy = new ArrayList<HashMap<DDLeaf,DDLeaf>>();
		for(HashMap<DDLeaf,DDLeaf> leafCountOld:alphaIndices) {
			leafCountsCopy.add(new HashMap<DDLeaf,DDLeaf>(leafCountOld));
		}
		return alphaVectors.get(DDBuilder.maxDotProduct(probElems,realElems,realMaxElems,realMinElems,leafCountsCopy));
	}
}
