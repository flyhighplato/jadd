package masg.dd.pomdp

import masg.dd.AlgebraicDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.ProbDD;
import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;

class POMDP extends AbstractPOMDP {
	
	private Map actRestrTransnFn = [:]
	private Map actRestrObservFn = [:]
	private Map actRestTransObservFn = [:]
	private Map actRestrRewFn = [:]
	
	private Map actObsRestrObservFn = [:];
	
	
	public POMDP(FactoredCondProbDD initialBelief, AlgebraicDD rewFn, FactoredCondProbDD transnFn, FactoredCondProbDD observFn, ArrayList<DDVariable> states, ArrayList<DDVariable> observations, ArrayList<DDVariable> actions) {
		
		super(initialBelief,rewFn, transnFn, observFn, states, observations, actions);
		
		actSpace.each{ HashMap<DDVariable,Integer> actSpacePt ->
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
			
			obsSpace.each { HashMap<DDVariable,Integer> obsSpacePt ->
				actObsRestrObservFn[actSpacePt][obsSpacePt] = ((FactoredCondProbDD)actRestrObservFn[actSpacePt]).restrict(obsSpacePt).normalize()
			}
		}
		
	}
	
	public double getDiscount() {
		return 0.85d;
	}
	
	
	public final AlgebraicDD getRewardFunction(HashMap<DDVariable,Integer> actSpacePt) {
		return actRestrRewFn[actSpacePt];
	}
	
	public final FactoredCondProbDD getTransitionFunction(HashMap<DDVariable,Integer> actSpacePt) {
		return actRestrTransnFn[actSpacePt];
	}
	
	public final FactoredCondProbDD getTransitionFunction(HashMap<DDVariable,Integer> actSpacePt, HashMap<DDVariable,Integer> obsSpacePt) {
		return actObsRestrTransnFn[actSpacePt][obsSpacePt];
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
	
}
