package masg.dd.pomdp.agent.policy;

import java.util.HashMap;

import masg.dd.AlgebraicDD;
import masg.dd.ipomdp.IPOMDP;
import masg.dd.representation.DDInfo;
import masg.dd.representation.builder.DDBuilder;
import masg.dd.variables.DDVariable;

public class IQMDPPolicyBuilder {
	IPOMDP p;
	double discount = 0.9f;
	
	public IQMDPPolicyBuilder(IPOMDP p) {
		this.p = p;
	}
	
	public IQMDPPolicy build() {
		
		AlgebraicDD valFn = new AlgebraicDD(DDBuilder.build(new DDInfo(p.getStatesPrime(),false),0.0f).getRootNode());
		
		HashMap< HashMap<DDVariable,Integer>,HashMap<HashMap<DDVariable,Integer>, AlgebraicDD>> qFn = new HashMap< HashMap<DDVariable,Integer>,HashMap<HashMap<DDVariable,Integer>, AlgebraicDD>>();
		
		double bellmanError = 1.0d;
		
		for(int i=0;i<3 && bellmanError > 0.001d;i++) {
			System.out.println("Iteration:" + i);
			
			DDBuilder ddResult = null;
			AlgebraicDD valFnNew = new AlgebraicDD(DDBuilder.build(new DDInfo(p.getStates(),false),-Double.MAX_VALUE).getRootNode());
			for(HashMap<DDVariable,Integer> actSpacePt1:p.getActionSpace()) {
				
				for(HashMap<DDVariable,Integer> actSpacePt2:p.getActionOtherSpace()) {
					AlgebraicDD futureVal = p.getTransitionFunction().restrict(actSpacePt1).restrict(actSpacePt2).multiply(valFn);
					futureVal = futureVal.sumOut(p.getStatesPrime());
					futureVal = futureVal.multiply(discount);
					futureVal = futureVal.plus(p.getRewardFunction().restrict(actSpacePt1).restrict(actSpacePt2));
					
					ddResult = DDBuilder.approximate(futureVal.getFunction(), bellmanError * (1.0d-discount)/2.0d);
					
					futureVal = new AlgebraicDD(ddResult.getRootNode());
					System.out.println("computed actions:" + actSpacePt1 + "," + actSpacePt2);
					
					if(!qFn.containsKey(actSpacePt1)) {
						qFn.put(actSpacePt1, new HashMap<HashMap<DDVariable,Integer>, AlgebraicDD>());
					}
					qFn.get(actSpacePt1).put(actSpacePt2, futureVal);
					
					valFnNew = valFnNew.max(futureVal);
					
				}
			}
			
			valFnNew = valFnNew.prime();
			
			bellmanError = DDBuilder.findMaxLeaf(valFnNew.absDiff(valFn).getFunction()).getValue();
			
			System.out.println("bellmanError:" + bellmanError);
			
			valFn = valFnNew;
		}
		
		return new IQMDPPolicy(qFn);
	}
	
}
