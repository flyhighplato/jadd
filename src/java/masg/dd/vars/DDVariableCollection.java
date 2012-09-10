package masg.dd.vars;

import java.util.ArrayList;
import java.util.Collection;

public class DDVariableCollection extends ArrayList<DDVariable> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DDVariableCollection() {
		super();
	}
	public DDVariableCollection(Collection<DDVariable> lst) {
		super(lst);
	}
	public boolean add(String name, int numValues) {
		return add(new DDVariable(name,numValues));
	}

}
