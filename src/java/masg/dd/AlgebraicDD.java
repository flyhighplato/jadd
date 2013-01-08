package masg.dd;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;

import masg.dd.operations.AbsDiffOperation;
import masg.dd.operations.AdditionOperation;
import masg.dd.operations.BinaryOperation;
import masg.dd.operations.ConstantAdditionOperation;
import masg.dd.operations.ConstantMultiplicationOperation;
import masg.dd.operations.DivisionOperation;
import masg.dd.operations.MaxOperation;
import masg.dd.operations.MultiplicationOperation;
import masg.dd.operations.SubtractionOperation;
import masg.dd.operations.UnaryOperation;
import masg.dd.representation.DDElement;
import masg.dd.representation.DDInfo;
import masg.dd.representation.DDLeaf;
import masg.dd.representation.builder.DDBuilder;
import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;

public class AlgebraicDD {
	protected DDElement ruleCollection;
	protected DDVariableSpace variables;
	
	public final DDElement getFunction() {
		return ruleCollection;
	}
	
	public AlgebraicDD(DDVariableSpace vars, Closure<Double> c, boolean isMeasure) {
		variables = vars;
		ruleCollection = DDBuilder.build(new DDInfo(vars,isMeasure), c).getRootNode();
	}
	
	public AlgebraicDD(DDVariableSpace vars, Closure<Double>... c) {
		variables = vars;
		ruleCollection = DDBuilder.build(new DDInfo(vars,true), c).getRootNode();
	}
	
	public AlgebraicDD(DDVariableSpace vars, double val) {
		variables = vars;
		ruleCollection = DDBuilder.build(new DDInfo(vars,true), val).getRootNode();
	}
	
	public AlgebraicDD(DDElement immutableDDElement) {
		variables = new DDVariableSpace(immutableDDElement.getVariables());
		ruleCollection = immutableDDElement;
	}
	
	public Double getValue(HashMap<DDVariable,Integer> varSpacePoint) {
		return ruleCollection.getValue(varSpacePoint);
	}
	
	public DDVariableSpace getVariables() {
		return variables;
	}
	
	public AlgebraicDD restrict(HashMap<DDVariable,Integer> varSpacePoint) {
		return new AlgebraicDD(DDBuilder.restrict(varSpacePoint, ruleCollection));
	}
	
	public AlgebraicDD absDiff(AlgebraicDD dd) {
		return oper(new AbsDiffOperation(),dd);
	}
	
	public AlgebraicDD minus(AlgebraicDD dd) {
		return oper(new SubtractionOperation(),dd);
	}
	
	public AlgebraicDD multiply(AlgebraicDD dd) {
		return oper(new MultiplicationOperation(),dd);
	}
	
	public AlgebraicDD multiply(double mult) {
		return oper(new ConstantMultiplicationOperation(mult));
	}
	
	public AlgebraicDD plus(double val) {
		return oper(new ConstantAdditionOperation(val));
	}
	
	public AlgebraicDD plus(AlgebraicDD dd) {
		return oper(new AdditionOperation(),dd);
	}
	
	public AlgebraicDD div(AlgebraicDD dd) {
		return oper(new DivisionOperation(),dd);
	}
	
	public AlgebraicDD max(AlgebraicDD dd) {
		return oper(new MaxOperation(),dd);
	}
	
	public AlgebraicDD sumOut(DDVariableSpace vars) {
		boolean needsSumOut = false;
		
		for(DDVariable var:vars.getVariables()) {
			if(ruleCollection.getVariables().contains(var)) {
				needsSumOut = true;
				break;
			}
		}
		
		if(!needsSumOut)
			return this;
		
		return new AlgebraicDD( DDBuilder.eliminate(vars, ruleCollection) );
	}
	
	protected AlgebraicDD oper(UnaryOperation oper) {
		return new AlgebraicDD(DDBuilder.build(getVariables(), ruleCollection, oper));
	}
	
	protected AlgebraicDD oper(BinaryOperation oper, AlgebraicDD ddOther) {
		DDVariableSpace vars = getVariables().union(ddOther.getVariables());
		
		ArrayList<DDElement> dDs = new ArrayList<DDElement>();
		dDs.add(ruleCollection);
		dDs.add(ddOther.ruleCollection);
		return new AlgebraicDD(DDBuilder.build(vars, dDs, oper));
	}
	
	protected AlgebraicDD oper(BinaryOperation oper, ArrayList<AlgebraicDD> ddOtherList) {
		ArrayList<DDElement> dDs = new ArrayList<DDElement>();
		dDs.add(ruleCollection);
		
		DDVariableSpace vars = getVariables();
		for(AlgebraicDD dd:ddOtherList) {
			dDs.add(dd.ruleCollection);
			vars = vars.union(dd.getVariables());
		}
		return new AlgebraicDD(DDBuilder.build(vars, dDs, oper));
	}
	
	public AlgebraicDD normalize() {
		return new AlgebraicDD(DDBuilder.normalize(ruleCollection.getVariables(),ruleCollection));
	}
	public AlgebraicDD prime() {
		return new AlgebraicDD(DDBuilder.prime(ruleCollection).getRootNode());
	}
	
	public AlgebraicDD unprime() {
		return new AlgebraicDD(DDBuilder.unprime(ruleCollection).getRootNode());
	}
	
	public Double getTotalWeight() {
		return ((DDLeaf)DDBuilder.eliminate(ruleCollection.getVariables(), ruleCollection)).getValue();
	}
	
	public String toString() {
		return ruleCollection.toString();
	}
}
