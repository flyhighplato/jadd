package masg.dd.representation.builder;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import masg.dd.context.DDContext;
import masg.dd.operations.AdditionOperation;
import masg.dd.operations.BinaryOperation;
import masg.dd.operations.ConstantMultiplicationOperation;
import masg.dd.operations.DivisionOperation;
import masg.dd.operations.UnaryOperation;
import masg.dd.representation.BaseDDElement;
import masg.dd.representation.DDElement;
import masg.dd.representation.DDInfo;
import masg.dd.representation.DDLeaf;
import masg.dd.representation.DDNode;
import masg.dd.representation.builder.buildfunctions.DDBuilderClosureFunction;
import masg.dd.representation.builder.buildfunctions.DDBuilderConstantFunction;
import masg.dd.representation.builder.buildfunctions.DDBuilderFunction;
import masg.dd.representation.builder.buildfunctions.DDBuilderProbabilityClosuresFunction;
import masg.dd.representation.builder.buildfunctions.DDBuilderRestrictFunction;
import masg.dd.representation.builder.buildfunctions.DDBuilderTranslateFunction;
import masg.dd.representation.builder.buildfunctions.MaxLeafComparator;
import masg.dd.variables.DDVariable;

public class DDBuilder {
	
	HashMap<Double, DDLeaf> leaves = new HashMap<Double, DDLeaf>();
	HashMap<DDVariable, ArrayList< HashMap<DDElement, HashSet<DDNode>> > >  nodes = new HashMap<DDVariable, ArrayList< HashMap<DDElement, HashSet<DDNode>> > >();
	
	BaseDDElement rootNode = null;
	private DDInfo info;
	
	protected DDBuilder(ArrayList<DDVariable> vars, boolean isMeasure ) {
		this.info = new DDInfo(new HashSet<DDVariable>(vars), isMeasure);
		
		for(DDVariable var:vars) {
			ArrayList< HashMap<DDElement, HashSet<DDNode>> > temp = new ArrayList< HashMap<DDElement, HashSet<DDNode>> >();
			
			for(int i=0;i<var.getValueCount();i++) {
				temp.add(new HashMap<DDElement,HashSet<DDNode>>());
			}
			
			nodes.put(var, temp);
		}
	}
	
	protected DDBuilder(DDInfo info) {
		this.info = info;
		
		for(DDVariable var:info.getVariables()) {
			ArrayList< HashMap<DDElement, HashSet<DDNode>> > temp = new ArrayList< HashMap<DDElement, HashSet<DDNode>> >();
			
			for(int i=0;i<var.getValueCount();i++) {
				temp.add(new HashMap<DDElement,HashSet<DDNode>>());
			}
			
			nodes.put(var, temp);
		}
	}
	
