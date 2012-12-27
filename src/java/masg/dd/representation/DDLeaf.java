package masg.dd.representation;

import java.util.HashMap;
import java.util.HashSet;

import masg.dd.representation.builder.DDBuilder;
import masg.dd.variables.DDVariable;

public class DDLeaf extends BaseDDElement implements Comparable<DDLeaf> {
	private Double value;
	private DDInfo info;
	
	public DDLeaf(DDInfo info, double value) {
		super();
		this.value = value;
		this.info = info;
	}
	
	public String toString() {
		return DDBuilder.toString(this);
	}
	
	public Double getValue() {
		return value;
	}
	
	
	public boolean equals(Object o) {
		if(o instanceof DDLeaf) {
			DDLeaf otherLeaf = (DDLeaf) o;
			
			return Math.abs(otherLeaf.value - value)<Double.MIN_VALUE;
		}
		
		return false;
	}
	
	public int compareTo(DDLeaf l) {
		return Double.compare(l.value, value);
	}
	
	public int hashCode() {
		return (int)id;
	}

	@Override
	public boolean isMeasure() {
		return info.isMeasure();
	}

	@Override
	public Double getTotalWeight() {
		return value;
	}

	@Override
	public Double getValue(HashMap<DDVariable, Integer> path) {
		return value;
	}

	@Override
	public DDVariable getVariable() {
		return null;
	}

	@Override
	public HashSet<DDVariable> getVariables() {
		return info.getVariables();
	}

	@Override
	public String toString(String spacer) {
		return toString();
	}
}
