package masg.dd.vars;

public class DDVariable {
	protected final String name;
	protected final int numValues;
	protected final int numBits;
	
	public DDVariable(String name, int numValues) {
		this.name = name;
		this.numValues = numValues;		
		numBits = (int) Math.floor(Math.log(numValues)/Math.log(2) + 1.0f);
	}
	
	public DDVariable getPrime() {
		return new DDVariable(name + "'", numValues);
	}
	
	public String getName() {
		return name;
	}
	
	public int getValueCount() {
		return numValues;
	}
	
	public int getBitCount() {
		return numBits;
	}
	
	public String toString() {
		return name;
	}
	
	public boolean equals(Object o) {
		
		if(o instanceof DDVariable) {
			DDVariable otherVar = (DDVariable) o;
			return otherVar.numValues==numValues && otherVar.name.equals(name);
		}
		return o.equals(this);
	}
	
	public int hashCode() {
		return name.hashCode() + numValues;
	}
}
