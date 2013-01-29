package masg.dd.pomdp.agent.belief;

import masg.dd.FactoredCondProbDD;

public interface Belief {
	FactoredCondProbDD getBeliefFunction();
}
