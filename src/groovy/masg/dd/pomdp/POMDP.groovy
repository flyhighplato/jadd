package masg.dd.pomdp

import masg.dd.AlgebraicDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.ProbDD;
import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;

class POMDP {
	private final FactoredCondProbDD initialBelief;
	private final AlgebraicDD rewFn;
	private final FactoredCondProbDD transnFn;
	private final FactoredCondProbDD observFn;
	private final DDVariableSpace states;
	private final DDVariableSpace statesPrime;
	private final DDVariableSpace observations;
	private final DDVariableSpace actions;
	
	private Map actRestrTransnFn = [:]
	private Map actRestrObservFn = [:]
	private Map actRestTransObservFn = [:]
	private Map actRestrRewFn = [:]
	
	private Map actObsRestrObservFn = [:];
	
	
	private DDVariableSpace actSpace;
	private DDVariableSpace obsSpace;
	
	public POMDP(FactoredCondProbDD initialBelief, AlgebraicDD rewFn, FactoredCondProbDD transnFn, FactoredCondProbDD observFn, DDVariableSpace states, DDVariableSpace observations, DDVariableSpace actions) {
		transnFn = transnFn.normalize();
		observFn = observFn.normalize();
		
		this.initialBelief = initialBelief;
		this.rewFn = rewFn;
		this.transnFn = transnFn;
		this.observFn = observFn;
		this.states = states;
		
		ArrayList<DDVariable> temp = new ArrayList<DDVariable>();
		for(DDVariable var:states.getVariables()) {
			temp.add(var.getPrimed());
		}
		statesPrime = new DDVariableSpace(temp);
		
		this.observations = observations;
		this.actions = actions;
		
		
		actions.each{ HashMap<DDVariable,Integer> actSpacePt ->
			actRestrTransnFn[actSpacePt]=transnFn.restrict(actSpacePt);
			actRestrObservFn[actSpacePt]=observFn.restrict(actSpacePt);
			actRestrRewFn[actSpacePt]=rewFn.restrict(actSpacePt);
			
			
			actObsRestrObservFn[actSpacePt] = [:]
			
			
			FactoredCondProbDD restrTransFn = actRestrTransnFn[actSpacePt];
			restrTransFn = restrTransFn.normalize();
			
			FactoredCondProbDD restrObsFn = actRestrObservFn[actSpacePt];
			restrObsFn = restrObsFn.normalize();
			
			FactoredCondProbDD restrTransObsFn = restrTransFn.multiply(restrObsFn);
			actRestTransObservFn[actSpacePt] = restrTransObsFn.normalize();
			
			observations.each { HashMap<DDVariable,Integer> obsSpacePt ->
				actObsRestrObservFn[actSpacePt][obsSpacePt] = ((FactoredCondProbDD)actRestrObservFn[actSpacePt]).restrict(obsSpacePt).normalize()
			}
		}
		
	}
	
	public final FactoredCondProbDD getInitialBelief() {
		return initialBelief;
	}
	
	public final AlgebraicDD getRewardFunction() {
		return rewFn;
	}
	
	public final AlgebraicDD getRewardFunction(HashMap<DDVariable,Integer> actSpacePt) {
		return actRestrRewFn[actSpacePt];
	}
	
	public final FactoredCondProbDD getTransitionFunction() {
		return transnFn;
	}
	
	public final FactoredCondProbDD getTransitionFunction(HashMap<DDVariable,Integer> actSpacePt) {
		return actRestrTransnFn[actSpacePt];
	}
	
	public final FactoredCondProbDD getTransitionFunction(HashMap<DDVariable,Integer> actSpacePt, HashMap<DDVariable,Integer> obsSpacePt) {
		return actObsRestrTransnFn[actSpacePt][obsSpacePt];
	}
	
	public final FactoredCondProbDD getObservationFunction() {
		return observFn;
	}
	
	public final FactoredCondProbDD getObservationFunction(HashMap<DDVariable,Integer> obsSpacePt) {
		return actRestrObservFn[obsSpacePt];
	}
	
	public final FactoredCondProbDD getObservationFunction(HashMap<DDVariable,Integer> actSpacePt, HashMap<DDVariable,Integer> obsSpacePt) {
		return actObsRestrObservFn[actSpacePt][obsSpacePt];
	}
	
	public final FactoredCondProbDD getObservedTransitionFunction(HashMap<DDVariable,Integer> actSpacePt) {
		return actRestTransObservFn[actSpacePt]
	}
	
	public final DDVariableSpace getStates() {
		return states;
	}
	
	public final DDVariableSpace getStatesPrime() {
		return statesPrime;
	}
	
	public final DDVariableSpace getObservations() {
		return observations;
	}
	
	public final DDVariableSpace getActions() {
		return actions;
	}
}
