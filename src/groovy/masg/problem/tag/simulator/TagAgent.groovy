package masg.problem.tag.simulator;

import masg.dd.pomdp.POMDP;
import masg.dd.pomdp.agent.belief.Belief
import masg.dd.pomdp.agent.policy.Policy
import masg.dd.variables.DDVariable

public class TagAgent {
	protected POMDP pomdp;
	protected Policy policy;
	
	private Belief currBelief;
	
	protected int row, column = 0;
	
	public DDVariable rowVar
	public DDVariable colVar
	
	public TagAgent(POMDP pomdp, Policy policy) {
		rowVar = new DDVariable('a1_row',5)
		colVar = new DDVariable('a1_col',5)
		
		this.pomdp = pomdp;
		this.policy = policy;
		this.currBelief = new Belief(pomdp, pomdp.getInitialBelief());
		
		updateState();
	}
	
	private void updateState(HashMap<DDVariable,Integer> observation) {
		HashMap<DDVariable,Integer> statePoint = currBelief.sampleCurrentState();
		row = statePoint.get(rowVar);
		column = statePoint.get(colVar);
	}
	
	public int getRow() {
		return row;
	}
	
	public int getColumn() {
		return column;
	}
}
