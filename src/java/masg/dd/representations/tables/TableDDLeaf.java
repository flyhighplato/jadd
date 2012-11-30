package masg.dd.representations.tables;

public class TableDDLeaf extends TableDDElement {
	Double value;
	
	public TableDDLeaf(double value) {
		super();
		this.value = value;
	}
	
	public String toString() {
		return value==null?"null":value.toString();
	}
	
	
	public boolean equals(Object o) {
		if(o instanceof TableDDLeaf) {
			TableDDLeaf otherLeaf = (TableDDLeaf) o;
			
			return Math.abs(otherLeaf.value - value)<0.001f;
		}
		
		return false;
	}
	
	public int hashCode() {
		return (int)id;
	}
}
