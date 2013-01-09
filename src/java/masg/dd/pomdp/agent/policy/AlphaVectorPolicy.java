package masg.dd.pomdp.agent.policy;

import java.util.ArrayList;
import java.util.HashMap;

import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.pomdp.agent.belief.Belief;
import masg.dd.variables.DDVariable;

public class AlphaVectorPolicy implements Policy {

	private ArrayList<BeliefAlphaVector> alphaVectors = new ArrayList<BeliefAlphaVector>();
	
	public AlphaVectorPolicy(ArrayList<BeliefAlphaVector> alphaVectors) {
		this.alphaVectors = alphaVectors;
	}
	
	public final ArrayList<BeliefAlphaVector> getAlphaVectors() {
		return new ArrayList<BeliefAlphaVector>(alphaVectors);
	}
	
	@Override
	public HashMap<DDVariable, Integer> getAction(Belief belief) {
		return belief.pickBestAlpha(alphaVectors).getAction();
	}
}
