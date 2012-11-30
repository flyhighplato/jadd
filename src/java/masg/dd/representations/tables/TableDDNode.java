package masg.dd.representations.tables;

import masg.dd.variables.DDVariable;

public class TableDDNode extends TableDDElement {
	DDVariable v;
	TableDDElement[] children;
	
	public TableDDNode(DDVariable v) {
		super();
		init(v,new TableDDElement[v.getValueCount()]);
	}
	
	public TableDDNode(DDVariable v, TableDDElement[] children) {
		super();
		init(v,children);
	}
	
	private void init(DDVariable v, TableDDElement[] children) {
		this.v = v;
		this.children = children;
	}
	
	public String toString() {
		return getKey();
	}
	
	public String getKey() {
		return id + ":" + v.toString() ;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof TableDDNode) {
			TableDDNode otherNode = (TableDDNode) o;
			
			return otherNode.id == id;
		}
		
		return false;
	}
	
	public int hashCode() {
		return (int)id;
	}
}
