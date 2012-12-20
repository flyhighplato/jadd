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
	
	public QMDPPolicy build() {
		
		AlgebraicDD valFn = new AlgebraicDD(TableDD.build(p.getStatesPrime(),0.0f).asDagDD(false));
		
		
		ArrayList<DDVariable> qFnVars = new ArrayList<DDVariable>();
		qFnVars.addAll(p.getStates());
		qFnVars.addAll(p.getStatesPrime());
		
		HashMap<HashMap<DDVariable,Integer>, AlgebraicDD> qFn = new HashMap<HashMap<DDVariable,Integer>, AlgebraicDD>();
		
		double bellmanError = 1.0d;
		
		for(int i=0;i<3 && bellmanError > 0.001d;i++) {
			System.out.println("Iteration:" + i);
			
			TableDD ddResult = null;
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
				ddResult = TableDD.approximate(futureVal.getFunction(), bellmanError * (1.0d-discount)/2.0d);
				futureVal = new AlgebraicDD(ddResult.asDagDD(false));
				System.out.println("computed action:" + actSpacePt);
				
				valFnNew = valFnNew.max(futureVal);
				qFn.put(actSpacePt, futureVal);
			}
			
			valFnNew = valFnNew.prime();
			
			bellmanError = TableDD.findMaxLeaf(valFnNew.absDiff(valFn).getFunction()).getValue();
			
			System.out.println("bellmanError:" + bellmanError);
			
			valFn = valFnNew;
		}
		
		
		
		return new QMDPPolicy(qFn);
		
	}
}
