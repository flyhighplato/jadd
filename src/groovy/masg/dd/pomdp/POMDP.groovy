package masg.dd.pomdp

import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.ProbDD;
import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;

class POMDP {
	private final ProbDD initialBelief;
	private final AlgebraicDD rewFn;
	private final CondProbDD transnFn;
	private final CondProbDD observFn;
	private final ArrayList<DDVariable> states;
	private final ArrayList<DDVariable> statesPrime;
	private final ArrayList<DDVariable> observations;
	private final ArrayList<DDVariable> actions;
	
	private Map actRestrTransnFn = [:]
	private Map actRestrObservFn = [:]
	private Map actRestTransObservFn = [:]
	private Map actRestrRewFn = [:]
	
	private Map actObsRestrTransnFn = [:]
	private Map actObsRestrObservFn = [:];
	
	
	private DDVariableSpace actSpace;
	private DDVariableSpace obsSpace;
	
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
		
		actSpace = new DDVariableSpace(actions);
		obsSpace = new DDVariableSpace(observations);
		
		actSpace.each{ HashMap<DDVariable,Integer> actSpacePt ->
			actRestrTransnFn[actSpacePt]=transnFn.restrict(actSpacePt);
			actRestrObservFn[actSpacePt]=observFn.restrict(actSpacePt);
			actRestrRewFn[actSpacePt]=rewFn.restrict(actSpacePt);
			
			
			actObsRestrTransnFn[actSpacePt] = [:]
			actObsRestrObservFn[actSpacePt] = [:]
			
			
			CondProbDD restrTransFn = actRestrTransnFn[actSpacePt];
			CondProbDD norm = restrTransFn.sumOut(getStatesPrime());
			restrTransFn = restrTransFn.div(norm);
			
			CondProbDD restrObsFn = actRestrObservFn[actSpacePt];
			norm = restrObsFn.sumOut(getObservations());
			restrObsFn = restrObsFn.div(norm);
			
			CondProbDD restrTransObsFn = restrTransFn.multiply(restrObsFn);
			norm = restrTransObsFn.sumOut(getStatesPrime());
			actRestTransObservFn[actSpacePt] = restrTransObsFn.div(norm);
			
			obsSpace.each { HashMap<DDVariable,Integer> obsSpacePt ->
				actObsRestrTransnFn[actSpacePt][obsSpacePt] = actRestrTransnFn[actSpacePt].restrict(obsSpacePt)
				actObsRestrObservFn[actSpacePt][obsSpacePt] = actRestrObservFn[actSpacePt].restrict(obsSpacePt)
			}
		}
		
	}
	
	public final ProbDD getInitialBelief() {
		return initialBelief;
	}
	
	public final AlgebraicDD getRewardFunction() {
		return rewFn;
	}
	
	public final AlgebraicDD getRewardFunction(HashMap<DDVariable,Integer> actSpacePt) {
		return actRestrRewFn[actSpacePt];
	}
	
	public final CondProbDD getTransitionFunction() {
		return transnFn;
	}
	
	public final CondProbDD getTransitionFunction(HashMap<DDVariable,Integer> actSpacePt) {
		return actRestrTransnFn[actSpacePt];
	}
	
	public final CondProbDD getTransitionFunction(HashMap<DDVariable,Integer> actSpacePt, HashMap<DDVariable,Integer> obsSpacePt) {
		return actObsRestrTransnFn[actSpacePt][obsSpacePt];
	}
	
	public final CondProbDD getObservationFunction() {
		return observFn;
	}
	
	public final CondProbDD getObservationFunction(HashMap<DDVariable,Integer> obsSpacePt) {
		return actRestrObservFn[obsSpacePt];
	}
	
	public final CondProbDD getObservationFunction(HashMap<DDVariable,Integer> actSpacePt, HashMap<DDVariable,Integer> obsSpacePt) {
		return actObsRestrObservFn[actSpacePt][obsSpacePt];
	}
	
	public final CondProbDD getObservedTransitionFunction(HashMap<DDVariable,Integer> actSpacePt) {
		return actRestTransObservFn[actSpacePt]
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
	
	public final DDVariableSpace getObservationSpace() {
		return obsSpace;
	}
	
	public final ArrayList<DDVariable> getActions() {
		return actions;
	}
	
	public final DDVariableSpace getActionSpace() {
		return actSpace;
	}
}
