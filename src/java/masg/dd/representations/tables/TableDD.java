package masg.dd.representations.tables;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import masg.dd.context.DDContext;
import masg.dd.variables.DDVariable;

public class TableDD {
	
	HashMap<Double, TableDDLeaf> leaves = new HashMap<Double, TableDDLeaf>();
	HashMap<DDVariable, ArrayList< HashMap<TableDDElement, HashSet<TableDDNode>> > >  nodes = new HashMap<DDVariable, ArrayList< HashMap<TableDDElement, HashSet<TableDDNode>> > >();
	
	TableDDElement topNode = null;
	
	public TableDD(ArrayList<DDVariable> vars, Closure<Double> c) {
		
		for(DDVariable var:vars) {
			ArrayList< HashMap<TableDDElement, HashSet<TableDDNode>> > temp = new ArrayList< HashMap<TableDDElement, HashSet<TableDDNode>> >();
			
			for(int i=0;i<var.getValueCount();i++) {
				temp.add(new HashMap<TableDDElement,HashSet<TableDDNode>>());
			}
			
			nodes.put(var, temp);
		}
		
		topNode = makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, new HashSet<DDVariable>(vars),c);
	}
	
	public TableDDLeaf makeLeaf(Double value) {
		TableDDLeaf l = leaves.get(value);
		
		if(l==null) {
			l = new TableDDLeaf(value);
			leaves.put(value, l);
		}
		
		return l;
	}
	
	public TableDDNode makeNode(DDVariable var, TableDDElement[] children) {
		HashSet<TableDDNode> possDupes = nodes.get(var).get(0).get(children[0]);
		
		for(int i=1;i<var.getValueCount() && possDupes!=null && possDupes.size()>0;++i) {
			HashSet<TableDDNode> temp = nodes.get(var).get(i).get(children[i]);
			if(temp==null) {
				possDupes = null;
				break;
			}
			possDupes.retainAll(temp);
		}
		
		
		TableDDNode n = null;
		
		if(possDupes!=null) {
			for(TableDDNode possParent:possDupes) {
				
				n = possParent;
				
				for(int i=0;i<var.getValueCount();++i) {
					if(!possParent.children[i].equals(children[i])) {
						n = null;
						break;
					}
				}
				
				
			}
		}
		
		if(n==null) {
			n = new TableDDNode(var, children);
			
			for(int i=0;i<var.getValueCount();++i) {
				if(nodes.get(var).get(i).get(n.children[i])==null) {
					nodes.get(var).get(i).put(n.children[i], new HashSet<TableDDNode>());
				}
				nodes.get(var).get(i).get(n.children[i]).add(n);
			}
		}
		
		
		
		return n;
	}
	
	private TableDDElement makeSubGraph(HashMap<DDVariable,Integer> path, List<DDVariable> varOrder, HashSet<DDVariable> vars, Closure<Double> c) {
		
		if(varOrder.size()==0) {
			HashMap<String,Integer> args = new HashMap<String,Integer>();
			for(Entry<DDVariable,Integer> e:path.entrySet()) {
				args.put(e.getKey().getName(), e.getValue());
			}
			double result = c.call(args);
			return makeLeaf(result);
		}
		
		DDVariable currVar = varOrder.get(0);
		List<DDVariable> nextVarOrder = varOrder.subList(1, varOrder.size());
		if(vars.contains(currVar)) {
			TableDDElement[] children = new TableDDElement[currVar.getValueCount()];
			
			for(int i=0;i<currVar.getValueCount();i++) {
				path.put(currVar, i);
				children[i] = makeSubGraph(path, nextVarOrder, vars, c );
			}
			
			boolean allEqual = true;
			
			TableDDElement currEl = children[0];
			for(int i=1;i<currVar.getValueCount() && allEqual;i++) {
				allEqual = allEqual && currEl.equals(children[i]);
				currEl = children[i];
			}
			
			if(allEqual) {
				return children[0];
			}
			
			return makeNode(currVar, children);
		}
		else {
			return makeSubGraph(path,nextVarOrder, vars, c);
		}
	}
	
	
	public String toString(TableDDElement el, HashSet<Long> processed) {
		String str = "";
		
		if(el instanceof TableDDNode) {
			TableDDNode n = (TableDDNode) el;
			if(processed.contains(n.id)) {
				return str;
			}
			processed.add(n.id);
			
			String parentLabel = "	\"" + n.id + ":" + n.getKey() + "\"";
			
			HashMap<TableDDElement,HashSet<Integer>> childrenPaths = new HashMap<TableDDElement,HashSet<Integer>>();
			for(int i=0;i<n.children.length;i++) {
				
				TableDDElement child = n.children[i];

				if(childrenPaths.get(child)==null) {
					childrenPaths.put(child, new HashSet<Integer>());
				}
				childrenPaths.get(child).add(i);

			}
			
			for(Entry<TableDDElement, HashSet<Integer>> e:childrenPaths.entrySet()) {
				TableDDElement child = e.getKey();
				
				String childLabel = "";
				
				if(child instanceof TableDDNode) {
					TableDDNode nChild = (TableDDNode) child;
					childLabel = nChild.getId() + ":" + nChild.getKey();
				}
				else if (child instanceof TableDDLeaf) {
					TableDDLeaf lChild = (TableDDLeaf) child;
					childLabel = lChild.getId() + ":" + lChild.value;
				}
				
				str += parentLabel + " -> \"" + childLabel + "\" [ label = \"" + e.getValue() + "\" ];\n";
				str += toString(child,processed);
				
			}
		}
		
		
		return str;
	}
	
	public String toString() {
		String str = "";
		str += "digraph add {\n";
		str += "	rankdir=LR;\n";
		str += "	node [shape = circle];\n";

		str += toString(topNode, new HashSet<Long>());
		
		str += "}\n";
		
		return str;
	}
}
