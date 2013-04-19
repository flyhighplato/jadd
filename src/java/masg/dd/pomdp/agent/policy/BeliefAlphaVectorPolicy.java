package masg.dd.pomdp.agent.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	
	public BeliefAlphaVectorPolicy() {
		
	}
	
	public BeliefAlphaVectorPolicy(ArrayList<BeliefAlphaVector> alphaVectors) {
		for(int i=0;i<alphaVectors.size();++i) {
			addAlphaVector(alphaVectors.get(i));
		}
	}
	
	public final boolean addAlphaVector(AlphaVector alphaVector) {
		
		boolean dominated = false;
		for(AlphaVector oldAlpha:alphaVectors) {
			if(alphaVector.getValueFunction().maxAbsDiff(oldAlpha.getValueFunction()) < 0.0001d) {
				dominated = true;
				break;
			}
		}
		
		if(!dominated) {
		
			DDElement alpha = alphaVector.getValueFunction().getFunction();
			maxValues.add(DDBuilder.findMaxLeaf(alpha));
			minValues.add(DDBuilder.findMinLeaf(alpha));
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
