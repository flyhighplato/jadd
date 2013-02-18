package masg.dd.ipomdp

import java.util.ArrayList;

import masg.dd.AlgebraicDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;

import masg.dd.pomdp.AbstractPOMDP

class IPOMDP extends AbstractPOMDP{
	
	protected ArrayList<DDVariable> actionsOther;
	protected DDVariableSpace actOtherSpace;
	
	
	public IPOMDP(FactoredCondProbDD initialBelief, AlgebraicDD rewFn, FactoredCondProbDD transnFn, FactoredCondProbDD observFn, ArrayList<DDVariable> states, ArrayList<DDVariable> observations, ArrayList<DDVariable> actions, ArrayList<DDVariable> actionsOther) {
		super(initialBelief,rewFn, transnFn, observFn, states, observations, actions);
		
		this.actionsOther = actionsOther;
		actOtherSpace = new DDVariableSpace(actionsOther);
	}
	
	public ArrayList<DDVariable> getActionsOther() {
		return actionsOther;
	}
	
	public DDVariableSpace getActionOtherSpace() {
		return actOtherSpace;
	}
}
