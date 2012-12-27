package masg.dd.representation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import masg.dd.representation.builder.DDBuilder;
import masg.dd.variables.DDVariable;

public class DDNode extends BaseDDElement implements DDElement {
	private DDVariable v;
	private BaseDDElement[] children;
	private HashSet<BaseDDElement> uniqChildren;
	private DDInfo info;
	
	public DDNode(DDInfo info, DDVariable v) {
		super();
		init(info, v,new BaseDDElement[v.getValueCount()]);
	}
	
	public DDNode(DDInfo info, DDVariable v, BaseDDElement[] children) {
		super();
		init(info, v,children);
	}
	
	private void init(DDInfo info, DDVariable v, BaseDDElement[] children) {
		this.v = v;
		this.children = children;
		this.uniqChildren = new HashSet<BaseDDElement>(Arrays.asList(this.children));
		this.info = info;
	}
	
	public DDVariable getVariable() {
		return v;
	}
	
	public final BaseDDElement[] getChildren() {
		return children;
	}
	
	public final BaseDDElement getChild(int i) {
		return children[i];
	}
	
	public String toString() {
		return DDBuilder.toString(this);
	}
	
	public String getKey() {
		return id + ":" + v.toString() ;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof DDNode) {
			DDNode otherNode = (DDNode) o;
			
			return otherNode.id == id;
		}
		
		return false;
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
		Double sum = 0.0d;
		
		for(BaseDDElement child:children) {
			sum+=child.getTotalWeight();;
		}
		
		return sum;
	}

	@Override
	public Double getValue(HashMap<DDVariable, Integer> path) {
		if(path.containsKey(v)) {	
			return children[path.get(v)].getValue(path);	
		}
		else if(info.isMeasure()) {
			Double sum = 0.0d;
			
			for(BaseDDElement child:uniqChildren) {
				Double temp = child.getValue(path);
				
				if(temp==null)
					return null;
				
				sum+=temp;
			}
			
			return sum;
		}
		
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
