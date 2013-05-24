package masg.dd.pomdp.agent.belief

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Map.Entry

import masg.dd.AlgebraicDD
import masg.dd.CondProbDD
import masg.dd.FactoredCondProbDD
import masg.dd.ProbDD;
import masg.dd.pomdp.AbstractPOMDP
import masg.dd.pomdp.IPOMDP
import masg.dd.pomdp.agent.policy.BeliefAlphaVectorPolicy
import masg.dd.pomdp.agent.policy.Policy
import masg.dd.pomdp.agent.policy.RandomPolicy
import masg.dd.pomdp.agent.policy.serialization.AlphaVectorPolicyReader
import masg.dd.variables.DDVariable;
import masg.problem.tag.TagProblemIPOMDP;
import spock.lang.Specification

class BeliefExpectationSamplerSpec extends Specification {
	def "experimenting with belief expectation"() {
		when:
		
			
			TagProblemIPOMDP problem = new TagProblemIPOMDP()
			AbstractPOMDP pMe = problem.getIPOMDP();
			AbstractPOMDP pOther = problem.otherAgents.get(0);
			
			String fileName = "100_100_100.policy"
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			AlphaVectorPolicyReader policyReader = new AlphaVectorPolicyReader(reader);
			BeliefAlphaVectorPolicy pol0 = policyReader.read(0)
			reader.close()
			
			reader = new BufferedReader(new FileReader(fileName));
			policyReader = new AlphaVectorPolicyReader(reader);
			BeliefAlphaVectorPolicy pol1 = policyReader.read(1)
			reader.close()
		
			Policy polOther = pol1;
			Policy polMe = pol0;
			
			CondProbDD temp = new CondProbDD(pMe.getStates(),pOther.getStates(),0,1.0d)
			temp = temp.normalize()
			FactoredCondProbDD beliefHimGivenBeliefMe = new FactoredCondProbDD(temp)
			
			FactoredCondProbDD transitionProbOther;
			HashSet<DDVariable> retainVarsOther = new HashSet<DDVariable>();
			retainVarsOther.addAll(pOther.getObservations());
			retainVarsOther.addAll(pOther.getActions());
			retainVarsOther.addAll(pMe.getActions());
			retainVarsOther.addAll(pOther.getStatesPrime());
			
			HashSet<DDVariable> retainVarsMe = new HashSet<DDVariable>();
			retainVarsMe.addAll(pMe.getObservations());
			retainVarsMe.addAll(pMe.getActions());
			retainVarsMe.addAll(pOther.getActions());
			retainVarsMe.addAll(pMe.getStatesPrime());
			
			FactoredCondProbDD beliefMe = pMe.initialBelief;
			FactoredCondProbDD expectedBeliefOther = pOther.initialBelief;

			DDVariable w_row_me_var = new DDVariable(0,"w_row",5)
			DDVariable w_col_me_var = new DDVariable(0,"w_col",5)
			DDVariable a1_row_me_var = new DDVariable(0,"a1_row",5)
			DDVariable a1_col_me_var = new DDVariable(0,"a1_col",5)
			DDVariable a2_row_me_var = new DDVariable(0,"a2_row",5)
			DDVariable a2_col_me_var = new DDVariable(0,"a2_col",5)
			
			DDVariable w_row_other_var = new DDVariable(1,"w_row",5)
			DDVariable w_col_other_var = new DDVariable(1,"w_col",5)
			DDVariable a1_row_other_var = new DDVariable(1,"a1_row",5)
			DDVariable a1_col_other_var = new DDVariable(1,"a1_col",5)
			DDVariable a2_row_other_var = new DDVariable(1,"a2_row",5)
			DDVariable a2_col_other_var = new DDVariable(1,"a2_col",5)
			
			ArrayList<ArrayList<ArrayList<DDVariable>>> vars = []
			def closures = []
			
			vars << [ [w_row_me_var],[w_row_other_var] ]
			
			closures << { Map variables ->
				String varName = "w_row"
				variables[varName] == variables[1][varName]? 1.0d: 0.0d
			}
			
			vars << [ [w_col_me_var],[w_col_other_var] ]
			
			closures << { Map variables ->
				String varName = "w_col"
				variables[varName] == variables[1][varName]? 1.0d: 0.0d
			}
			
			vars << [ [a1_col_me_var],[a2_col_other_var] ]
			
			closures << { Map variables ->
				variables["a1_col"] == variables[1]["a2_col"]? 1.0d: 0.0d
			}
			
			vars << [ [a1_row_me_var],[a2_row_other_var] ]
			
			closures << { Map variables ->
				variables["a1_row"] == variables[1]["a2_row"]? 1.0d: 0.0d
			}
			
			vars << [ [a2_col_me_var],[a1_col_other_var] ]
			
			closures << { Map variables ->
				variables["a2_col"] == variables[1]["a1_col"]? 1.0d: 0.0d
			}
			
			vars << [ [a2_row_me_var],[a1_row_other_var] ]
			
			closures << { Map variables ->
				variables["a2_row"] == variables[1]["a1_row"]? 1.0d: 0.0d
			}
			
			
			FactoredCondProbDD worldStateTranslator = new FactoredCondProbDD(vars,0,closures)
			
			AlgebraicDD dotProdcutOnes = new AlgebraicDD(pOther.getStates(),1.0d);
			
			HashMap<HashMap<DDVariable,Integer>,Double> actDist = new HashMap<HashMap<DDVariable,Integer>,Double>();
			actDist.put(pol1.getAction(expectedBeliefOther),1.0d);
			
			AlgebraicDD tempAct = new AlgebraicDD(pOther.getActions(),0.0d);
			actDist.each{ act, actProb ->
				tempAct = tempAct.plus(new AlgebraicDD(pOther.getActions(),1,act).multiply(actProb));
			}
			tempAct = tempAct.normalize()
			FactoredCondProbDD actionProbOther = new FactoredCondProbDD(new CondProbDD(new ArrayList<DDVariable>(),new ArrayList<DDVariable>(pOther.getActions()),tempAct));
			actionProbOther = actionProbOther.normalize()
			
			100.times {
				
				
				
				HashMap<DDVariable,Integer> actMe = polMe.getAction(beliefMe)
				FactoredCondProbDD actionProbMeAccordingToOther = new FactoredCondProbDD(new CondProbDD(new ArrayList<DDVariable>(),new ArrayList<DDVariable>(pOther.getActions()),1,1.0d));
				actionProbMeAccordingToOther = actionProbMeAccordingToOther.normalize()
				
				FactoredCondProbDD stateOfOtherWorld = worldStateTranslator.multiply(beliefMe);
				//stateOfOtherWorld = stateOfOtherWorld.prime();
				
				
				FactoredCondProbDD transitionProbMe =  pMe.getTransitionFunction().multiply(beliefMe,retainVarsMe);
				transitionProbMe =  pMe.getObservationFunction().multiply(transitionProbMe,retainVarsMe);
				transitionProbMe = transitionProbMe.restrict(actMe)
				transitionProbMe = transitionProbMe.multiply(actionProbOther)
				transitionProbMe = transitionProbMe.sumOut(pMe.getStates())
				
				FactoredCondProbDD myObservationProb = transitionProbMe.sumOut(pMe.getStatesPrime())
				myObservationProb = myObservationProb.normalize()
				HashMap<DDVariable,Integer> obs = sampleSpacePoint(pMe.getObservations(),myObservationProb)
				beliefMe = transitionProbMe.restrict(obs).unprime()
				
				FactoredCondProbDD stateOfOtherWorldNew = worldStateTranslator.multiply(beliefMe).prime();
				
				FactoredCondProbDD obsProbOther = pOther.getObservationFunction().restrict(actMe)
				//obsProbOther = obsProbOther.multiply(actionProbOther);
				obsProbOther = obsProbOther.multiply(stateOfOtherWorldNew);
				obsProbOther = obsProbOther.sumOut(pOther.getStatesPrime());
				obsProbOther = obsProbOther.normalize();
				
				
				/*FactoredCondProbDD transitionProbOtherTemp =  pOther.getTransitionFunction().multiply(stateOfOtherWorld,retainVarsOther);
				transitionProbOtherTemp =  pOther.getObservationFunction().multiply(transitionProbOtherTemp,retainVarsOther);
				transitionProbOtherTemp = transitionProbOtherTemp.restrict(actMe)
				transitionProbOtherTemp = transitionProbOtherTemp.multiply(actionProbOther)
				transitionProbOtherTemp = transitionProbOtherTemp.sumOut(pOther.getStates())
				transitionProbOtherTemp = transitionProbOtherTemp.sumOut(pOther.getObservations())
				transitionProbOtherTemp = transitionProbOtherTemp.normalize()*/

				
				/*
				//Determine the observation distribution they will get according to your belief about the state of the world
				FactoredCondProbDD obsProbOther = transitionProbOtherTemp.sumOut(pOther.getStatesPrime());
				//Weigh the most likely beliefs they will take on based on the above distribution of observations
				FactoredCondProbDD newObsFn = pOther.getObservationFunction()
				newObsFn = newObsFn.restrict(actMe); 
				
				AlgebraicDD newAlgObsFn = newObsFn.multiply(obsProbOther.toProbabilityDD().getFunction());
				newAlgObsFn = newAlgObsFn.sumOut(pOther.getObservations());
				newAlgObsFn = newAlgObsFn.normalize();
				FactoredCondProbDD posteriorStateFilter = new FactoredCondProbDD(new CondProbDD(new ArrayList<DDVariable>(),new ArrayList<DDVariable>(newAlgObsFn.function.variables),newAlgObsFn));
				posteriorStateFilter = posteriorStateFilter.normalize().prime()
				*/
				
				//FactoredCondProbDD posteriorStateFilter = transitionProbOtherTemp;
				
				//Transition their belief according to their model of the world
				FactoredCondProbDD restrTransOther = pOther.getTransitionFunction().multiply(expectedBeliefOther, retainVarsOther);
				restrTransOther =  pOther.getObservationFunction().multiply(restrTransOther,retainVarsOther);
				restrTransOther = restrTransOther.multiply(actionProbOther)
				restrTransOther = restrTransOther.normalize()
				restrTransOther = restrTransOther.multiply(obsProbOther)
				//restrTransOther = restrTransOther.multiply(actionProbMeAccordingToOther); 
				//restrTransOther = restrTransOther.sumOut(pMe.getActions()).sumOut(pOther.getActions())
				//restrTransOther = restrTransOther.normalize()
				restrTransOther = restrTransOther.sumOut(pOther.getObservations(),false, true);
				
				
				restrTransOther = restrTransOther.normalize();
				FactoredCondProbDD expectedBeliefOtherOld = expectedBeliefOther;
				
				expectedBeliefOther = restrTransOther.unprime();
				
				//Weigh their posterior states according to the likely observations
				//expectedBeliefOther = restrTransOther.sumOut(pOther.getStates());
				//expectedBeliefOther = expectedBeliefOther.multiply(posteriorStateFilter);
				//expectedBeliefOther = expectedBeliefOther.normalize()
				//expectedBeliefOther = expectedBeliefOther.unprime()
				
				actDist = pol1.getActionDistribution(expectedBeliefOther)
				
				
				tempAct = new AlgebraicDD(pOther.getActions(),0.0d);
				actDist.each{ act, actProb ->
					tempAct = tempAct.plus(new AlgebraicDD(pOther.getActions(),1,act).multiply(actProb));
				}
				tempAct = tempAct.normalize()
				actionProbOther = new FactoredCondProbDD(new CondProbDD(new ArrayList<DDVariable>(),new ArrayList<DDVariable>(pOther.getActions()),tempAct));
				actionProbOther = actionProbOther.normalize()
				
				
				println obs 
				println actMe
				//println obsProbOther
				println actDist
				
			}
			
			
		then:
			println()
			println expectedBeliefOther
			println()
			println beliefMe
			println()
	}
	
	Random random = new Random();
	
	private HashMap<DDVariable,Integer> sampleSpacePoint(ArrayList<DDVariable> variables, FactoredCondProbDD probFn) {
		HashMap<DDVariable,Integer> point = new HashMap<DDVariable,Integer>();
		
		for(DDVariable variable:variables) {
			ArrayList<DDVariable> sumOutVars = new ArrayList<DDVariable>(variables);
			sumOutVars.remove(variable);
			
			def probTempFn = probFn.sumOut(sumOutVars)
			probTempFn = probTempFn.normalize()
			probTempFn = probTempFn.toProbabilityDD();
			
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
