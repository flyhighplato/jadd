package masg.dd.pomdp.agent.policy;

import java.util.ArrayList;
import java.util.HashMap;

import masg.dd.AlgebraicDD;
import masg.dd.operations.MultiplicationOperation;
import masg.dd.pomdp.POMDP;
import masg.dd.representations.dag.ImmutableDDElement;
import masg.dd.representations.tables.TableDD;
import masg.dd.variables.DDVariable;

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
				System.out.println("0");
				ArrayList<ImmutableDDElement> dags = new ArrayList<ImmutableDDElement>();
				dags.add(valFn.getFunction());
				for(AlgebraicDD ddComp:p.getTransitionFunction(actSpacePt).getComponentFunctions()) {
					dags.add(ddComp.getFunction());
				}
				
				System.out.println("1");
				ImmutableDDElement resDD = TableDD.build(qFnVars, dags, new MultiplicationOperation());
				System.out.println("2");
				AlgebraicDD futureVal = new AlgebraicDD(resDD);
				System.out.println("3");
				futureVal = futureVal.sumOut(p.getStatesPrime());
				System.out.println("4");
				futureVal = futureVal.multiply(discount);
				System.out.println("5");
				futureVal = futureVal.plus(p.getRewardFunction(actSpacePt));
				System.out.println("6");
				TableDD ddResult = TableDD.approximate(futureVal.getFunction(), bellmanError * (1.0d-discount)/2.0d);
				System.out.println("7");
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
