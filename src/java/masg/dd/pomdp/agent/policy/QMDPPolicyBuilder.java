package masg.dd.pomdp.agent.policy;

import java.util.ArrayList;
import java.util.HashMap;

import masg.dd.AlgebraicDD;
import masg.dd.pomdp.POMDP;
import masg.dd.representation.DDInfo;
import masg.dd.representation.builder.DDBuilder;
import masg.dd.variables.DDVariable;

public class QMDPPolicyBuilder {
	POMDP p;
	double discount = 0.9f;
	
	public QMDPPolicyBuilder(POMDP p) {
		this.p = p;
	}
	
	public QMDPPolicy build() {
		
		AlgebraicDD valFn = new AlgebraicDD(DDBuilder.build(new DDInfo(p.getStatesPrime(),false),0.0f).getRootNode());
		
		
		ArrayList<DDVariable> qFnVars = new ArrayList<DDVariable>();
		qFnVars.addAll(p.getStates());
		qFnVars.addAll(p.getStatesPrime());
		
		HashMap<HashMap<DDVariable,Integer>, AlgebraicDD> qFn = new HashMap<HashMap<DDVariable,Integer>, AlgebraicDD>();
		
		double bellmanError = 1.0d;
		
		for(int i=0;i<50 && bellmanError > 0.001d;i++) {
			System.out.println("Iteration:" + i);
			
			DDBuilder ddResult = null;
			AlgebraicDD valFnNew = new AlgebraicDD(DDBuilder.build(new DDInfo(p.getStates(),false),-Double.MAX_VALUE).getRootNode());
			for(HashMap<DDVariable,Integer> actSpacePt:p.getActionSpace()) {
				AlgebraicDD futureVal = p.getTransitionFunction(actSpacePt).multiply(valFn);
				futureVal = futureVal.sumOut(p.getStatesPrime());
				futureVal = futureVal.multiply(discount);
				futureVal = futureVal.plus(p.getRewardFunction(actSpacePt));
				ddResult = DDBuilder.approximate(futureVal.getFunction(), bellmanError * (1.0d-discount)/2.0d);
				futureVal = new AlgebraicDD(ddResult.getRootNode());
				System.out.println("computed action:" + actSpacePt);
				
				valFnNew = valFnNew.max(futureVal);
				qFn.put(actSpacePt, futureVal);
			}
			
			valFnNew = valFnNew.prime();
			
			bellmanError = DDBuilder.findMaxLeaf(valFnNew.absDiff(valFn).getFunction()).getValue();
			
			System.out.println("bellmanError:" + bellmanError);
			
			valFn = valFnNew;
		}
		
		
		
		return new QMDPPolicy(qFn);
		
	}
}