	public static DDBuilder build(DDInfo info, Closure<Double>... closures) {
		DDBuilder dd = new DDBuilder(info);
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.getDDInfo().getVariables(), new DDBuilderProbabilityClosuresFunction(closures));
		return dd;
	}
	
	public static DDBuilder build(DDInfo info, Closure<Double> c) {
		DDBuilder dd = new DDBuilder(info);
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.getDDInfo().getVariables(), new DDBuilderClosureFunction(c));
		return dd;
	}
	
	public static DDBuilder build(DDInfo info, double constVal) {
		DDBuilder dd = new DDBuilder(info);
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.getDDInfo().getVariables(), new DDBuilderConstantFunction(constVal));
		return dd;
	}
	
	public static DDElement build(ArrayList<DDVariable> vars, ArrayList<DDElement> dags, BinaryOperation op) {
		vars = putInCanonicalOrder(vars);
		
		ArrayList<DDElement> dagsNew = new ArrayList<DDElement>();
		for(DDElement dd:dags) {
			if(dd.isMeasure()) {
				
				HashSet<DDVariable> dagVars = new HashSet<DDVariable>(dd.getVariables());
				dagVars.removeAll(vars);
				
				if(dagVars.size()>0) {
					dd = DDBuilder.eliminate(new ArrayList<DDVariable>(dagVars), dd);
				}
			}
			
			dagsNew.add(dd);
		}
		
		dags = dagsNew;
		
		DDElement el1 = dags.get(0); 
		DDElement el2 = null;
		
		HashSet<DDVariable> varsNew = new HashSet<DDVariable>(vars);
		DDBuilder dd=null;
		for(int i=1;i<dags.size();i++) {
			el2 = dags.get(i);
			dd = new DDBuilder(new ArrayList<DDVariable>(varsNew),el1.isMeasure() && el2.isMeasure());
			dd.rootNode = dd.applyOperation(el1, el2, op, new HashMap<DDElement,HashMap<DDElement,BaseDDElement>>());
			el1 = dd.rootNode;
		}
		
		return el1;
	}
	
	public static DDElement build(ArrayList<DDVariable> vars, DDElement dag, UnaryOperation op) {
		vars = putInCanonicalOrder(vars);
		
		DDBuilder dd = new DDBuilder(vars, dag.isMeasure());
		return dd.applyOperation(dag, op, new HashMap<DDElement,DDElement>());
	}
	
	public static DDElement restrict(HashMap<DDVariable,Integer> restrictVarValues, DDElement dag) {
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(dag.getVariables());
		vars.removeAll(restrictVarValues.keySet());
		
		vars = putInCanonicalOrder(vars);
		
		DDBuilder dd = new DDBuilder(vars,dag.isMeasure());
		return dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.getDDInfo().getVariables(), new DDBuilderRestrictFunction(dag,restrictVarValues));
	}
	
	public static DDElement eliminate(ArrayList<DDVariable> elimVars, DDElement dag) {
		
		if(elimVars.size()==0) {
			return dag;
		}
		
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(dag.getVariables());
		elimVars = new ArrayList<DDVariable>(elimVars);
		
		elimVars.retainAll(vars);
		elimVars = putInCanonicalOrder(elimVars);
		
		
		
		
		DDInfo info = new DDInfo(vars, dag.isMeasure());
		DDBuilder dd = new DDBuilder(info);
		DDElement result = dd.sumSubtrees(dag, elimVars, new HashMap<DDElement,DDElement>());
		
		HashSet<DDVariable> newVars = new HashSet<DDVariable>(dag.getVariables());
		newVars.removeAll(elimVars);
		info.updateInfo(newVars, dag.isMeasure());
		
		return result;
	}
	
	public static DDElement normalize(ArrayList<DDVariable> normVars, DDElement dag) {
		normVars = new ArrayList<DDVariable>(normVars);
		normVars.retainAll(dag.getVariables());
		
		DDElement normDD = eliminate(normVars,dag);
		DDBuilder dd = new DDBuilder(new ArrayList<DDVariable>(dag.getVariables()),dag.isMeasure());
		return dd.applyOperation(dag, normDD, new DivisionOperation(), new HashMap<DDElement,HashMap<DDElement,BaseDDElement>>());
	}
	
	public static DDBuilder prime(DDElement dag) {
		HashMap<DDVariable,DDVariable> translation = new HashMap<DDVariable,DDVariable>();
		
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>();
		for(DDVariable var:dag.getVariables()) {
			DDVariable varPrimed = var.getPrimed();
			vars.add(varPrimed);
			translation.put(varPrimed, var);
		}
		
		DDInfo info = new DDInfo(vars, dag.isMeasure());
		DDBuilder dd = new DDBuilder(info);
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.getDDInfo().getVariables(), new DDBuilderTranslateFunction(dag,translation));
		
		info.updateInfo(vars, dag.isMeasure());
		return dd;
		
	}
	
	public static DDBuilder unprime(DDElement dag) {
		HashMap<DDVariable,DDVariable> translation = new HashMap<DDVariable,DDVariable>();
		
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>();
		for(DDVariable var:dag.getVariables()) {
			DDVariable varUnprimed = var.getUnprimed();
			vars.add(varUnprimed);
			translation.put(varUnprimed, var);
		}
		
		DDInfo info = new DDInfo(vars, dag.isMeasure());
		DDBuilder dd = new DDBuilder(info);
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.getDDInfo().getVariables(), new DDBuilderTranslateFunction(dag,translation));
		info.updateInfo(vars, dag.isMeasure());
		return dd;
		
	}
	
	public static DDBuilder approximate(DDElement dag, double tolerance) {
		DDBuilder dd = new DDBuilder(new ArrayList<DDVariable>(dag.getVariables()), dag.isMeasure());
		dd.rootNode = dd.approximateSubgraph(dag, new ArrayList<DDLeaf>(), tolerance, new HashMap<DDElement,BaseDDElement>());
		return dd;
	}
	
	public static DDLeaf findMaxLeaf(DDElement dag) {
		return findLeaf(dag,null,new MaxLeafComparator(), new HashSet<DDElement>());
	}
	
	public static DDLeaf findMaxLeaf(DDBuilder tdd) {
		DDLeaf res = null;
				
		for(DDLeaf l:tdd.leaves.values()) {
			if(res == null) {
				res = l;
			}
			else if(l.getValue()>res.getValue()) {
				res = l;
			}
		}
		
		return res;
	}
	
	private static ArrayList<DDVariable> putInCanonicalOrder(ArrayList<DDVariable> vars) {
		ArrayList<DDVariable> retVars = new ArrayList<DDVariable>();
		for(int i=0;i<DDContext.canonicalVariableOrdering.size();i++) {
			DDVariable currVar = DDContext.canonicalVariableOrdering.get(i);
			if(vars.contains(currVar)) {
				retVars.add(currVar);
			}
		}
		return retVars;
		
	}
	public BaseDDElement getRootNode() {
		return rootNode;
	}
	
	public DDInfo getDDInfo() {
		return info;
	}
	
	protected DDLeaf makeLeaf(Double value) {
		DDLeaf l = leaves.get(value);
		
		if(l==null) {
			l = new DDLeaf(getDDInfo(),value);
			leaves.put(value, l);
		}
		
		return l;
	}
	
	protected DDNode makeNode(DDVariable var, DDElement[] children) {
		if(nodes.get(var)==null || nodes.get(var).get(0)==null) {
			System.out.println("Wha--?");
		}
		
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
		
		if(n==null) {
			n = new DDNode(info, var, children);
			
			for(int i=0;i<var.getValueCount();++i) {
				if(nodes.get(var).get(i).get(n.getChild(i))==null) {
					nodes.get(var).get(i).put(n.getChild(i), new HashSet<DDNode>());
				}
				nodes.get(var).get(i).get(n.getChild(i)).add(n);
			}
		}

		return n;
	}
	
	protected static DDLeaf findLeaf(DDElement el, DDLeaf currFoundLeaf, Comparator<DDLeaf> compr, HashSet<DDElement> findCache) {
		if(findCache.contains(el)) {
			return currFoundLeaf;
		}
		
		findCache.add(el);
		
		if(el instanceof DDLeaf) {
			DDLeaf l = (DDLeaf) el;
			
			if(currFoundLeaf == null || compr.compare(l, currFoundLeaf)>0) {
				return l;
			}
		}
		else {
			DDNode n = (DDNode) el;
			
			for(DDElement childEl:n.getChildren()) {
				currFoundLeaf = findLeaf(childEl,currFoundLeaf,compr,findCache);
			}
		}
		
		return currFoundLeaf;
	}
	
	protected BaseDDElement approximateSubgraph(DDElement el, ArrayList<DDLeaf> newLeaves, double tolerance, HashMap<DDElement, BaseDDElement> approxCache) {
		if(approxCache.containsKey(el)) {
			return approxCache.get(el);
		}
		
		if(el instanceof DDLeaf) {
			DDLeaf l = (DDLeaf) el;
			DDLeaf lNew = makeLeaf(l.getValue());
			
			int findIx = Collections.binarySearch(newLeaves,lNew);
			
			if(findIx>-1) {
				approxCache.put(el, newLeaves.get(findIx));
				return newLeaves.get(findIx);
			}
			else {
				int ubIx = -(findIx + 1);
				if(ubIx == 0){
					newLeaves.add(lNew);
					approxCache.put(el, lNew);
					return lNew;
				}
				
				int lbIx = ubIx-1;
				
				if(ubIx == newLeaves.size()) {
					ubIx = ubIx-1;
				}
				
				double distFromUb = Math.abs(newLeaves.get(ubIx).getValue()-l.getValue());
				double distFromLb = Math.abs(newLeaves.get(lbIx).getValue()-l.getValue());
				
				if(distFromLb>tolerance && distFromUb>tolerance) {
					newLeaves.add(lNew);
					Collections.sort(newLeaves);
					approxCache.put(el, lNew);
					return lNew;
				}
				
				if(distFromUb<=tolerance){
					approxCache.put(el, newLeaves.get(ubIx));
					return newLeaves.get(ubIx);
				}
				else {
					approxCache.put(el, newLeaves.get(lbIx));
					return newLeaves.get(lbIx);
				}
			}
			
		}
		else if (el instanceof DDNode) {
			DDNode n = (DDNode) el;
			BaseDDElement[] children = new BaseDDElement[n.getVariable().getValueCount()];
			
			for(int i=0;i<n.getVariable().getValueCount();i++) {
				children[i] = approximateSubgraph(n.getChild(i),newLeaves,tolerance,approxCache);
			}
			
			boolean allEqual = true;
			
			BaseDDElement currEl = children[0];
			for(int i=1;i<n.getVariable().getValueCount() && allEqual;i++) {
				allEqual = allEqual && currEl.equals(children[i]);
				currEl = children[i];
			}
			
			if(allEqual) {
				approxCache.put(el, children[0]);
				return children[0];
			}
			
			DDNode nNew = makeNode(n.getVariable(),children);
			approxCache.put(el, nNew);
			return nNew;
		}
		
		return null;
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
	
	protected DDElement sumSubtrees(DDElement el,List<DDVariable> subtreeElimVars, HashMap<DDElement,DDElement> applyCache) {
		
		if(applyCache.containsKey(el)) {
			return applyCache.get(el);
		}
		
		int varIx = -1;
		
		if(el instanceof DDNode) {
			DDNode n = (DDNode) el;
			varIx = DDContext.getVariableIndex(n.getVariable());
		}
		
		DDVariable currVar = subtreeElimVars.get(0);
		int subtreeRootVarIx = DDContext.getVariableIndex(currVar);
		
		if(el instanceof DDLeaf || varIx>subtreeRootVarIx) {
			//ArrayList<DDVariable> varsNew = new ArrayList<DDVariable>(el.getVariables());
			
			double multiplier = 1.0d;
			for(DDVariable v:subtreeElimVars) {
				multiplier*=v.getValueCount();
			}
			
			//DDBuilder dd = new DDBuilder(info);
			DDElement result = applyOperation(el, new ConstantMultiplicationOperation(multiplier), new HashMap<DDElement,DDElement>());
			
			if(result instanceof DDLeaf) {
				result = makeLeaf(((DDLeaf) result).getValue());
			}
			else if(result instanceof DDNode) {
				if(subtreeElimVars.size()>1) {
					result = sumSubtrees(result,subtreeElimVars.subList(1, subtreeElimVars.size()), applyCache);
				}
			}
			
			applyCache.put(el, result);
			return result;
		}
		else {
			DDNode n = (DDNode) el;
			
			if(n.getVariable().equals(currVar)) {
				ArrayList<DDElement> dags = new ArrayList<DDElement>(Arrays.asList(n.getChildren()));
				
				DDElement result = dags.get(0); 
				
				DDElement el2 = null;
				DDBuilder dd=null;
				for(int i=1;i<dags.size();i++) {
					el2 = dags.get(i);
					dd = new DDBuilder(info);
					result = dd.applyOperation(result, el2, new AdditionOperation(), new HashMap<DDElement,HashMap<DDElement,BaseDDElement>>());
				}
				
				
				if(subtreeElimVars.size()>1) {
					result = sumSubtrees(result,subtreeElimVars.subList(1, subtreeElimVars.size()), applyCache);
				}
				
				if(result instanceof DDLeaf) {
					result = makeLeaf(((DDLeaf) result).getValue());
				}
				
				applyCache.put(el, result);
				return result;
			}
			else {
				DDNode nNew;
				DDElement[] children = new DDElement[el.getVariable().getValueCount()];
				
				for(int i=0;i<el.getVariable().getValueCount();i++) {
					children[i] = sumSubtrees(n.getChild(i), subtreeElimVars, applyCache);
				}
				
				nNew = makeNode(el.getVariable(),children);

				boolean allEqual = true;
				
				DDElement currEl = children[0];
				for(int i=1;i<el.getVariable().getValueCount() && allEqual;i++) {
					allEqual = allEqual && currEl.equals(children[i]);
					currEl = children[i];
				}
				
				if(allEqual) {
					applyCache.put(el, children[0]);
					return children[0];
				}
				
				applyCache.put(el, nNew);
				return nNew;
			}
			
			
		}
		
	}
	
	protected DDElement applyOperation(DDElement el, UnaryOperation op, HashMap<DDElement,DDElement> applyCache) {
		if(applyCache.containsKey(el)) {
			return applyCache.get(el);
		}
		
		DDElement result = null;
		if(el instanceof DDLeaf) {
			DDLeaf l = (DDLeaf) el;
			
			result = makeLeaf(op.invoke(l.getValue()));
		}
		else if(el instanceof DDNode) {
			DDNode n = (DDNode) el;
			
			DDElement[] children = new BaseDDElement[n.getVariable().getValueCount()];
			
			for(int i=0;i<n.getVariable().getValueCount();i++) {
				children[i] = applyOperation(n.getChild(i),op, applyCache);
			}
			
			result = makeNode(n.getVariable(), children);
			
			boolean allEqual = true;
			
			DDElement currEl = children[0];
			for(int i=1;i<n.getVariable().getValueCount() && allEqual;i++) {
				allEqual = allEqual && currEl.equals(children[i]);
				currEl = children[i];
			}
			
			if(allEqual) {
				result = children[0];
			}
		}
		
		applyCache.put(el, result);
		return result;
	}
	protected BaseDDElement applyOperation(DDElement el1, DDElement el2, BinaryOperation op, HashMap<DDElement,HashMap<DDElement,BaseDDElement>> applyCache) {
		
		if(applyCache.containsKey(el1) && applyCache.get(el1).containsKey(el2)) {
			return applyCache.get(el1).get(el2);
		}
		else {
			applyCache.put(el1, new HashMap<DDElement,BaseDDElement>());
		}
		
		if(el1 instanceof DDLeaf && el2 instanceof DDLeaf) {
			DDLeaf l1 = (DDLeaf) el1;
			DDLeaf l2 = (DDLeaf) el2;
			
			DDLeaf lNew = makeLeaf(op.invoke(l1.getValue(), l2.getValue()));
			applyCache.get(el1).put(el2, lNew);
			return lNew;
		}
		
		DDNode nNew = null;
		DDVariable currVar = null;
		BaseDDElement[] children;
		
		if(el1 instanceof DDLeaf && el2 instanceof DDNode) {
			DDNode n2 = (DDNode) el2;
			currVar = n2.getVariable();
			children = new BaseDDElement[n2.getVariable().getValueCount()];
			
			for(int i=0;i<n2.getVariable().getValueCount();i++) {
				children[i] = applyOperation(el1,n2.getChild(i),op, applyCache);
			}
		}
		else if(el1 instanceof DDNode && el2 instanceof DDLeaf) {
			DDNode n1 = (DDNode) el1;
			currVar = n1.getVariable();
			children = new BaseDDElement[n1.getVariable().getValueCount()];
			
			for(int i=0;i<n1.getVariable().getValueCount();i++) {
				children[i] = applyOperation(n1.getChild(i),el2,op, applyCache);
			}
		}
		else if(el1 instanceof DDNode && el2 instanceof DDNode) {
			DDNode n1 = (DDNode) el1;
			DDNode n2 = (DDNode) el2;
			
			int varIx1 = DDContext.getVariableIndex(n1.getVariable());
			int varIx2 = DDContext.getVariableIndex(n2.getVariable());
			
			if(varIx1==varIx2) {
				currVar = n1.getVariable();
				children = new BaseDDElement[n1.getVariable().getValueCount()];
				
				for(int i=0;i<n1.getVariable().getValueCount();i++) {
					children[i] = applyOperation(n1.getChild(i),n2.getChild(i),op, applyCache);
				}
			}
			else if(varIx1<varIx2) {
				currVar = n1.getVariable();
				children = new BaseDDElement[n1.getVariable().getValueCount()];
				
				for(int i=0;i<n1.getVariable().getValueCount();i++) {
					children[i] = applyOperation(n1.getChild(i),n2,op,applyCache);
				}
			}
			else {
				currVar = n2.getVariable();
				children = new BaseDDElement[n2.getVariable().getValueCount()];
				
				for(int i=0;i<n2.getVariable().getValueCount();i++) {
					children[i] = applyOperation(n1,n2.getChild(i),op,applyCache);
				}
			}
			
		}
		else {
			return null;
		}
		
		boolean allEqual = true;
		
		BaseDDElement currEl = children[0];
		for(int i=1;i<currVar.getValueCount() && allEqual;i++) {
			allEqual = allEqual && currEl.equals(children[i]);
			currEl = children[i];
		}
		
		if(allEqual) {
			applyCache.get(el1).put(el2, children[0]);
			return children[0];
		}
		
		
		nNew = makeNode(currVar,children);
		applyCache.get(el1).put(el2, nNew);

		return nNew;
	}
	
	
	public static String toString(DDElement el, HashSet<Long> processed) {
		String str = "";
		
		if(processed.contains(el.getId())) {
			return str;
		}
		processed.add(el.getId());
		
		
		
		if(el instanceof DDNode) {
			DDNode n = (DDNode) el;
			
			String parentLabel = "	\"" + n.getKey() + "\"";
			
			HashMap<DDElement,HashSet<Integer>> childrenPaths = new HashMap<DDElement,HashSet<Integer>>();
			for(int i=0;i<n.getChildren().length;i++) {
				
				DDElement child = n.getChild(i);

				if(childrenPaths.get(child)==null) {
					childrenPaths.put(child, new HashSet<Integer>());
				}
				childrenPaths.get(child).add(i);

			}
			
			for(Entry<DDElement, HashSet<Integer>> e:childrenPaths.entrySet()) {
				DDElement child = e.getKey();
				
				String childLabel = "";
				
				if(child instanceof DDNode) {
					DDNode nChild = (DDNode) child;
					childLabel = nChild.getKey();
				}
				else if (child instanceof DDLeaf) {
					DDLeaf lChild = (DDLeaf) child;
					childLabel = lChild.getId() + ":" + lChild.getValue();
				}
				
				str += parentLabel + " -> \"" + childLabel + "\" [ label = \"" + e.getValue() + "\" ];\n";
				
				if(child instanceof DDNode) {
					str += toString(child,processed);
				}
			}
		}
		else if (el instanceof DDLeaf) {
			DDLeaf l = (DDLeaf) el;
			str += "	\"" + l.getValue() + "\"\n";
		}
		
		
		return str;
	}
	
	public static String toString(DDElement el) {
		String str = "";
		str += "digraph add {\n";
		str += "	rankdir=LR;\n";
		str += "	node [shape = circle];\n";

		str += toString(el, new HashSet<Long>());
		
		str += "}\n";
		
		return str;
	}
	public String toString() {
		
		return toString(rootNode);
	}
}
