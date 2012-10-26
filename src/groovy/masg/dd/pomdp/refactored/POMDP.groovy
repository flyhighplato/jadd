package masg.dd.pomdp.refactored

import masg.dd.refactored.AlgebraicDD
import masg.dd.refactored.CondProbDD
import masg.dd.refactored.ProbDD
import masg.dd.vars.DDVariable

class POMDP {
	private final ProbDD initialBelief;
	private final AlgebraicDD rewFn;
	private final CondProbDD transnFn;
	private final CondProbDD observFn;
	private final ArrayList<DDVariable> states;
	private final ArrayList<DDVariable> statesPrime;
	private final ArrayList<DDVariable> observations;
	private final ArrayList<DDVariable> actions;
	
	public POMDP(ProbDD initialBelief, AlgebraicDD rewFn, CondProbDD transnFn, CondProbDD observFn, ArrayList<DDVariable> states, ArrayList<DDVariable> observations, ArrayList<DDVariable> actions) {
		this.initialBelief = initialBelief;
		this.rewFn = rewFn;
		this.transnFn = transnFn;
		this.observFn = observFn;
		this.states = states;
		
		ArrayList<DDVariable> temp = new ArrayList<DDVariable>();
		for(DDVariable var:states) {
			temp.add(var.getPrimed());
		}
		statesPrime = temp;
		
		this.observations = observations;
		this.actions = actions;
	}
	
	public final ProbDD getInitialBelief() {
		return initialBelief;
	}
	
	public final AlgebraicDD getRewardFunction() {
		return rewFn;
	}
	
	public final CondProbDD getTransitionFunction() {
		return transnFn;
	}
	
	public final CondProbDD getObservationFunction() {
		return observFn;
	}
	
	public final ArrayList<DDVariable> getStates() {
		return states;
	}
	
	public final ArrayList<DDVariable> getStatesPrime() {
		return statesPrime;
	}
	
	public final ArrayList<DDVariable> getObservations() {
		return observations;
	}
	
	public final ArrayList<DDVariable> getActions() {
		return actions;
	}
}
