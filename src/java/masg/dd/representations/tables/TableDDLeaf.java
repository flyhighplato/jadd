package masg.dd.representations.tables;

public class TableDDLeaf extends TableDDElement implements Comparable<TableDDLeaf> {
	Double value;
	
	public TableDDLeaf(double value) {
		super();
		this.value = value;
	}
	
	public String toString() {
		return value==null?"null":value.toString();
	}
	
	public Double getValue() {
		return value;
	}
	
	
	public boolean equals(Object o) {
		if(o instanceof TableDDLeaf) {
			TableDDLeaf otherLeaf = (TableDDLeaf) o;
			
			return Math.abs(otherLeaf.value - value)<Double.MIN_VALUE;
		}
		
		return false;
	}
	
	public int compareTo(TableDDLeaf l) {
		return Double.compare(l.value, value);
	}
	
	public int hashCode() {
		return (int)id;
	}
}
