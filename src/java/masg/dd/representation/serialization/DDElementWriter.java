package masg.dd.representation.serialization;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import masg.dd.representation.DDElement;
import masg.dd.representation.DDLeaf;
import masg.dd.representation.DDNode;
import masg.dd.variables.DDVariable;

public class DDElementWriter {
	private DDElement rootEl;
	
	private HashSet<DDNode> nodes = new HashSet<DDNode>();
	private HashSet<DDLeaf> leaves = new HashSet<DDLeaf>();
	
	public DDElementWriter(DDElement el) {
		rootEl = el;
		
		catalogUniqueSubElements(rootEl);
	}
	
	private void catalogUniqueSubElements(DDElement el) {
		
		if(el instanceof DDNode) {
			DDNode n = (DDNode) el;
			if(nodes.contains(el)) {
				return;
			}
			
			nodes.add(n);
			
			for(DDElement childEl:n.getChildren()) {
				catalogUniqueSubElements(childEl);
			}
		}
		else if(el instanceof DDLeaf) {
			DDLeaf l = (DDLeaf) el;
			
			if(leaves.contains(el)) {
				return;
			}
			
			leaves.add(l);
		}
	}
	
	public void write(BufferedWriter w) throws IOException {
		
		HashMap<DDVariable,Integer> varNumber = new HashMap<DDVariable,Integer>();
		int ix = 0;
		for(DDVariable v: rootEl.getVariables()) {
			w.write(ix + ":" + v.getName() + ":" + v.getValueCount());
			varNumber.put(v, ix);
			++ix;
			w.newLine();
		}
		
		w.newLine();
		w.write(rootEl.getId() + "");
		
		w.newLine();
		w.newLine();
		
		for(DDLeaf l:leaves) {
			w.write(l.getId() + ":" + l.getValue() + ":" + (l.isMeasure()?"1":"0"));
			w.newLine();
		}
		w.newLine();
		
		
		for(DDNode n:nodes) {
			w.write(n.getId() + ":" + varNumber.get(n.getVariable()) + ":" + (n.isMeasure()?"1":"0") + ":");
			
			for(int i = 0; i<n.getVariable().getValueCount(); ++i) {
				w.write(n.getChild(i).getId() + "");
				
				if(i<n.getVariable().getValueCount()-1)
					w.write(" ");
			}
			w.newLine();
		}
		w.newLine();
		
	}
}
