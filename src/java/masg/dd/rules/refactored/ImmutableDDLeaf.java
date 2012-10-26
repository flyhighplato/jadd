package masg.dd.rules.refactored;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import masg.dd.rules.operations.refactored.BinaryOperation;
import masg.dd.rules.operations.refactored.UnaryOperation;
import masg.dd.vars.DDVariable;
import masg.util.BitMap;

public class ImmutableDDLeaf extends BaseDDNode implements ImmutableDDElement{
	protected Double value;

	public ImmutableDDLeaf(double value) {
		this.value = value;
	}
	
	public ImmutableDDLeaf(MutableDDLeaf leaf) {
		this.value = leaf.getValue();
	}

	@Override
	public void getIsMeasure(HashMap<DDVariable, Boolean> map) {
	}

	@Override
	public HashMap<DDVariable, Boolean> getIsMeasure() {
		return new HashMap<DDVariable, Boolean>();
	}

	@Override
	public Double getTotalWeight() {
		return value;
	}

	@Override
	public Double getValue(ArrayList<DDVariable> vars, BitMap r) {
		return value;
	}
	
	public Double getValue() {
		return value;
	}

	@Override
	public ArrayList<DDVariable> getVariables() {
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>();
		return vars;
	}

	@Override
	public String toString(String spacer) {
		return spacer + ":" + value + "\n";
	}

	@Override
	public boolean isMeasure() {
		return true;
	}

	@Override
	public void apply(ArrayList<DDVariable> prefixVars, BitMap prefix, UnaryOperation oper, MutableDDElement newCollection) {
		newCollection.setValue(prefixVars, prefix, oper.invoke(value));
	}

	@Override
	public void apply(ArrayList<DDVariable> prefixVars, BitMap prefix,
			BinaryOperation oper,
			ArrayList<ImmutableDDElement> otherCollections,
			MutableDDElement newCollection) {
		
		double val = value;
		for(ImmutableDDElement otherDD:otherCollections) {
			val = oper.invoke(val, otherDD.getValue(prefixVars, prefix));
		}
		
		newCollection.setValue(prefixVars, prefix, val);
		
	}
	
	@Override
	public MutableDDElement restrict(Map<DDVariable, Integer> elimVarValues) {
		return new MutableDDLeaf(value);
	}

	@Override
	public void copy(ArrayList<DDVariable> prefixVars, BitMap prefix,
			BinaryOperation oper, MutableDDElement newCollection) {
		double val = newCollection.getValue(prefixVars, prefix);
		newCollection.setValue(prefixVars, prefix, oper.invoke(val, value));
	}

	@Override
	public void restrict(ArrayList<DDVariable> prefixVars, BitMap prefix,
			ArrayList<DDVariable> restrictVars, BitMap restrictKey,
			MutableDDElement newCollection) {
		
		newCollection.setValue(prefixVars, prefix, value);
	}

	@Override
	public MutableDDElement eliminateVariables(ArrayList<DDVariable> elimVars,
			BinaryOperation oper) {
		return new MutableDDLeaf(value);
	}
	
	@Override
	public DDVariable getVariable() {
		return null;
	}

	@Override
	public MutableDDElement apply(UnaryOperation oper) {
		return new MutableDDLeaf(oper.invoke(value));
	}

	@Override
	public MutableDDElement apply(BinaryOperation oper,
			ImmutableDDElement otherColl) {
		return new MutableDDLeaf(oper.invoke(value,otherColl.getTotalWeight()));
	}

	@Override
	public MutableDDElement apply(BinaryOperation oper,
			ArrayList<ImmutableDDElement> otherColl) {
		double val = value;
		for(ImmutableDDElement otherDD:otherColl) {
			val = oper.invoke(val, otherDD.getTotalWeight());
		}
		
		return new MutableDDLeaf(val);
	}

	public boolean equals(Object o) {
		if(o==this)
			return true;
		
		if(o instanceof ImmutableDDLeaf) {
			ImmutableDDLeaf leaf = (ImmutableDDLeaf) o;
			return leaf.getValue().equals(getValue());
		}
		else {
			return o.equals(this);
		}
	}

	
}
