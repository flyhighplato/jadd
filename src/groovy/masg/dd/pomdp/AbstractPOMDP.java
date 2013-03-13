package masg.dd.pomdp;

import java.util.ArrayList;

import masg.dd.AlgebraicDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;

public abstract class AbstractPOMDP {
	protected AlgebraicDD rewFn;
	protected FactoredCondProbDD transnFn;
	protected ArrayList<DDVariable> states;
	protected ArrayList<DDVariable> statesPrime;
	protected ArrayList<DDVariable> actions;
	
	protected FactoredCondProbDD initialBelief;
	protected FactoredCondProbDD observFn;
	protected ArrayList<DDVariable> observations;
	
	protected DDVariableSpace actSpace;
	protected DDVariableSpace obsSpace;
	
	
	public AbstractPOMDP(FactoredCondProbDD initialBelief, AlgebraicDD rewFn, FactoredCondProbDD transnFn, FactoredCondProbDD observFn, ArrayList<DDVariable> states, ArrayList<DDVariable> observations, ArrayList<DDVariable> actions) {
		transnFn = transnFn.normalize();
		observFn = observFn.normalize();
		
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
		
	}

	
	public final FactoredCondProbDD getInitialBelief() {
		return initialBelief;
	}
	
	public final AlgebraicDD getRewardFunction() {
		return rewFn;
	}
	
	public final FactoredCondProbDD getTransitionFunction() {
		return transnFn;
	}
	
	public final FactoredCondProbDD getObservationFunction() {
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
	
	public final DDVariableSpace getObservationSpace() {
		return obsSpace;
	}
	
	public final ArrayList<DDVariable> getActions() {
		return actions;
	}
	
	public final DDVariableSpace getActionSpace() {
		return actSpace;
	}
	
	public double getDiscount() {
		return 0.85d;
	}
	
	public double getTolerance() {
		return 0.0001d;
	}
}
