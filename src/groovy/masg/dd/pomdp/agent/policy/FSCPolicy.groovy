package masg.dd.pomdp.agent.policy

import java.util.HashMap;
import java.util.HashSet;

import masg.dd.pomdp.agent.belief.Belief;
import masg.dd.representation.DDElement;
import masg.dd.variables.DDVariable;

class FSCPolicy implements Policy {
	
	FSCPolicyNode currNode
	FSCPolicyNode initNode
	
	Random rand = new Random()

	FSCPolicyFingerprintBuilder fpBuilder
	
	public FSCPolicy(FSCPolicyNode initialNode) {
		initNode = initialNode
		fpBuilder = new FSCPolicyFingerprintBuilder(initNode)
		reset()
	}
	
	@Override
	public HashMap<DDVariable, Integer> getAction(Belief belief) {
		double needMass = rand.nextDouble()
		double currMass = 0.0d;
		
		
		HashMap<DDVariable,Integer> retAction
		
		currNode.actionDistributionFn.each { HashMap<DDVariable,Integer> action, Double mass ->
			currMass += mass
			
			if(currMass>=needMass) {
				retAction = action
			}
		}
		
		fpBuilder.selectAction(retAction);
		return retAction;
	}
	
	public void update(HashMap<DDVariable, Integer> observation) {
		
		double needMass = rand.nextDouble()
		double currMass = 0.0d;
		
		def prevNode = currNode
		
		currNode.transitionFn[observation].each{ FSCPolicyNode n, Double mass ->
			if(currMass<needMass) {
				currNode = n
			}
			
			currMass += mass
		}
		
		fpBuilder.selectObservation(observation)
		fpBuilder.transition(currNode)
		
	}
	
	public void reset() {
		currNode = initNode
		fpBuilder.restartAt(initNode)
	}
	
	
	public String toString() {
		return fpBuilder.toString()
	}
	
}