package masg.dd.pomdp.agent.policy;

import java.util.ArrayList;

import masg.dd.alphavector.AlphaVector;
import masg.dd.pomdp.agent.belief.Belief;

public interface AlphaVectorPolicy extends Policy {
	public AlphaVector getAlphaVector(Belief b2);
	public ArrayList<AlphaVector> getAlphaVectors();
}
