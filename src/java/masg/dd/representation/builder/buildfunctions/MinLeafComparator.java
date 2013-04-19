package masg.dd.representation.builder.buildfunctions;

import java.util.Comparator;

import masg.dd.representation.DDLeaf;

public class MinLeafComparator implements Comparator<DDLeaf>{

	@Override
	public int compare(DDLeaf o1, DDLeaf o2) {
		if(o1.getValue()==o2.getValue()) {
			return 0;
		}
		else if (o1.getValue()>o2.getValue()) {
			return -1;
		}
		else {
			return 1;
		}
	}
}