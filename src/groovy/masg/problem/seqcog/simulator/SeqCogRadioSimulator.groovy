package masg.problem.seqcog.simulator

import java.util.Random;

import masg.dd.FactoredCondProbDD
import masg.dd.ProbDD
import masg.dd.pomdp.agent.belief.Belief
import masg.dd.pomdp.agent.belief.POMDPBelief
import masg.dd.pomdp.agent.policy.Policy
import masg.dd.variables.DDVariable
import masg.problem.seqcog.SeqCogRadioAgent
import masg.problem.seqcog.SeqCogRadioProblemPOMDP


class SeqCogRadioSimulator {
	int numChannels = 3;
	int maxMessageLength = 5;
	int maxMessageDeadline = maxMessageLength;
	Random random = new Random();
	
	public void simulate(SeqCogRadioProblemPOMDP problem, Policy pol, int numTrials, int numSteps) {
		
		List<DDVariable> chOccStates = []
		numChannels.times {
			chOccStates << new DDVariable("ch${it}_is_occ",2);
		}
		
		List<DDVariable> qLenStates = []
		numChannels.times {
			qLenStates << new DDVariable("q${it}_length",maxMessageLength);
		}
		
		List<DDVariable> qDealineStates = []
		numChannels.times {
			qDealineStates << new DDVariable("q${it}_deadline",maxMessageDeadline);
		}
		
		int numSent = 0;
		int numDropped = 0;
		
		numTrials.times { trialIx ->
			
			SeqCogRadioAgent agent = new SeqCogRadioAgent(new POMDPBelief(problem.getPOMDP(),problem.getPOMDP().getInitialBelief()));
			
			HashMap<DDVariable,Integer> actualState = new HashMap<DDVariable,Integer>()
			numChannels.times{
				actualState[chOccStates[it]]=0;
				actualState[qLenStates[it]]=0
				actualState[qDealineStates[it]]=0
			}
			
			numSteps.times {
				
				numChannels.times{
					println "${chOccStates[it]} = ${actualState[chOccStates[it]]}"
					println "${qLenStates[it]} = ${actualState[qLenStates[it]]}"
					println "${qDealineStates[it]} = ${actualState[qDealineStates[it]]}"
				}
				println()
				
				
				HashMap<DDVariable,Integer> action = pol.getAction(agent.currBelief);
				
				println "action: ${action}"
				println()
				
				FactoredCondProbDD restrTransFn = problem.getPOMDP().getTransitionFunction().restrict(action)
				restrTransFn = restrTransFn.restrict(actualState)
				restrTransFn = restrTransFn.normalize();
				
				HashMap<DDVariable,Integer> actualStateNew = sampleSpacePoint(problem.getPOMDP().getStatesPrime(), restrTransFn)
				
				numChannels.times{
					println "${chOccStates[it].getPrimed()} = ${actualStateNew[chOccStates[it].getPrimed()]}"
					println "${qLenStates[it].getPrimed()} = ${actualStateNew[qLenStates[it].getPrimed()]}"
					println "${qDealineStates[it].getPrimed()} = ${actualStateNew[qDealineStates[it].getPrimed()]}"
				}
				println()
				
				FactoredCondProbDD restrObsFn = problem.getPOMDP().getObservationFunction().restrict(action)
				restrObsFn = restrObsFn.restrict(actualStateNew)
				restrObsFn = restrObsFn.normalize()
				
				HashMap<DDVariable,Integer> obs = sampleSpacePoint(problem.getPOMDP().getObservations(), restrObsFn);
				
				println "obs: ${obs}"
				println()
				
				POMDPBelief b = agent.currBelief
				agent.currBelief = b.getNextBelief(action, obs)
				
				numChannels.times{
					
					if(actualStateNew[qLenStates[it].getPrimed()]==0) {
						numSent++;
					}
					
					if(actualStateNew[qLenStates[it].getPrimed()]>actualStateNew[qDealineStates[it].getPrimed()]) {
						numDropped++;
					}
					actualState[chOccStates[it]] = actualStateNew[chOccStates[it].getPrimed()];
					actualState[qLenStates[it]] = actualStateNew[qLenStates[it].getPrimed()]
					actualState[qDealineStates[it]] = actualStateNew[qDealineStates[it].getPrimed()]
				}
				println "Sent: $numSent"
				println "Dropped: $numDropped"
			}
		}
	}
	
	private HashMap<DDVariable,Integer> sampleSpacePoint(ArrayList<DDVariable> variables, FactoredCondProbDD probFn) {
		HashMap<DDVariable,Integer> point = new HashMap<DDVariable,Integer>();
		
		for(DDVariable variable:variables) {
			ArrayList<DDVariable> sumOutVars = new ArrayList<DDVariable>(variables);
			sumOutVars.remove(variable);
			
			ProbDD probTempFn = probFn.sumOut(sumOutVars).toProbabilityDD();
			
			double thresh = random.nextDouble();
			double weight = 0.0f;
			
			for(int i=0;i<variable.getValueCount();i++){
				HashMap<DDVariable,Integer> tempPt = new HashMap<DDVariable,Integer>();
				tempPt.put(variable,i);
				weight += probTempFn.getValue(tempPt);
				if(weight>thresh) {
					point.put(variable,i);
					break;
				}
			}
		}
		return point;
	}
}
