package masg.dd.pomdp.agent.policy;

import java.util.ArrayList;
import java.util.HashMap;

import masg.dd.AlgebraicDD
import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.pomdp.agent.belief.Belief;
import masg.dd.representations.dag.MutableDDLeaf
import masg.dd.variables.DDVariable;

public class AlphaVectorPolicy implements Policy {

	private ArrayList<BeliefAlphaVector> alphaVectors = new ArrayList<BeliefAlphaVector>();
	
	public AlphaVectorPolicy(ArrayList<BeliefAlphaVector> alphaVectors) {
		this.alphaVectors = alphaVectors;
	}
	
	@Override
	public HashMap<DDVariable, Integer> getAction(Belief belief) {
		return belief.pickBestAlpha(alphaVectors).getAction();
	}
	
	public AlgebraicDD getValueFunction() {
		AlgebraicDD valueFunction = new AlgebraicDD(new MutableDDLeaf(-Double.MAX_VALUE))
		for(BeliefAlphaVector alpha: alphaVectors) {
			valueFunction = alpha.valueFunction.max(valueFunction);
		}
		return valueFunction
	}

}
