package masg.dd.representation;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import masg.dd.context.DDContext;
import masg.dd.representation.builder.DDBuilder;
import masg.dd.representation.builder.buildfunctions.DDBuilderClosureFunction;
import masg.dd.representation.builder.buildfunctions.DDBuilderDDFunction;
import masg.dd.representation.builder.buildfunctions.DDBuilderFunction;
import masg.dd.variables.DDVariable;

public class DDCollection {
	protected HashMap<Double, DDLeaf> leaves = new HashMap<Double, DDLeaf>();
	protected HashSet<DDElement> roots = new HashSet<DDElement>();
	protected HashSet<DDNode> uniqNodes = new HashSet<DDNode>();
	protected HashMap<DDVariable, ArrayList< HashMap<DDElement, HashSet<DDNode>> > >  nodes = new HashMap<DDVariable, ArrayList< HashMap<DDElement, HashSet<DDNode>> > >();
	protected DDInfo info;
	
	public DDCollection(DDInfo info) {
		this.info = info;
		init();
	}
	
	public DDCollection(DDElement el) {
		this.info = new DDInfo(el.getVariables(),el.isMeasure());
		init();
		merge(el);
	}

	protected void init() {
		for(DDVariable var:info.getVariables()) {
			ArrayList< HashMap<DDElement, HashSet<DDNode>> > temp = new ArrayList< HashMap<DDElement, HashSet<DDNode>> >();
			
			for(int i=0;i<var.getValueCount();i++) {
				temp.add(new HashMap<DDElement,HashSet<DDNode>>());
			}
			
			nodes.put(var, temp);
		}
	}
	
	public DDElement merge(Closure<Double> c) {
		DDElement root = makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, info.getVariables(), new DDBuilderClosureFunction(c));
		roots.add(root);
		return root;
	}
	
	public DDElement merge(DDElement el) {
		DDElement root = makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, info.getVariables(), new DDBuilderDDFunction(el));
		roots.add(root);
		return root;
	}
	
	public HashSet<DDNode> getNodes() {
		return new HashSet<DDNode>(uniqNodes);
	}
	
	public HashSet<DDLeaf> getLeaves() {
		return new HashSet<DDLeaf>(leaves.values());
	}
	
	public HashSet<DDElement> getRoots() {
		return new HashSet<DDElement>(roots);
	}
	
	protected BaseDDElement makeSubGraph(HashMap<DDVariable,Integer> path, List<DDVariable> varOrder, HashSet<DDVariable> vars, DDBuilderFunction fn) {
		
		if(varOrder.size()==0) {
			return makeLeaf(fn.invoke(path));
		}
		
		DDVariable currVar = varOrder.get(0);
		List<DDVariable> nextVarOrder = varOrder.subList(1, varOrder.size());
		if(vars.contains(currVar)) {
			BaseDDElement[] children = new BaseDDElement[currVar.getValueCount()];
			
			for(int i=0;i<currVar.getValueCount();i++) {
				path.put(currVar, i);
				children[i] = makeSubGraph(path, nextVarOrder, vars, fn );
			}
			
			boolean allEqual = true;
			
			BaseDDElement currEl = children[0];
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
			return makeSubGraph(path,nextVarOrder, vars, fn);
		}
	}
	
	public DDInfo getDDInfo() {
		return info;
	}
	
	int round = 1000000;
	
	protected DDLeaf findLeaf(Double value) {
		
		value = (double)Math.round(value * round) / round;
		
		DDLeaf l = leaves.get(value);
		
		return l;
	}

	protected DDLeaf makeLeaf(DDLeaf likeThisLeaf) {
		DDLeaf l = findLeaf(likeThisLeaf.getValue());
		
		if(l==null) {
			double value = (double)Math.round(likeThisLeaf.getValue() * round) / round;
			
			l = new DDLeaf(getDDInfo(),value);
			leaves.put(value, l);
		}
		
		return l;
	}
	
	protected DDNode findNode(DDVariable var, DDElement[] children) {
		HashSet<DDNode> possDupes = nodes.get(var).get(0).get(children[0]);
		
		for(int i=1;i<var.getValueCount() && possDupes!=null && possDupes.size()>0;++i) {
			HashSet<DDNode> temp = nodes.get(var).get(i).get(children[i]);
			if(temp==null) {
				possDupes = null;
				break;
			}
			possDupes.retainAll(temp);
		}
		
		
		DDNode n = null;
		
		if(possDupes!=null) {
			for(DDNode possParent:possDupes) {
				
				n = possParent;
				
				for(int i=0;i<var.getValueCount();++i) {
					if(!possParent.getChild(i).equals(children[i])) {
						n = null;
						break;
					}
				}
				
				
			}
		}
		
		return n;
	}
	protected DDNode makeNode(DDVariable var, DDElement[] children) {
		DDNode n = findNode(var,children);
		
		if(n==null) {
			n = new DDNode(info, var, children);
			
			for(int i=0;i<var.getValueCount();++i) {
				if(nodes.get(var).get(i).get(n.getChild(i))==null) {
					nodes.get(var).get(i).put(n.getChild(i), new HashSet<DDNode>());
				}
				nodes.get(var).get(i).get(n.getChild(i)).add(n);
			}
			
			uniqNodes.add(n);
		}

		return n;
	}
	
	public String toString() {
		String str = "";
		str += "digraph add {\n";
		str += "	rankdir=LR;\n";
		str += "	node [shape = circle];\n";

		HashSet<Long> processed =  new HashSet<Long>();
		
		for(DDElement el:roots) {
			str += DDBuilder.toString(el, processed,"");
		}
		
		str += "}\n";
		
		return str;
	}
}
