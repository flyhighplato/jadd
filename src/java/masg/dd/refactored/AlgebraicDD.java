package masg.dd.refactored;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import masg.dd.rules.operations.refactored.AdditionOperation;
import masg.dd.rules.operations.refactored.BinaryOperation;
import masg.dd.rules.operations.refactored.DivisionOperation;
import masg.dd.rules.operations.refactored.MultiplicationOperation;
import masg.dd.rules.refactored.ImmutableDDElement;
import masg.dd.rules.refactored.ImmutableDDLeaf;
import masg.dd.rules.refactored.ImmutableDDNode;
import masg.dd.rules.refactored.MutableDDElement;
import masg.dd.rules.refactored.MutableDDLeaf;
import masg.dd.rules.refactored.MutableDDNode;
import masg.dd.vars.DDVariable;
import masg.util.BitMap;

public class AlgebraicDD {
	protected ImmutableDDElement ruleCollection;
	
	public AlgebraicDD(ArrayList<DDVariable> vars, Closure<Double> c) {
		MutableDDElement mutableDDElement = MutableDDElementBuilder.build(vars, c, false);
		mutableDDElement.compress();
		if(mutableDDElement instanceof MutableDDNode)
			ruleCollection = new ImmutableDDNode((MutableDDNode) mutableDDElement);
		else if (mutableDDElement instanceof MutableDDElement)
			ruleCollection = new ImmutableDDLeaf((MutableDDLeaf)mutableDDElement);
	}
	
	public AlgebraicDD(MutableDDElement mutableDDElement) {
		mutableDDElement.compress();
		
		if(mutableDDElement instanceof MutableDDNode)
			ruleCollection = new ImmutableDDNode((MutableDDNode) mutableDDElement);
		else if (mutableDDElement instanceof MutableDDElement)
			ruleCollection = new ImmutableDDLeaf((MutableDDLeaf)mutableDDElement);
		
	}
	
	protected AlgebraicDD() {
		
	}
	
	static public AlgebraicDD build(ArrayList<DDVariable> vars, Closure<Double> c) {
		return new AlgebraicDD(vars,c);
	}
	
	public Double getValue(HashMap<DDVariable,Integer> varSpacePoint) {
		BitMap bm = MutableDDElementBuilder.varSpacePointToBitMap(ruleCollection.getVariables(), varSpacePoint);
		return ruleCollection.getValue(ruleCollection.getVariables(), bm);
	}
	
	public AlgebraicDD restrict(HashMap<DDVariable,Integer> varSpacePoint) {
		return new AlgebraicDD(ruleCollection.restrict(varSpacePoint));
	}
	
	public AlgebraicDD multiply(CondProbDD condProbDD) {
		return oper(new MultiplicationOperation(), condProbDD.getComponentFunctions());
	}
	
	public AlgebraicDD multiply(ProbDD pdd) {
		return multiply(pdd.getDD());
	}
	
	public AlgebraicDD multiply(AlgebraicDD dd) {
		return oper(new MultiplicationOperation(),dd);
	}
	
	public AlgebraicDD div(AlgebraicDD dd) {
		return oper(new DivisionOperation(),dd);
	}
	
	public AlgebraicDD sumOut(ArrayList<DDVariable> vars) {
		return new AlgebraicDD(ruleCollection.eliminateVariables(vars, new AdditionOperation()));
	}
	
	protected AlgebraicDD oper(BinaryOperation oper, AlgebraicDD ddOther) {
		return new AlgebraicDD(ruleCollection.apply(oper,ddOther.ruleCollection));
	}
	
	protected AlgebraicDD oper(BinaryOperation oper, List<AlgebraicDD> ddOtherList) {
		ArrayList<ImmutableDDElement> otherCollections = new ArrayList<ImmutableDDElement>();
		for(AlgebraicDD dd: ddOtherList) {
			otherCollections.add(dd.ruleCollection);
		}
		
		return new AlgebraicDD(ruleCollection.apply(oper, otherCollections));
	}
	
	public String toString() {
		return ruleCollection.toString();
	}
}
