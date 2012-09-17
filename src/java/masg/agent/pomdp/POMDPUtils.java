package masg.agent.pomdp;

import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.function.CondProbFunction;
import masg.dd.pomdp.POMDP;
import masg.dd.vars.DDVariable;


public class POMDPUtils {
	static public CondProbFunction updateBelief(POMDP p, CondProbFunction belief, HashMap<DDVariable,Integer> acts, HashMap<DDVariable,Integer> obs) throws Exception {
		HashMap<DDVariable, Integer> fixAt = new HashMap<DDVariable, Integer>();
		
		for(Entry<DDVariable,Integer> e: acts.entrySet()) {
			fixAt.put(e.getKey(), e.getValue());
		}
		
		for(Entry<DDVariable,Integer> e: obs.entrySet()) {
			fixAt.put(e.getKey(), e.getValue());
		}
		
		CondProbFunction fixedObsFn = p.getObsFns().restrict(fixAt);
		CondProbFunction fixedTransFn = p.getTransFn().restrict(fixAt);
		CondProbFunction temp = fixedTransFn.times(belief);
		
			
		temp = temp.sumOut(p.getStates(),false);
		CondProbFunction currBeliefFn = temp.times(fixedObsFn);
		currBeliefFn.normalize();
		currBeliefFn.unprimeAllContexts();
		
		return currBeliefFn;
	}
}
