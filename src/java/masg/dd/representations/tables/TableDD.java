package masg.dd.representations.tables;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import masg.dd.context.DDContext;
import masg.dd.operations.BinaryOperation;
import masg.dd.operations.UnaryOperation;
import masg.dd.representations.dag.ImmutableDDElement;
import masg.dd.representations.dag.ImmutableDDLeaf;
import masg.dd.representations.dag.ImmutableDDNode;
import masg.dd.variables.DDVariable;

public class TableDD {
	
	HashMap<Double, TableDDLeaf> leaves = new HashMap<Double, TableDDLeaf>();
	HashMap<DDVariable, ArrayList< HashMap<TableDDElement, HashSet<TableDDNode>> > >  nodes = new HashMap<DDVariable, ArrayList< HashMap<TableDDElement, HashSet<TableDDNode>> > >();
	
	TableDDElement rootNode = null;
	HashSet<DDVariable> vars;
	
	protected TableDD(ArrayList<DDVariable> vars ) {
		this.vars = new HashSet<DDVariable>(vars);
		
		for(DDVariable var:vars) {
			ArrayList< HashMap<TableDDElement, HashSet<TableDDNode>> > temp = new ArrayList< HashMap<TableDDElement, HashSet<TableDDNode>> >();
			
			for(int i=0;i<var.getValueCount();i++) {
				temp.add(new HashMap<TableDDElement,HashSet<TableDDNode>>());
			}
			
			nodes.put(var, temp);
		}
	}
	
	public ImmutableDDElement asDagDD() {
		if(rootNode instanceof TableDDNode) {
			return new ImmutableDDNode(vars, (TableDDNode)rootNode, null, false);
		}
		else if(rootNode instanceof TableDDLeaf) {
			return new ImmutableDDLeaf(vars, (TableDDLeaf)rootNode, null);
		}
		
		return null;
	}
	
	public ImmutableDDElement asDagDD(boolean isMeasure) {
		if(rootNode instanceof TableDDNode) {
			return new ImmutableDDNode(vars, (TableDDNode)rootNode, null, isMeasure);
		}
		else if(rootNode instanceof TableDDLeaf) {
			return new ImmutableDDLeaf(vars, (TableDDLeaf)rootNode, null);
		}
		
		return null;
	}
	
	public static TableDD build(ArrayList<DDVariable> vars, Closure<Double>... closures) {
		TableDD dd = new TableDD(vars);
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.vars, new ProbabilityClosuresFunction(closures));
		return dd;
	}
	
	public static TableDD build(ArrayList<DDVariable> vars, Closure<Double> c) {
		TableDD dd = new TableDD(vars);
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.vars, new ClosureFunction(c));
		return dd;
	}
	
	public static TableDD build(ArrayList<DDVariable> vars, double constVal) {
		TableDD dd = new TableDD(vars);
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.vars, new ConstantFunction(constVal));
		return dd;
	}
	
	public static TableDD build(ArrayList<DDVariable> vars, ArrayList<ImmutableDDElement> dags, BinaryOperation op) {
		TableDD dd = new TableDD(vars);
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.vars, new DagDDBinaryOperationFunction(dags,op));
		return dd;
	}
	
	public static TableDD build(ArrayList<DDVariable> vars, ImmutableDDElement dag, UnaryOperation op) {
		TableDD dd = new TableDD(vars);
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.vars, new DagDDUnaryOperationFunction(dag,op));
		return dd;
	}
	
	public static TableDD restrict(HashMap<DDVariable,Integer> restrictVarValues, ImmutableDDElement dag) {
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(dag.getVariables());
		vars.removeAll(restrictVarValues.keySet());
		
		TableDD dd = new TableDD(vars);
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.vars, new DagDDRestrictFunction(dag,restrictVarValues));
		return dd;
	}
	
	public static TableDD eliminate(ArrayList<DDVariable> elimVars, ImmutableDDElement dag) {
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(dag.getVariables());
		vars.removeAll(elimVars);
		
		TableDD dd = new TableDD(vars);
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.vars, new DagDDElimFunction(dag,elimVars));
		return dd;
	}
	
	public static TableDD prime(ImmutableDDElement dag) {
		HashMap<DDVariable,DDVariable> translation = new HashMap<DDVariable,DDVariable>();
		
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>();
		for(DDVariable var:dag.getVariables()) {
			DDVariable varPrimed = var.getPrimed();
			vars.add(varPrimed);
			translation.put(varPrimed, var);
		}
		
		TableDD dd = new TableDD(vars);
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.vars, new DagDDTranslateFunction(dag,translation));
		return dd;
		
	}
	
	public static TableDD unprime(ImmutableDDElement dag) {
		HashMap<DDVariable,DDVariable> translation = new HashMap<DDVariable,DDVariable>();
		
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>();
		for(DDVariable var:dag.getVariables()) {
			DDVariable varUnprimed = var.getUnprimed();
			vars.add(varUnprimed);
			translation.put(varUnprimed, var);
		}
		
		TableDD dd = new TableDD(vars);
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.vars, new DagDDTranslateFunction(dag,translation));
		return dd;
		
	}
	
	public TableDDElement getRootNode() {
		return rootNode;
	}
	
	protected TableDDLeaf makeLeaf(Double value) {
		TableDDLeaf l = leaves.get(value);
		
		if(l==null) {
			l = new TableDDLeaf(value);
			leaves.put(value, l);
		}
		
		return l;
	}
	
	protected TableDDNode makeNode(DDVariable var, TableDDElement[] children) {
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
	
	protected TableDDElement makeSubGraph(HashMap<DDVariable,Integer> path, List<DDVariable> varOrder, HashSet<DDVariable> vars, DDBuilderFunction fn) {
		
		if(varOrder.size()==0) {
			return makeLeaf(fn.invoke(path));
		}
		
		DDVariable currVar = varOrder.get(0);
		List<DDVariable> nextVarOrder = varOrder.subList(1, varOrder.size());
		if(vars.contains(currVar)) {
			TableDDElement[] children = new TableDDElement[currVar.getValueCount()];
			
			for(int i=0;i<currVar.getValueCount();i++) {
				path.put(currVar, i);
				children[i] = makeSubGraph(path, nextVarOrder, vars, fn );
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
			return makeSubGraph(path,nextVarOrder, vars, fn);
		}
	}
	
	
	public String toString(TableDDElement el, HashSet<Long> processed) {
		String str = "";
		
		if(processed.contains(el.id)) {
			return str;
		}
		processed.add(el.id);
		
		
		
		if(el instanceof TableDDNode) {
			TableDDNode n = (TableDDNode) el;
			
			String parentLabel = "	\"" + n.getKey() + "\"";
			
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
					childLabel = nChild.getKey();
				}
				else if (child instanceof TableDDLeaf) {
					TableDDLeaf lChild = (TableDDLeaf) child;
					childLabel = lChild.getId() + ":" + lChild.value;
				}
				
				str += parentLabel + " -> \"" + childLabel + "\" [ label = \"" + e.getValue() + "\" ];\n";
				
				if(child instanceof TableDDNode) {
					str += toString(child,processed);
				}
			}
		}
		else if (el instanceof TableDDLeaf) {
			TableDDLeaf l = (TableDDLeaf) el;
			str += "	\"" + l.getValue() + "\"\n";
		}
		
		
		return str;
	}
	
	public String toString() {
		String str = "";
		str += "digraph add {\n";
		str += "	rankdir=LR;\n";
		str += "	node [shape = circle];\n";

		str += toString(rootNode, new HashSet<Long>());
		
		str += "}\n";
		
		return str;
	}
}
