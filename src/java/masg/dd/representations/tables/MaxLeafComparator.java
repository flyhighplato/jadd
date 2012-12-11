package masg.dd.representations.tables;

import java.util.Comparator;

import masg.dd.representations.dag.ImmutableDDLeaf;

public class MaxLeafComparator implements Comparator<ImmutableDDLeaf>{

	@Override
	public int compare(ImmutableDDLeaf o1, ImmutableDDLeaf o2) {
		if(o1.getValue()==o2.getValue()) {
			return 0;
		}
		else if (o1.getValue()>o2.getValue()) {
			return 1;
		}
		else {
			return -1;
		}
	}
}