package masg.dd.pomdp.agent.policy;

import java.util.ArrayList;
import java.util.HashMap;

import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.MutableDDElementBuilder;
import masg.dd.ProbDD;
import masg.dd.operations.MultiplicationOperation;
import masg.dd.pomdp.POMDP;
import masg.dd.representations.dag.ImmutableDDElement;
import masg.dd.representations.dag.MutableDDElement;
import masg.dd.representations.dag.MutableDDNode;
import masg.dd.representations.tables.TableDD;
import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;
import masg.util.BitMap;

public class QMDPPolicyBuilder {
	POMDP p;
	double discount = 0.9f;
	
	public QMDPPolicyBuilder(POMDP p) {
		this.p = p;
	}
	
	public HashMap<HashMap<DDVariable, Integer>, AlgebraicDD> build() {
		
		AlgebraicDD valFn = new AlgebraicDD(TableDD.build(p.getStatesPrime(),0.0f).asDagDD(false));
		
		
		ArrayList<DDVariable> qFnVars = new ArrayList<DDVariable>();
		qFnVars.addAll(p.getStates());
		qFnVars.addAll(p.getStatesPrime());
		
		HashMap<HashMap<DDVariable,Integer>, AlgebraicDD> qFn = new HashMap<HashMap<DDVariable,Integer>, AlgebraicDD>();
		
		double bellmanError = 0.0f;
		
		
		
		for(int i=0;i<10;i++) {
			
			AlgebraicDD valFnNew = new AlgebraicDD(TableDD.build(p.getStates(),-Double.MAX_VALUE).asDagDD(false));
			for(HashMap<DDVariable,Integer> actSpacePt:p.getActionSpace()) {
				
				ArrayList<ImmutableDDElement> dags = new ArrayList<ImmutableDDElement>();
				dags.add(valFn.getFunction());
				for(AlgebraicDD ddComp:p.getTransitionFunction(actSpacePt).getComponentFunctions()) {
					dags.add(ddComp.getFunction());
				}
				
				ImmutableDDElement resDD = TableDD.build(qFnVars, dags, new MultiplicationOperation());
				AlgebraicDD futureVal = new AlgebraicDD(resDD);
				futureVal = futureVal.sumOut(p.getStatesPrime());
				futureVal = futureVal.multiply(discount);
				futureVal = futureVal.plus(p.getRewardFunction(actSpacePt));
				TableDD ddResult = TableDD.approximate(futureVal.getFunction(), bellmanError * (1.0d-discount)/2.0d);
				
				futureVal = new AlgebraicDD(ddResult.asDagDD(false));
				System.out.println("computed action:" + actSpacePt);
				
				valFnNew = valFnNew.max(futureVal);
				qFn.put(actSpacePt, futureVal);
			}
			//System.out.println("valFnNew:" + valFnNew);
			//TableDD ddResult = TableDD.approximate(valFnNew.getFunction(), 5.5f);
			//valFnNew = new AlgebraicDD(ddResult.asDagDD(false));
			
			
			valFnNew = valFnNew.prime();
			
			System.out.println("Finding bellman error");
			bellmanError = TableDD.findMaxLeaf(valFnNew.absDiff(valFn).getFunction()).getValue();
			
			
			System.out.println("bellmanError:" + bellmanError);
			
			valFn = valFnNew;
			
			//System.out.println(ddResult);
			
			
		}
		
		
		
		return null;
		
	}
}
