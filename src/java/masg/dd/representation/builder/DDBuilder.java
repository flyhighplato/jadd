package masg.dd.representation.builder;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import masg.dd.context.DDContext;
import masg.dd.operations.AdditionOperation;
import masg.dd.operations.BinaryOperation;
import masg.dd.operations.ConstantMultiplicationOperation;
import masg.dd.operations.DivisionOperation;
import masg.dd.operations.MultiplicationOperation;
import masg.dd.operations.UnaryOperation;
import masg.dd.representation.BaseDDElement;
import masg.dd.representation.DDElement;
import masg.dd.representation.DDInfo;
import masg.dd.representation.DDLeaf;
import masg.dd.representation.DDNode;
import masg.dd.representation.builder.buildfunctions.DDBuilderClosureFunction;
import masg.dd.representation.builder.buildfunctions.DDBuilderConstantFunction;
import masg.dd.representation.builder.buildfunctions.DDBuilderDiractDeltaFunction;
import masg.dd.representation.builder.buildfunctions.DDBuilderFunction;
import masg.dd.representation.builder.buildfunctions.DDBuilderProbabilityClosuresFunction;
import masg.dd.representation.builder.buildfunctions.DDBuilderRestrictFunction;
import masg.dd.representation.builder.buildfunctions.DDBuilderTranslateFunction;
import masg.dd.representation.builder.buildfunctions.MaxLeafComparator;
import masg.dd.representation.builder.buildfunctions.MinLeafComparator;
import masg.dd.variables.DDVariable;

public class DDBuilder {
	
	ArrayList<Double> sortedLeaves = new ArrayList<Double>();
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
	
