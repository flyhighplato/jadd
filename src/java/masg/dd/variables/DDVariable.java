package masg.dd.variables;

public class DDVariable {
	protected final String name;
	protected final int numValues;
	protected final int numBits;
	
	protected final int scope;
	
	public DDVariable(String name, int numValues) {
		this.scope = 0;
		this.name = name;
		this.numValues = numValues;		
		numBits = (int) Math.floor(Math.log(numValues)/Math.log(2) + 1.0f);
	}
	
	public DDVariable(int scope, String name, int numValues) {
		this.scope = scope;
		this.name = name;
		this.numValues = numValues;		
		numBits = (int) Math.floor(Math.log(numValues)/Math.log(2) + 1.0f);
	}
	
	public DDVariable getPrimed() {
		if(!isPrime())
			return new DDVariable(scope, name + "'", numValues);
		else
			return this;
	}
	
	public DDVariable getUnprimed() {
		if(isPrime())
			return new DDVariable(scope, name.substring(0, name.length()-1), numValues);
		else
			return this;
	}
	
	public boolean isPrime() {
		return name.endsWith("'");	
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
	
	public int getScope() {
		return scope;
	}
	
	public String toString() {
		return "<" + scope + ">" + name;
	}
	
	public boolean equals(Object o) {
		
		if(o instanceof DDVariable) {
			DDVariable otherVar = (DDVariable) o;
			return otherVar.scope==scope && otherVar.numValues==numValues && otherVar.name.hashCode() == name.hashCode() && otherVar.name.equals(name);
		}
		return false;
	}
	
	public int hashCode() {
		return name.hashCode() + numValues + scope;
	}
}
