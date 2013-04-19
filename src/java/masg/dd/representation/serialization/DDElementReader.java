package masg.dd.representation.serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import masg.dd.representation.DDElement;
import masg.dd.representation.DDInfo;
import masg.dd.representation.DDLeaf;
import masg.dd.representation.DDNode;
import masg.dd.variables.DDVariable;

public class DDElementReader {
	BufferedReader reader;
	public DDElementReader(BufferedReader reader) {
		this.reader = reader;
	}
	
	private class DDNodeScaffold {
		Long oldId = -1L;
		@SuppressWarnings("unused")
		boolean isMeasure = false;
		DDVariable v = null;
		HashMap<Integer,Long> missingChildren = new HashMap<Integer,Long>();
		DDElement[] children = null;
	}

	public DDElement read(int scope) throws IOException {
		
		HashSet<DDVariable> vars = new HashSet<DDVariable>();
		HashMap<Integer,DDVariable> varByIndex = new HashMap<Integer,DDVariable>();
		
		HashMap<Long,DDLeaf> leaves = new HashMap<Long,DDLeaf>();
		HashMap<Long,DDNode> nodes = new HashMap<Long,DDNode>();
		
		long rootId = -1L;

		DDElement rootEl = null;
		
		while(true) {
			String str = reader.readLine();

			if(str.isEmpty()) {
				break;
			}
			
			String[] params = str.split(":");
			
			int ix = Integer.parseInt(params[0]);
			String varName = params[1];
			int valCount = Integer.parseInt(params[2]);
			
			DDVariable v = new DDVariable(scope,varName,valCount);
			vars.add(v);
			varByIndex.put(ix, v);
			
		}
		
		while(true) {
			String str = reader.readLine();
			if(str.isEmpty()) {
				break;
			}
			rootId = Long.parseLong(str);
		}
		
		DDInfo info = null;
		
		while(true) {
			String str = reader.readLine();

			if(str.isEmpty()) {
				break;
			}
			
			String[] params = str.split(":");
			
			long id = Long.parseLong(params[0]);
			double val = Double.parseDouble(params[1]);
			boolean isMeasure = Integer.parseInt(params[2])==1;
			
			if(info == null) {
				info = new DDInfo(vars,isMeasure);
			}
			
			DDLeaf l = new DDLeaf(info,val);
			leaves.put(id,l);
			
			if(id==rootId) {
				rootEl = l;
			}

		}
		
		HashSet<DDNodeScaffold> scaffoldNodes = new HashSet<DDNodeScaffold>();
		
		while(true) {
			String str = reader.readLine();

			if(str==null || str.isEmpty()) {
				break;
			}
			
			String[] params = str.split(":");
			
			DDNodeScaffold n = new DDNodeScaffold();
			n.oldId = Long.parseLong(params[0]);
			n.v = varByIndex.get(Integer.parseInt(params[1]));
			n.isMeasure = Integer.parseInt(params[2])==1;
			
			String[] childIdStr = params[3].split(" ");
			n.children = new DDElement[n.v.getValueCount()];
			
			for(int i=0;i<n.children.length;++i) {
				n.missingChildren.put(i, Long.parseLong(childIdStr[i]));
			}
			
			
			scaffoldNodes.add(n);
			
		}
		
		while(!scaffoldNodes.isEmpty()) {
			HashSet<DDNodeScaffold> newScaffoldNodes = new HashSet<DDNodeScaffold>();
			
			boolean madeProgress = false;
			for(DDNodeScaffold ns:scaffoldNodes) {
				
				HashMap<Integer,Long> newMissingChildren = new HashMap<Integer,Long>();
				for(Entry<Integer,Long> e:ns.missingChildren.entrySet()) {
					
					if(leaves.containsKey(e.getValue())) {
						ns.children[e.getKey()] = leaves.get(e.getValue());
						madeProgress = true;
					}
					else if(nodes.containsKey(e.getValue())) {
						ns.children[e.getKey()] = nodes.get(e.getValue());
						madeProgress = true;
					}
					else {
						newMissingChildren.put(e.getKey(), e.getValue());
					}
				}
				
				ns.missingChildren = newMissingChildren;
				
				if(ns.missingChildren.isEmpty()) {
					DDNode n = new DDNode(info,ns.v,ns.children);
					nodes.put(ns.oldId, n);
					
					madeProgress = true;
					
					if(ns.oldId == rootId) {
						rootEl = n;
					}
				}
				else {
					newScaffoldNodes.add(ns);
				}
			}
			
			if(!madeProgress) {
				return null;
			}
			scaffoldNodes = newScaffoldNodes;
		}
		
		return rootEl;
	}
	
	
}