	public static DDBuilder build(DDInfo info, int defaultScopeId, Closure<Double>... closures) {
		DDBuilder dd = new DDBuilder(info);
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.getCanonicalVariableOrdering(), dd.getDDInfo().getVariables(), new DDBuilderProbabilityClosuresFunction(defaultScopeId, closures));
		return dd;
	}
	
	public static DDBuilder build(DDInfo info,  int defaultScopeId, Closure<Double> c) {
		DDBuilder dd = new DDBuilder(info);
		
		ArrayList<DDVariable> varOrder = putInCanonicalOrder(new ArrayList<DDVariable>(dd.getDDInfo().getVariables()));

		
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), varOrder, dd.getDDInfo().getVariables(), new DDBuilderClosureFunction(defaultScopeId, c));
		return dd;
	}
	
	public static DDBuilder build(DDInfo info, int defaultScopeId,  HashMap<DDVariable,Integer> singlePt) {
		DDBuilder dd = new DDBuilder(info);
		
		ArrayList<DDVariable> varOrder = putInCanonicalOrder(new ArrayList<DDVariable>(dd.getDDInfo().getVariables()));

		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), varOrder, dd.getDDInfo().getVariables(), new DDBuilderDiractDeltaFunction(defaultScopeId, singlePt));
		return dd;
	}
	
	public static DDLeaf build(DDInfo info, double constVal) {
		//DDBuilder dd = new DDBuilder(info);
		
		
		//dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.getCanonicalVariableOrdering(), dd.getDDInfo().getVariables(), new DDBuilderConstantFunction(constVal));
		return new DDLeaf(info,constVal);
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
	
	public static DDLeaf build(ArrayList<DDVariable> vars, ArrayList<DDElement> dags, BinaryOperation mapOp, BinaryOperation collectOp) {
		vars = putInCanonicalOrder(vars);
		
		boolean allMeasure = true;
		
		for(DDElement dag:dags) {
			allMeasure = allMeasure && dag.isMeasure();
		}
		
		List<DDElement> dagsNew = new ArrayList<DDElement>(dags);

		DDBuilder dd = new DDBuilder(new ArrayList<DDVariable>(),allMeasure);
		DDLeaf l = dd.mapCollectLeafOperation( putInCanonicalOrder(vars), dagsNew , mapOp, collectOp, new HashMap<HashSet<DDElement>,DDLeaf>());
		return l;
		
	}
	
	public static DDElement build(ArrayList<DDVariable> vars, ArrayList<DDVariable> collectVars, ArrayList<DDElement> dags, BinaryOperation mapOp, BinaryOperation collectOp) {
		boolean allMeasure = true;
		
		for(DDElement dag:dags) {
			allMeasure = allMeasure && dag.isMeasure();
		}
		
		List<DDElement> dagsNew = new ArrayList<DDElement>(dags);

		
		DDInfo info = new DDInfo(vars,allMeasure);
		DDBuilder dd = new DDBuilder(info);
		DDElement resEl = dd.mapCollectOperation( putInCanonicalOrder(vars), putInCanonicalOrder(collectVars), dagsNew , mapOp, collectOp, new HashMap<HashSet<DDElement>,DDElement>());
		
		ArrayList<DDVariable> newVars = new ArrayList<DDVariable>(vars);
		if(collectVars!=null) {
			newVars.removeAll(collectVars);
		}

		info.updateInfo(newVars, allMeasure);
		return resEl;
		
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
		return dd.makeSubGraph(new HashMap<DDVariable,Integer>(), putInCanonicalOrder(new ArrayList<DDVariable>(dd.getDDInfo().getVariables())), dd.getDDInfo().getVariables(), new DDBuilderRestrictFunction(dag,restrictVarValues));
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
		
		ArrayList<DDVariable> varOrder = new ArrayList<DDVariable>(dd.getDDInfo().getVariables());
		varOrder.addAll(dag.getVariables());
		varOrder = putInCanonicalOrder(varOrder);
		
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), varOrder, dd.getDDInfo().getVariables(), new DDBuilderTranslateFunction(dag,translation));
		
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
		
		ArrayList<DDVariable> varOrder = new ArrayList<DDVariable>(dd.getDDInfo().getVariables());
		varOrder.addAll(dag.getVariables());
		varOrder = putInCanonicalOrder(varOrder);
		
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), varOrder, dd.getDDInfo().getVariables(), new DDBuilderTranslateFunction(dag,translation));
		info.updateInfo(vars, dag.isMeasure());
		return dd;
		
	}
	
	public static DDBuilder switchScope(DDElement dag, int newScopeId) {
		HashMap<DDVariable,DDVariable> translation = new HashMap<DDVariable,DDVariable>();
		
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>();
		for(DDVariable var:dag.getVariables()) {
			DDVariable newScopeVar = new DDVariable(newScopeId,var.getName(),var.getValueCount());
			vars.add(newScopeVar);
			translation.put(newScopeVar, var);
		}
		
		DDInfo info = new DDInfo(vars, dag.isMeasure());
		DDBuilder dd = new DDBuilder(info);
		
		ArrayList<DDVariable> varOrder = new ArrayList<DDVariable>(dd.getDDInfo().getVariables());
		varOrder.addAll(dag.getVariables());
		varOrder = putInCanonicalOrder(varOrder);
		
		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), varOrder, dd.getDDInfo().getVariables(), new DDBuilderTranslateFunction(dag,translation));
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
	
	public static DDLeaf findMinLeaf(DDElement dag) {
		return findLeaf(dag,null,new MinLeafComparator(), new HashSet<DDElement>());
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
	
	private static ArrayList<DDVariable> putInCanonicalOrder(List<DDVariable> vars) {
		
		if(vars == null)
			return null;
		
		ArrayList<DDVariable> retVars = new ArrayList<DDVariable>();
		for(int i=0;i<DDContext.getCanonicalVariableOrdering().size();i++) {
			DDVariable currVar = DDContext.getCanonicalVariableOrdering().get(i);
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
	
	static double tolerance = 0.00001d;
	int maxLeafThresh = 1;
	protected DDLeaf makeLeaf(Double value) {
		
		DDLeaf l = leaves.get(value);
		
		if(l==null) {
			
			if(sortedLeaves.size()>maxLeafThresh) {
				int findIx = Collections.binarySearch(sortedLeaves, value);
				if(findIx<=-1) {
					int ubIx = -(findIx + 1);
					if(ubIx == 0) {
						sortedLeaves.add(0, value);
						l = new DDLeaf(getDDInfo(),value);
						leaves.put(value, l);
						return l;
					}
					
					int lbIx = ubIx-1;
					
					if(ubIx == sortedLeaves.size()) {
						ubIx = lbIx;
					}
					
					double distFromUb = Math.abs(sortedLeaves.get(ubIx)-value);
					double distFromLb = Math.abs(sortedLeaves.get(lbIx)-value);
					
					if(distFromLb > tolerance && distFromUb > tolerance) {
						sortedLeaves.add(ubIx, value);
						l = new DDLeaf(getDDInfo(),value);
						leaves.put(value, l);
						return l;
					}
					
					if(distFromUb<=tolerance){
						l = leaves.get(sortedLeaves.get(ubIx));
						return l;
					}
					else {
						l = leaves.get(sortedLeaves.get(lbIx));
						
						return l;
					}
					
					
				}
				else {
					l = new DDLeaf(getDDInfo(),value);
					leaves.put(value, l);
					return l;
				}
			}
			else {
				l = new DDLeaf(getDDInfo(),value);
				
				sortedLeaves.add(value);
				leaves.put(value, l);
				if(sortedLeaves.size()>=maxLeafThresh) {
					Collections.sort(sortedLeaves);
				}
				
				return l;
				
			}
			
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
					Collections.sort(newLeaves);
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
			
			// Shortcut hack
			if(op instanceof MultiplicationOperation) {
				DDLeaf l1 = (DDLeaf) el1;
				if(l1.getValue() == 0.0d) {
					DDLeaf lNew = makeLeaf(l1.getValue());
					applyCache.get(el1).put(el2, lNew);
					return lNew;
				}
			}
			
			for(int i=0;i<n2.getVariable().getValueCount();i++) {
				children[i] = applyOperation(el1,n2.getChild(i),op, applyCache);
			}
		}
		else if(el1 instanceof DDNode && el2 instanceof DDLeaf) {
			DDNode n1 = (DDNode) el1;
			currVar = n1.getVariable();
			children = new BaseDDElement[n1.getVariable().getValueCount()];
			
			// Shortcut hack
			if(op instanceof MultiplicationOperation) {
				DDLeaf l2 = (DDLeaf) el2;
				if(l2.getValue() == 0.0d) {
					DDLeaf lNew = makeLeaf(l2.getValue());
					applyCache.get(el1).put(el2, lNew);
					return lNew;
				}
			}
			
			for(int i=0;i<n1.getVariable().getValueCount();i++) {
				children[i] = applyOperation(n1.getChild(i),el2,op, applyCache);
			}
		}
		else if(el1 instanceof DDNode && el2 instanceof DDNode) {
			DDNode n1 = (DDNode) el1;
			DDNode n2 = (DDNode) el2;
			
			int varIx1 = DDContext.getVariableIndex(n1.getVariable());
			int varIx2 = DDContext.getVariableIndex(n2.getVariable());
			
			if(varIx1 == varIx2) {
				currVar = n1.getVariable();
				children = new BaseDDElement[n1.getVariable().getValueCount()];
				
				for(int i=0;i<n1.getVariable().getValueCount();i++) {
					children[i] = applyOperation(n1.getChild(i),n2.getChild(i),op, applyCache);
				}
			}
			else if(varIx1 < varIx2) {
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
	
	public static HashSet<HashMap<DDVariable,Integer>> getAllUniqueNonZeroPaths(ArrayList<DDVariable> pathVars, List<DDElement> elems) {
		HashSet<DDVariable> allVars = new HashSet<DDVariable>();
		for(DDElement el:elems) {
			allVars.addAll(el.getVariables());
		}
		
		pathVars = new ArrayList<DDVariable>(pathVars);
		pathVars.retainAll(allVars);
		
		return getAllUniquePaths(putInCanonicalOrder(new ArrayList<DDVariable>(allVars)),putInCanonicalOrder(pathVars),elems,new HashMap<DDVariable,Integer>());
	}
	protected static HashSet<HashMap<DDVariable,Integer>> getAllUniquePaths(List<DDVariable> allVars, List<DDVariable> pathVars, List<DDElement> elems, HashMap<DDVariable,Integer> prevPath) {
		HashSet<HashMap<DDVariable,Integer>> retPaths = new HashSet<HashMap<DDVariable,Integer>>();

		
		if(allVars.size()>0 && pathVars.size()>0 && elems.size()>0) {
			
			DDVariable currVar = allVars.get(0);
			DDVariable currPathVar = pathVars.get(0);
			
			
			List<DDVariable> nextAllVars = allVars.subList(1, allVars.size());
			List<DDVariable> nextPathVars = pathVars;
			
			boolean canPathResolve = false;
			
			if(currVar.equals(currPathVar)) {
				canPathResolve = true;
				nextPathVars = pathVars.subList(1, pathVars.size());
			}
			else {
				nextPathVars = pathVars;
				while(nextPathVars.size()>0 && DDContext.getVariableIndex(currVar) > DDContext.getVariableIndex(nextPathVars.get(0))) {
					nextPathVars = nextPathVars.subList(1, nextPathVars.size());
				}
			}
			
			for(int varVal = 0; varVal < currVar.getValueCount(); varVal++) {
				
				
				HashMap<DDVariable,Integer> nextPath = prevPath;
				
				if(canPathResolve) {
					nextPath = new HashMap<DDVariable,Integer>();
					nextPath.putAll(prevPath);
					nextPath.put(currVar, varVal);
				}
				
				ArrayList<DDElement> nextElems = new ArrayList<DDElement>();
				
				boolean isZero = false;
				for(DDElement el:elems) {
					
					if(el instanceof DDNode) {
						DDNode n = (DDNode) el;
						if(n.getVariable().equals(currVar)) {
							nextElems.add(n.getChild(varVal));
						}
						else {
							nextElems.add(el);
						}
						
						
					}
					else {
						if(((DDLeaf) el).getValue() == 0.0d) {
							isZero = true;
							break;
						}
						
					}
				}
				
				if(!isZero) {
					retPaths.addAll(getAllUniquePaths(nextAllVars,nextPathVars,nextElems,nextPath));
				}
			}
			
		}
		else /*if(elems.size()>0)*/ {
			
			boolean isZero = false;
			
			if(elems.size()>0) {
				for(DDElement el:elems) {
					if(el instanceof DDLeaf && ((DDLeaf) el).getValue() == 0.0d) {
						isZero = true;
						break;
					}
				}
			}

			if(!isZero) {	
				retPaths.add(prevPath);
			}

		}

		
		
		return retPaths;
		
		
	}
	
	
	public static void indexAlphaVector(HashMap<DDLeaf,DDLeaf> maxLeavesAfter, DDElement alphaEl) {
		if(alphaEl instanceof DDNode) {
			DDNode n = (DDNode) alphaEl;
			
			for(int i=0;i<n.getVariable().getValueCount();++i) {
				indexAlphaVector(maxLeavesAfter,n.getChild(i));
			}
		}
		else {
			DDLeaf l = (DDLeaf) alphaEl;
			
			if(!maxLeavesAfter.containsKey(l)) {
				HashMap<DDLeaf,DDLeaf> newMaxLeavesAfter = new HashMap<DDLeaf,DDLeaf>();
				newMaxLeavesAfter.put(l, l);
				
				for(Entry<DDLeaf,DDLeaf> e:maxLeavesAfter.entrySet()) {
					DDLeaf currLeaf = e.getKey();
					DDLeaf largestAfterLeaf = e.getValue();
					
					if(largestAfterLeaf.getValue() < l.getValue()) {
						newMaxLeavesAfter.put(currLeaf, l);
					}
					else {
						newMaxLeavesAfter.put(currLeaf, largestAfterLeaf);
					}
				}
				
				maxLeavesAfter.putAll(newMaxLeavesAfter);
			}
		}
		
	}
	public static int maxDotProduct(
			List<DDElement> probElems, 
			List<DDElement> realElems, 
			List<DDLeaf> maxRealElemValues, 
			List<DDLeaf> minRealElemValues,
			List<HashMap<DDLeaf,DDLeaf>> maxLeavesAfter) {
		
		HashSet<DDVariable> varsSet = new HashSet<DDVariable>();
		
		List<Double> probElemProbLeft = new ArrayList<Double>();
		for(DDElement probEl:probElems) {
			probElemProbLeft.add(1.0d);
			
			varsSet.addAll(probEl.getVariables());
		}
		
		List<Double> results = new ArrayList<Double>();
		
		for(DDElement realEl:realElems) {
			results.add(0.0d);
			varsSet.addAll(realEl.getVariables());
		}
		
		List<DDVariable> vars = putInCanonicalOrder(new ArrayList<DDVariable>(varsSet));
		
		int numWinners = maxDotProduct(vars,probElems,1.0d,realElems,maxRealElemValues,minRealElemValues,results,maxLeavesAfter,new HashMap<HashSet<DDElement>,DDLeaf>());
		
		double maxValue = -Double.MAX_VALUE;
		int maxIx = -1;
		for(int i=0;i<results.size();++i) {
			Double result = results.get(i);
			if(result != null && result > maxValue) {
				maxValue = result;
				maxIx = i;
			}
		}
		
		//System.out.println("Num winners:" + numWinners);
		
		return maxIx;
		
	}
	protected static int maxDotProduct(List<DDVariable> vars, 
			List<DDElement> probElems, 
			Double probElemProbLeft,
			List<DDElement> realElems, 
			List<DDLeaf> maxRealElemValues, 
			List<DDLeaf> minRealElemValues, 
			List<Double> results,
			List<HashMap<DDLeaf,DDLeaf>> leafCounts,
			HashMap<HashSet<DDElement>,DDLeaf> applyCache) {
		
		int numWinners = 0;
		if(vars.size()>0) {
			List<DDVariable> nextVars = vars.subList(1, vars.size());
			DDVariable currVar = vars.get(0);
			
			for(int varVal = 0; varVal < currVar.getValueCount(); varVal++) {
				ArrayList<DDElement> nextProbElems = new ArrayList<DDElement>();
				ArrayList<DDElement> nextRealElems = new ArrayList<DDElement>();
				
				for(DDElement el:probElems) {
					if(el instanceof DDNode && ((DDNode) el).getVariable().equals(currVar)) {
						DDNode n = (DDNode) el;
						nextProbElems.add(n.getChild(varVal));
					}
					else {
						if(el instanceof DDLeaf) {
							DDLeaf l = (DDLeaf) el;
							if(l.getValue() == 0.0d) {
								return Integer.MAX_VALUE;
							}
						}
						nextProbElems.add(el);
					}
				}
				
				for(int i=0;i<realElems.size();++i) {
					if(results.get(i) != null) {
						DDElement el = realElems.get(i);
						if(el instanceof DDNode && ((DDNode) el).getVariable().equals(currVar)) {
							DDNode n = (DDNode) el;
							nextRealElems.add(n.getChild(varVal));
						}
						else {
							nextRealElems.add(el);
						}
					}
					else {
						nextRealElems.add(null);
					}
				}
				
				numWinners = maxDotProduct(nextVars,nextProbElems,probElemProbLeft,nextRealElems,maxRealElemValues,minRealElemValues,results,leafCounts,applyCache);
				
				if(numWinners<2) {
					return numWinners;
				}
				
			}
			
		}
		else {
			
			double probTotal = 1.0d;
			
			for(int i=0;i<probElems.size();++i) {
				DDLeaf currProbLeaf = (DDLeaf) probElems.get(i);
				double prob = currProbLeaf.getValue();
				
				probTotal *= prob;
			}
			
			probElemProbLeft-=probTotal;
			
			if(probElemProbLeft<0.0d) {
				System.out.println("TEST");
			}
			
			double maxResultSoFar = -Double.MAX_VALUE;
			int maxResultIxSoFar = -1;
			
			for(int i=0;i<realElems.size();++i) {
				if(results.get(i) != null) {
					DDLeaf currValueLeaf = (DDLeaf) realElems.get(i);
					
					HashMap<DDLeaf,DDLeaf> leafCount = leafCounts.get(i);
					
					if(leafCount.containsKey(currValueLeaf)) {
						maxRealElemValues.set(i, leafCount.get(currValueLeaf));
					}
					
					double value = currValueLeaf.getValue();
					double result = probTotal*value + results.get(i);
					
					if(result>maxResultSoFar) {
						maxResultSoFar = result;
						maxResultIxSoFar = i;
					}
					
					results.set(i, result);
				}
			}
			
			if(minRealElemValues.get(maxResultIxSoFar).getValue()<0) {
				maxResultSoFar += probElemProbLeft * minRealElemValues.get(maxResultIxSoFar).getValue();
			}
			
			for(int i=0;i<realElems.size();++i) {
				if(results.get(i) != null) {
					double maxLeft = probElemProbLeft * maxRealElemValues.get(i).getValue();
					
					if( maxResultSoFar > results.get(i) + maxLeft) {
						results.set(i, null);
					}
					else {
						numWinners++;
					}
				}
			}
			

			if(numWinners<1 && realElems.size()>1) {
				System.out.println("TEST");
			}
		}
		
		
		return numWinners;
	}
	
	public static double dotProduct(
			List<DDElement> probElems, 
			DDElement realElem) {
		
		HashSet<DDVariable> varsSet = new HashSet<DDVariable>(realElem.getVariables());
		
		for(DDElement probEl:probElems) {
			varsSet.addAll(probEl.getVariables());
		}
		
		ArrayList<DDVariable> vars = putInCanonicalOrder(new ArrayList<DDVariable>(varsSet));
		
		return dotProduct(vars,probElems,realElem,new HashMap<HashSet<DDElement>,Double>());
		
	}
	protected static double dotProduct(
			List<DDVariable> vars,
			List<DDElement> probElems, 
			DDElement realElem,
			HashMap<HashSet<DDElement>,Double> cache) {
		
		HashSet<DDElement> key = new HashSet<DDElement>(probElems);
		key.add(realElem);
		
		if(cache.containsKey(key)) {
			return cache.get(key);
		}
		double total = 0.0d;
		
		if(vars.size()>0) {
			List<DDVariable> nextVars = vars.subList(1, vars.size());
			DDVariable currVar = vars.get(0);
			
			for(int varVal = 0; varVal < currVar.getValueCount(); varVal++) {
				ArrayList<DDElement> nextProbElems = new ArrayList<DDElement>();
				DDElement nextRealElem = realElem;
				
				boolean isZero = false;
				for(DDElement el:probElems) {
					if(el instanceof DDNode && ((DDNode) el).getVariable().equals(currVar)) {
						DDNode n = (DDNode) el;
						nextProbElems.add(n.getChild(varVal));
					}
					else {
						if(el instanceof DDLeaf) {
							DDLeaf l = (DDLeaf) el;
							if(l.getValue() == 0.0d) {
								isZero = true;
								break;
							}
						}
						nextProbElems.add(el);
					}
				}
				
				if(isZero) {
					continue;
				}
				
				if(realElem instanceof DDNode && ((DDNode) realElem).getVariable().equals(currVar)) {
					nextRealElem = ((DDNode) realElem).getChild(varVal);
				}
				
				total+=dotProduct(nextVars,nextProbElems,nextRealElem,cache);
			}
		}
		else {
			DDLeaf realLeaf = (DDLeaf) realElem;
			total = realLeaf.getValue();
			
			for(DDElement el:probElems) {
				DDLeaf probLeaf = (DDLeaf) el;
				
				total*=probLeaf.getValue();
			}
		}
		
		cache.put(key, total);
		return total;
	}
	
	protected DDLeaf mapCollectLeafOperation(List<DDVariable> vars, List<DDElement> elems, BinaryOperation mapOp, BinaryOperation collectOp, HashMap<HashSet<DDElement>,DDLeaf> applyCache) {
		
		// There should be no duplicates within the elements, so this 
		// should just create set of the same items, but the order being
		// unimportant
		HashSet<DDElement> cacheKey = new HashSet<DDElement>(elems);
		
		if(applyCache.containsKey(cacheKey)) {
			return applyCache.get(cacheKey);
		}
		
		if(vars.size()>0) {
			List<DDVariable> nextVars = vars.subList(1, vars.size());
			DDVariable currVar = vars.get(0);
			
			ArrayList<DDElement> resolvableElems = new ArrayList<DDElement>();
			ArrayList<DDElement> unresolvableElems = new ArrayList<DDElement>();
			for(DDElement el:elems) {
				if(el instanceof DDNode && ((DDNode) el).getVariable().equals(currVar)) {
					resolvableElems.add(el);
				}
				else {
					unresolvableElems.add(el);
				}
			}
			
			Double value = null;
			for(int varVal = 0; varVal < currVar.getValueCount(); varVal++) {
				ArrayList<DDElement> nextElems = new ArrayList<DDElement>(unresolvableElems);
				
				for(DDElement el:resolvableElems) {
					DDNode n = (DDNode) el;
					nextElems.add(n.getChild(varVal));
				}
				
				DDLeaf resLeaf = mapCollectLeafOperation(nextVars,nextElems,mapOp,collectOp,applyCache);
				if(value == null) {
					value = resLeaf.getValue();
				}
				else {
					value = collectOp.invoke(value, resLeaf.getValue());
				}
			}
			
			DDLeaf resLeaf = makeLeaf(value);
			applyCache.put(cacheKey, resLeaf);
			return resLeaf;
		}
		else {
			
			Double value = null;
			for(DDElement el:elems) {
				DDLeaf l = (DDLeaf) el;
				if(value == null) {
					value = l.getValue();
				}
				else {
					value = mapOp.invoke(value, l.getValue());
				}
				
				if(mapOp instanceof MultiplicationOperation && value == 0.0d) {
					break;
				}
			}
			
			DDLeaf resLeaf = makeLeaf(value);
			applyCache.put(cacheKey, resLeaf);
			return resLeaf;
		}

	}
	
	protected DDElement mapCollectOperation(List<DDVariable> allVarsInOrder, List<DDVariable> collectAtVarsInOrder, List<DDElement> elems, BinaryOperation mapOp, BinaryOperation collectOp, HashMap<HashSet<DDElement>,DDElement> applyCache) {
		
		// There should be no duplicates within the elements, so this 
		// should just create set of the same items, but the order being
		// unimportant
		HashSet<DDElement> cacheKey = new HashSet<DDElement>(elems);
		
		if(applyCache.containsKey(cacheKey)) {
			return applyCache.get(cacheKey);
		}
		
		
		if(allVarsInOrder.size()>0) {
			List<DDVariable> nextAllVarsInOrder = allVarsInOrder.subList(1, allVarsInOrder.size());
			List<DDVariable> nextCollectAtVarsInOrder = null;
			
			DDVariable currVar = allVarsInOrder.get(0);
			boolean collectHere = collectAtVarsInOrder!=null && collectAtVarsInOrder.size()>0 && currVar.equals(collectAtVarsInOrder.get(0));
			
			if(collectHere) {
				nextCollectAtVarsInOrder = collectAtVarsInOrder.subList(1, collectAtVarsInOrder.size());
			} else {
				nextCollectAtVarsInOrder = collectAtVarsInOrder;
			}
			
			ArrayList<DDElement> resolvableElems = new ArrayList<DDElement>();
			ArrayList<DDElement> unresolvableElems = new ArrayList<DDElement>();
			for(DDElement el:elems) {
				if(el instanceof DDNode && ((DDNode) el).getVariable().equals(currVar)) {
					resolvableElems.add(el);
				}
				else if(collectAtVarsInOrder!=null && collectAtVarsInOrder.size()>0 && mapOp instanceof MultiplicationOperation && el instanceof DDLeaf && ((DDLeaf) el).getValue() == 0.0d) {
					DDLeaf resLeaf = makeLeaf(0.0d);
					applyCache.put(cacheKey, resLeaf);
					return resLeaf;
				}
				else {
					unresolvableElems.add(el);
				}
			}

			ArrayList<DDElement> childrenList = new ArrayList<DDElement>();
			for(int varVal = 0; varVal < currVar.getValueCount(); varVal++) {
				ArrayList<DDElement> nextElems = new ArrayList<DDElement>(unresolvableElems);
				
				for(DDElement el:resolvableElems) {
					DDNode n = (DDNode) el;
					nextElems.add(n.getChild(varVal));
				}
				
				DDElement resEl = mapCollectOperation(nextAllVarsInOrder,nextCollectAtVarsInOrder,nextElems,mapOp,collectOp,applyCache);
				childrenList.add(resEl);
			}
			
			DDElement resEl;
			if(collectHere) {
				resEl = mapCollectOperation(nextAllVarsInOrder,null,childrenList, collectOp, null, applyCache);
			}
			else {
				BaseDDElement[] children = childrenList.toArray(new BaseDDElement[0]);
				
				boolean allEqual = true;
				BaseDDElement currEl = children[0];
				for(int i=1;i<currVar.getValueCount() && allEqual;i++) {
					allEqual = allEqual && currEl.equals(children[i]);
					currEl = children[i];
				}
				
				if(allEqual) {
					resEl = children[0];
				}
				else {
					resEl = makeNode(currVar,children);
				}
			}
			
			applyCache.put(cacheKey, resEl);
			return resEl;
		}
		else {
			
			Double value = null;
			for(DDElement el:elems) {
				DDLeaf l = (DDLeaf) el;
				if(value == null) {
					value = l.getValue();
				}
				else {
					value = mapOp.invoke(value, l.getValue());
				}
				
				if(mapOp instanceof MultiplicationOperation && value == 0.0d) {
					break;
				}
			}
			
			DDLeaf resLeaf = makeLeaf(value);
			applyCache.put(cacheKey, resLeaf);
			return resLeaf;
		}

	}
	
	protected DDElement sumSubtrees(DDElement el,List<DDVariable> subtreeElimVars, HashMap<DDElement,DDElement> applyCache) {
		ArrayList<DDVariable> allVarsInOrder = putInCanonicalOrder(new ArrayList<DDVariable>(el.getVariables()));
		HashMap<DDElement,Integer> multipliers = new HashMap<DDElement,Integer>();
		multipliers.put(el, 1);
		
		return sumSubtrees(allVarsInOrder,putInCanonicalOrder(subtreeElimVars),multipliers,new  HashMap<HashMap<DDElement,Integer>,DDElement>());
	}
	
	protected DDElement sumSubtrees(List<DDVariable> allVarsInOrder, List<DDVariable> collectAtVarsInOrder, HashMap<DDElement,Integer> multipliers, HashMap<HashMap<DDElement,Integer>,DDElement> applyCache) {
		
		// There should be no duplicates within the elements, so this 
		// should just create set of the same items, but the order being
		// unimportant
		
		if(applyCache.containsKey(multipliers)) {
			return applyCache.get(multipliers);
		}
		
		DDElement resEl;
		if(allVarsInOrder.size()>0) {
			List<DDVariable> nextAllVarsInOrder = allVarsInOrder.subList(1, allVarsInOrder.size());
			List<DDVariable> nextCollectAtVarsInOrder = null;
			
			DDVariable currVar = allVarsInOrder.get(0);
			boolean collectHere = collectAtVarsInOrder!=null && collectAtVarsInOrder.size()>0 && currVar.equals(collectAtVarsInOrder.get(0));
			
			if(collectHere) {
				nextCollectAtVarsInOrder = collectAtVarsInOrder.subList(1, collectAtVarsInOrder.size());
			} else {
				nextCollectAtVarsInOrder = collectAtVarsInOrder;
			}
			
			HashMap<DDElement,Integer> newMultipliers = new HashMap<DDElement,Integer>();
			
			ArrayList<DDElement> resolvableElems = new ArrayList<DDElement>();
			for(Entry<DDElement,Integer> e:multipliers.entrySet()) {
				DDElement el = e.getKey();
				
				if(el instanceof DDNode && ((DDNode) el).getVariable().equals(currVar)) {
					resolvableElems.add(el);
				}
				else {
					
					if(collectHere) {
						newMultipliers.put(el, e.getValue() * currVar.getValueCount());
					}
					else {
						newMultipliers.put(el, e.getValue());
					}
				}
			}
			
			
			if(collectHere) {
				for(DDElement el:resolvableElems) {
					DDNode n = (DDNode) el;
					
					for(int varVal = 0; varVal < currVar.getValueCount(); varVal++) {
						DDElement child = n.getChild(varVal);
						if(!newMultipliers.containsKey(child)){
							newMultipliers.put(child, multipliers.get(n));
						}
						else {
							newMultipliers.put(child, newMultipliers.get(child) + multipliers.get(n));
						}
						
					}
				}
				
				resEl = sumSubtrees(nextAllVarsInOrder,nextCollectAtVarsInOrder,newMultipliers,applyCache);
			}
			else {
				ArrayList<DDElement> childrenList = new ArrayList<DDElement>();
				for(int varVal = 0; varVal < currVar.getValueCount(); varVal++) {
					
					HashMap<DDElement,Integer> tempMultipliers = new HashMap<DDElement,Integer>();
					tempMultipliers.putAll(newMultipliers);
					
					for(DDElement el:resolvableElems) {
						
						DDNode n = (DDNode) el;
						DDElement child = n.getChild(varVal);
						
						if(!tempMultipliers.containsKey(child)){
							tempMultipliers.put(child, multipliers.get(n));
						}
						else {
							tempMultipliers.put(child, tempMultipliers.get(child) + multipliers.get(n));
							
						}
					}
					
					childrenList.add(sumSubtrees(nextAllVarsInOrder,nextCollectAtVarsInOrder,tempMultipliers,applyCache));
				}
				
				BaseDDElement[] children = childrenList.toArray(new BaseDDElement[0]);
				
				boolean allEqual = true;
				BaseDDElement currEl = children[0];
				for(int i=1;i<currVar.getValueCount() && allEqual;i++) {
					allEqual = allEqual && currEl.equals(children[i]);
					currEl = children[i];
				}
				
				
				if(allEqual) {
					resEl = children[0];
				}
				else {
					resEl = makeNode(currVar,children);
				}
				
				
			}

		}
		else {
			
			
			Double value = null;
			for(Entry<DDElement,Integer> e:multipliers.entrySet()) {
				if(! (e.getKey() instanceof DDLeaf)) {
					System.out.println("Not a leaf");
				}
				DDLeaf l = (DDLeaf) e.getKey();
				if(value == null) {
					value = l.getValue() * e.getValue();
				}
				else {
					value = value + l.getValue() * e.getValue();
				}
			}
			
			resEl = makeLeaf(value);
		}
		
		applyCache.put(multipliers, resEl);
		return resEl;
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
		str += "//" + el.getVariables() + "\n";
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
