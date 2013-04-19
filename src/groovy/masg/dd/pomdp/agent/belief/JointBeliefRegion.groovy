package masg.dd.pomdp.agent.belief

import java.util.ArrayList;
import java.util.Date;

import masg.dd.AlgebraicDD
import masg.dd.ProbDD
import masg.dd.pomdp.AbstractPOMDP
import masg.dd.pomdp.IPOMDP
import masg.dd.pomdp.POMDP;
import masg.dd.pomdp.agent.policy.Policy;
import masg.dd.variables.DDVariable

class JointBeliefRegion {
	IPOMDP p
	ArrayList<JointBelief> beliefSamples = []
	
	public sampleStartingWith(JointBelief initBelief, int numSamples, int episodeLength, AbstractPOMDP pMe, AbstractPOMDP pOther, Policy policyMe, Policy policyOther){
		beliefSamples << initBelief
		
		JointBelief beliefMe = initBelief
		JointBelief beliefOther = initBelief.reverse()
		int samplesTaken = 1;
		int episodeStep = 0;
		
		while(samplesTaken<numSamples) {

			long startTime = new Date().getTime();
			
			if(beliefSamples.size()%10==0) {
				println "Sampled ${beliefSamples.size()}"
				
				
			}
			
			if(episodeStep>=episodeLength) {
				beliefMe = initBelief
				beliefOther = initBelief.reverse()
				episodeStep = 0;
			}
			
			HashMap<DDVariable,Integer> actMe = policyMe.getAction(beliefMe)
			HashMap<DDVariable,Integer> actOther = policyOther.getAction(beliefOther)
			
			HashMap<DDVariable,Integer> obsMe = beliefMe.sampleNextObservation(actMe, actOther);
			HashMap<DDVariable,Integer> obsOther = beliefOther.sampleNextObservation(actOther,actMe);
			
			beliefMe = beliefMe.getNextBelief(actMe, actOther, obsMe, obsOther)
			beliefOther = beliefOther.getNextBelief(actOther, actMe, obsOther, obsMe)
			
			boolean goodSample = true;
			
			
			if(goodSample) {
				samplesTaken++;
				beliefSamples << new JointBelief(beliefMe, beliefOther)
			}
			
			episodeStep++;
			
			println "Took " + ( new Date().getTime() - startTime) + " milliseconds for sample"
		}
		
		println "Sampled ${beliefSamples.size()}"
	}
}
