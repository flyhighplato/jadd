package masg.dd.pomdp.agent.policy;

import java.util.HashMap;

import masg.dd.pomdp.agent.belief.Belief;
import masg.dd.variables.DDVariable;

public interface Policy {
	public HashMap<DDVariable,Integer> getAction(Belief belief);
}
