package masg.dd;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import masg.dd.operations.AbsDiffOperation;
import masg.dd.operations.AdditionOperation;
import masg.dd.operations.BinaryOperation;
import masg.dd.operations.ConstantAdditionOperation;
import masg.dd.operations.ConstantMultiplicationOperation;
import masg.dd.operations.DivisionOperation;
import masg.dd.operations.IsEqualOperation;
import masg.dd.operations.MaxOperation;
import masg.dd.operations.MultiplicationOperation;
import masg.dd.operations.SubtractionOperation;
import masg.dd.operations.UnaryOperation;
import masg.dd.representations.dag.ImmutableDDElement;
import masg.dd.representations.tables.TableDD;
import masg.dd.variables.DDVariable;

public class AlgebraicDD {
	protected ImmutableDDElement ruleCollection;
	protected ArrayList<DDVariable> variables;
	
	public final ImmutableDDElement getFunction() {
		return ruleCollection;
	}
	
	public AlgebraicDD(ArrayList<DDVariable> vars, Closure<Double> c, boolean isMeasure) {
		variables = vars;
		ruleCollection = TableDD.build(vars, c).asDagDD(isMeasure);
	}
	
	public AlgebraicDD(ArrayList<DDVariable> vars, Closure<Double>... c) {
		variables = vars;
		ruleCollection = TableDD.build(vars, c).asDagDD(true);
	}
	
	public AlgebraicDD(ArrayList<DDVariable> vars, double val) {
		variables = vars;
		ruleCollection = TableDD.build(vars, val).asDagDD(true);
	}
	
	public AlgebraicDD(ImmutableDDElement immutableDDElement) {
		variables = new ArrayList<DDVariable>(immutableDDElement.getVariables());
		ruleCollection = immutableDDElement;
	}
	
	public Double getValue(HashMap<DDVariable,Integer> varSpacePoint) {
		return ruleCollection.getValue(varSpacePoint);
	}
	
	public ArrayList<DDVariable> getVariables() {
		return variables;
	}
	
	public AlgebraicDD restrict(HashMap<DDVariable,Integer> varSpacePoint) {
		return new AlgebraicDD(TableDD.restrict(varSpacePoint, ruleCollection).asDagDD(ruleCollection.isMeasure()));
	}
	
	public AlgebraicDD absDiff(AlgebraicDD dd) {
		return oper(new AbsDiffOperation(),dd);
	}
	
	public AlgebraicDD minus(AlgebraicDD dd) {
		return oper(new SubtractionOperation(),dd);
	}
	
	public AlgebraicDD multiply(CondProbDD condProbDD) {
		
		HashSet<DDVariable> thisVars = new HashSet<DDVariable>(variables);
		ArrayList<AlgebraicDD> pertinentFns = new ArrayList<AlgebraicDD>();
		for(AlgebraicDD dd: condProbDD.getComponentFunctions()) {
			for(DDVariable v:dd.getVariables()) {
				if(thisVars.contains(v)) {
					pertinentFns.add(dd);
					break;
				}
			}
		}
		
		return oper(new MultiplicationOperation(), pertinentFns);
	}
	
	public AlgebraicDD multiply(ProbDD pdd) {
		return multiply(pdd.getDD());
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
	
	public AlgebraicDD div(ProbDD pdd) {
		return div(pdd.getDD());
	}
	
	public AlgebraicDD div(CondProbDD condProbDD) {
		
		HashSet<DDVariable> thisVars = new HashSet<DDVariable>(variables);
		ArrayList<AlgebraicDD> pertinentFns = new ArrayList<AlgebraicDD>();
		for(AlgebraicDD dd: condProbDD.getComponentFunctions()) {
			for(DDVariable v:dd.getVariables()) {
				if(thisVars.contains(v)) {
					pertinentFns.add(dd);
					break;
				}
			}
		}
		
		return oper(new DivisionOperation(), pertinentFns);
	}

	public AlgebraicDD div(AlgebraicDD dd) {
		return oper(new DivisionOperation(),dd);
	}
	
	public AlgebraicDD equalAtAnyPoint(AlgebraicDD dd) {
		return oper(new IsEqualOperation(),dd);
	}
	
	public AlgebraicDD max(AlgebraicDD dd) {
		return oper(new MaxOperation(),dd);
	}
	
	public AlgebraicDD sumOut(ArrayList<DDVariable> vars) {
		boolean needsSumOut = false;
		
		for(DDVariable var:vars) {
			if(ruleCollection.getVariables().contains(var)) {
				needsSumOut = true;
				break;
			}
		}
		
		if(!needsSumOut)
			return this;
		
		return new AlgebraicDD( TableDD.eliminate(vars, ruleCollection) );
	}
	
	protected AlgebraicDD oper(UnaryOperation oper) {
		return new AlgebraicDD(TableDD.build(getVariables(), ruleCollection, oper).asDagDD(ruleCollection.isMeasure()));
	}
	
	protected AlgebraicDD oper(BinaryOperation oper, AlgebraicDD ddOther) {
		ArrayList<ImmutableDDElement> dDs = new ArrayList<ImmutableDDElement>();
		dDs.add(ruleCollection);
		dDs.add(ddOther.ruleCollection);
		return new AlgebraicDD(TableDD.build(getVariables(), dDs, oper));
	}
	
	
	
	protected AlgebraicDD oper(BinaryOperation oper, List<AlgebraicDD> ddOtherList) {
		ArrayList<ImmutableDDElement> dDs = new ArrayList<ImmutableDDElement>();
		dDs.add(ruleCollection);
		boolean isMeasure = true;
		for(AlgebraicDD dd: ddOtherList) {
			dDs.add(dd.ruleCollection);
			isMeasure = isMeasure && dd.ruleCollection.isMeasure();
		}
		
		return new AlgebraicDD(TableDD.build(getVariables(), dDs, oper));
	}
	
	public AlgebraicDD prime() {
		return new AlgebraicDD(TableDD.prime(ruleCollection).asDagDD(ruleCollection.isMeasure()));
	}
	
	public AlgebraicDD unprime() {
		return new AlgebraicDD(TableDD.unprime(ruleCollection).asDagDD(ruleCollection.isMeasure()));
	}
	
	public Double getTotalWeight() {
		return ruleCollection.getTotalWeight();
	}
	
	public String toString() {
		return ruleCollection.toString();
	}
}
