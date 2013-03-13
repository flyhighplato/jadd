package masg.dd.pomdp

import masg.dd.AlgebraicDD
import masg.dd.FactoredCondProbDD
import masg.dd.variables.DDVariable
import masg.dd.variables.DDVariableSpace

class IPOMDP extends AbstractPOMDP {
	protected ArrayList<POMDP> otherAgents;
	
	public IPOMDP(FactoredCondProbDD initialBelief, AlgebraicDD rewFn, FactoredCondProbDD transnFn, FactoredCondProbDD observFn, ArrayList<DDVariable> states, ArrayList<DDVariable> observations, ArrayList<DDVariable> actions, ArrayList<POMDP> actionsOther) {
		super(initialBelief,rewFn, transnFn, observFn, states, observations, actions);
		
		this.otherAgents = otherAgents;
	}
}
