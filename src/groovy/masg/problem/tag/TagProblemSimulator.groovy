package masg.problem.tag

import java.text.SimpleDateFormat
import java.util.Random;

import masg.dd.FactoredCondProbDD
import masg.dd.ProbDD
import masg.dd.pomdp.agent.belief.POMDPBelief
import masg.dd.pomdp.agent.policy.AlphaVectorPolicy
import masg.dd.pomdp.agent.policy.FSCPolicy
import masg.dd.pomdp.agent.policy.Policy
import masg.dd.pomdp.agent.policy.serialization.AlphaVectorPolicyWriter
import masg.dd.variables.DDVariable
import masg.dd.variables.DDVariableSpace
import masg.problem.tag.simulator.TagAgent
import masg.problem.tag.simulator.TagGrid
import masg.problem.tag.simulator.TagSimulationRecorder
import masg.problem.tag.simulator.TagWumpus

class TagProblemSimulator {
	
	DDVariable a1_row = new DDVariable("a1_row",5)
	DDVariable a1_col = new DDVariable("a1_col",5)
	DDVariable a2_row = new DDVariable("a2_row",5)
	DDVariable a2_col = new DDVariable("a2_col",5)
	DDVariable w_row = new DDVariable("w_row",5)
	DDVariable w_col = new DDVariable("w_col",5)
	
	DDVariable a1_row_loc = new DDVariable("a1_row_loc",5)
	DDVariable a1_col_loc = new DDVariable("a1_col_loc",5)
	DDVariable w_pres = new DDVariable("w_pres",2)
	
	Random random = new Random();
	
	private String filePath = System.getProperty("user.dir") + "/experiments/tagproblem/runs/" +  new SimpleDateFormat("MM-dd-yy hh.mm.ss.SS a").format(new Date())
	
	public int simulate(TagProblemModel problem, Policy pol1, Policy pol2, int numTrials, int numSteps, List<TagSimulationRecorder> simRecorders = []) {
		
		new File(System.getProperty("user.dir") + "/experiments").mkdir()
		new File(System.getProperty("user.dir") + "/experiments/tagproblem").mkdir()
		new File(System.getProperty("user.dir") + "/experiments/tagproblem/runs").mkdir()
		new File(filePath).mkdir()
		new File(filePath + "/policy").mkdir()
		
		if(pol1 instanceof AlphaVectorPolicy) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath + "/policy/runPolicy.policy",false));
			AlphaVectorPolicyWriter policyWriter = new AlphaVectorPolicyWriter(pol1);
			policyWriter.write(writer);
			writer.flush();
			writer.close();
		}
		
		
		simRecorders.each{ 
			it.initialize(problem, pol1, filePath)
			it.startSimulation()
		}
		
		int numColocations = 0;
		int totalColocations = 0;
		
		
		numTrials.times { trialIx ->
			
			simRecorders.each{
				it.startTrial()
			}
			
			if(pol1 instanceof FSCPolicy) {
				pol1.reset()
			}
			
			if(pol2 instanceof FSCPolicy) {
				pol2.reset()
			}
			
			ArrayList<HashMap<DDVariable,Integer>> actions = [];
			ArrayList<HashMap<DDVariable,Integer>> observations = [];
			
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filePath + "/" + (trialIx+1) + ".trial"))
			
			
			
			TagAgent agent1 = new TagAgent(new POMDPBelief(problem.getPOMDP(),problem.getPOMDP().initialBelief))
			TagAgent agent2 = new TagAgent(new POMDPBelief(problem.getPOMDP(),problem.getPOMDP().initialBelief))
			TagWumpus wumpus = new TagWumpus(problem.getPOMDP())
			TagGrid grid = new TagGrid(5,5, agent1, agent2, wumpus)
			
			numColocations = 0;
			numSteps.times {
				
				
				HashMap<DDVariable,Integer> actualStateAgt1 = new HashMap<DDVariable,Integer>()
				actualStateAgt1[a1_row] = agent1.row
				actualStateAgt1[a1_col] = agent1.column
				actualStateAgt1[a2_row] = agent2.row
				actualStateAgt1[a2_col] = agent2.column
				actualStateAgt1[w_row] = wumpus.row
				actualStateAgt1[w_col] = wumpus.column
				
				HashMap<DDVariable,Integer> actualStateAgt2 = new HashMap<DDVariable,Integer>()
				actualStateAgt2[a1_row] = agent2.row
				actualStateAgt2[a1_col] = agent2.column
				actualStateAgt2[a2_row] = agent1.row
				actualStateAgt2[a2_col] = agent1.column
				actualStateAgt2[w_row] = wumpus.row
				actualStateAgt2[w_col] = wumpus.column
				
				String strInfo = "STEP #$it\n"
				strInfo += "=============================================================\n"
				
				strInfo += "\n"
				strInfo += "\tActual State\n"
				strInfo += "\t============\n"
				
				StringWriter sw = new StringWriter()
				grid.draw(new BufferedWriter(sw))
				
				strInfo += sw.toString() + "\n"
				
				strInfo += "\tWumpus: row = ${wumpus.row}, col = ${wumpus.column} \n"
				strInfo += "\tAgent1: row = ${agent1.row}, col = ${agent1.column} \n"
				strInfo += "\tAgent2: row = ${agent2.row}, col = ${agent2.column} \n"
				
				DDVariableSpace space = new DDVariableSpace(new ArrayList<DDVariable>([w_row,w_col]))
				
				
				FactoredCondProbDD summedOutBelief1 = agent1.currBelief.getBeliefFunction().sumOut(new ArrayList<DDVariable>([a1_row,a1_col,a2_row,a2_col]))
				
				strInfo += "\n"
				strInfo += "\tAgent1 Believes\n"
				strInfo += "\t=============\n"
				
				space.each{ pt ->
					strInfo += "\t$pt = ${Math.round(summedOutBelief1.getValue(pt)*100000)/100000}\n"
				}
				
				
				if((wumpus.row==agent1.row && wumpus.column==agent1.column) || (wumpus.row==agent2.row && wumpus.column==agent2.column)) {
					++numColocations;
				}
				
				strInfo += "\n"
				strInfo += "\tAccumulated Reward\n"
				strInfo += "\t=======\n"
				strInfo += "\tColocations: $numColocations \n"
				
				
				HashMap<DDVariable,Integer> action1 = pol1.getAction(agent1.currBelief);
				HashMap<DDVariable,Integer> action2 = pol2.getAction(agent2.currBelief);
				
				strInfo += "\n"
				strInfo += "\tTaking Action\n"
				strInfo += "\t======\n"
				strInfo += "\tAgent1: $action1 \n"
				strInfo += "\tAgent2: $action2 \n"
				
				
				
				FactoredCondProbDD restrTransFn1 = problem.getPOMDP().getTransitionFunction().restrict(action1)
				restrTransFn1 = restrTransFn1.restrict(actualStateAgt1)
				restrTransFn1 = restrTransFn1.normalize();
				
				
				FactoredCondProbDD restrTransFn2 = problem.getPOMDP().getTransitionFunction().restrict(action2)
				restrTransFn2 = restrTransFn2.restrict(actualStateAgt2)
				restrTransFn2 = restrTransFn2.normalize();
				
				HashMap<DDVariable,Integer> actualStateAgt1New = sampleSpacePoint(problem.getPOMDP().getStatesPrime(), restrTransFn1)
				HashMap<DDVariable,Integer> actualStateAgt2New = sampleSpacePoint(problem.getPOMDP().getStatesPrime(), restrTransFn2)
				
				wumpus.moveRandomly(5, 5)
				
				actualStateAgt1New[w_row.getPrimed()] = wumpus.row
				actualStateAgt1New[w_col.getPrimed()] = wumpus.column
				
				actualStateAgt2New[w_row.getPrimed()] = wumpus.row
				actualStateAgt2New[w_col.getPrimed()] = wumpus.column
				
				
				HashMap<DDVariable,Integer> actualStateNewAg1Primed = new HashMap<DDVariable,Integer>();
				actualStateNewAg1Primed[a1_row.getPrimed()] = actualStateAgt1New[a1_row.getPrimed()]
				actualStateNewAg1Primed[a1_col.getPrimed()] = actualStateAgt1New[a1_col.getPrimed()]
				actualStateNewAg1Primed[a2_row.getPrimed()] = actualStateAgt2New[a1_row.getPrimed()]
				actualStateNewAg1Primed[a2_col.getPrimed()] = actualStateAgt2New[a1_col.getPrimed()]
				actualStateNewAg1Primed[w_row.getPrimed()] = wumpus.row
				actualStateNewAg1Primed[w_col.getPrimed()] = wumpus.column
				
				HashMap<DDVariable,Integer> actualStateNewAg2Primed = new HashMap<DDVariable,Integer>();
				actualStateNewAg2Primed[a1_row.getPrimed()] = actualStateAgt2New[a1_row.getPrimed()]
				actualStateNewAg2Primed[a1_col.getPrimed()] = actualStateAgt2New[a1_col.getPrimed()]
				actualStateNewAg2Primed[a2_row.getPrimed()] = actualStateAgt1New[a1_row.getPrimed()]
				actualStateNewAg2Primed[a2_col.getPrimed()] = actualStateAgt1New[a1_col.getPrimed()]
				actualStateNewAg2Primed[w_row.getPrimed()] = wumpus.row
				actualStateNewAg2Primed[w_col.getPrimed()] = wumpus.column
				
				
				FactoredCondProbDD restrObsFn1 = problem.getPOMDP().getObservationFunction().restrict(action1)
				restrObsFn1 = restrObsFn1.restrict(actualStateNewAg1Primed)
				restrObsFn1 = restrObsFn1.normalize()
				
				FactoredCondProbDD restrObsFn2 = problem.getPOMDP().getObservationFunction().restrict(action2)
				restrObsFn2 = restrObsFn2.restrict(actualStateNewAg2Primed)
				restrObsFn2 = restrObsFn2.normalize()
				
				HashMap<DDVariable,Integer> obs1 = sampleSpacePoint(problem.getPOMDP().getObservations(), restrObsFn1);
				HashMap<DDVariable,Integer> obs2 = sampleSpacePoint(problem.getPOMDP().getObservations(), restrObsFn2);
				
				if(pol1 instanceof FSCPolicy) {
					pol1.update(obs1)
				}
				
				if(pol2 instanceof FSCPolicy) {
					pol2.update(obs2)
				}
				
				simRecorders.each{
					it.step(grid, action1, obs1)
				}
				
				strInfo += "\n"
				strInfo += "\tWill Get Observation\n"
				strInfo += "\t===========\n"
				strInfo += "\tAgent1: $obs1 \n"
				strInfo += "\tAgent2: $obs2 \n"
				
				println strInfo
				
				fileWriter.write(strInfo)
				fileWriter.newLine()

				if(agent1.currBelief instanceof POMDPBelief) {
					POMDPBelief b = agent1.currBelief
					agent1.currBelief = b.getNextBelief(action1, obs1)
				}
				agent1.row = actualStateNewAg1Primed[a1_row.getPrimed()]
				agent1.column = actualStateNewAg1Primed[a1_col.getPrimed()]
				
				if(agent2.currBelief instanceof POMDPBelief) {
					POMDPBelief b = agent2.currBelief
					agent2.currBelief = b.getNextBelief(action2, obs2)
				}
				
				agent2.row = actualStateNewAg2Primed[a1_row.getPrimed()]
				agent2.column = actualStateNewAg2Primed[a1_col.getPrimed()]
			}
			
			
			totalColocations+=numColocations;
			
			fileWriter.flush();
			fileWriter.close();
			
			fileWriter = new BufferedWriter(new FileWriter(filePath + "/trial" + (trialIx+1) + "Summary.summary"))
			
			String summary = "Total colocations: $numColocations \n"
			
			println summary
			
			fileWriter.write(summary)
			fileWriter.newLine()
			
			fileWriter.flush();
			fileWriter.close();
			
			simRecorders.each{
				it.endTrial()
			}
		}
		
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filePath + "/runSummary.summary"))
		
		String summary = "Total colocations: $totalColocations \n"
		summary += "Total trials: $numTrials "
		summary += "Steps per trial: $numSteps) "
		
		println summary
		
		fileWriter.write(summary)
		fileWriter.newLine()
		
		fileWriter.flush();
		fileWriter.close();
		
		simRecorders.each{
			it.endSimulation()
		}
		return totalColocations;
	}
	
	private HashMap<DDVariable,Integer> sampleSpacePoint(ArrayList<DDVariable> variables, FactoredCondProbDD probFn) {
		HashMap<DDVariable,Integer> point = new HashMap<DDVariable,Integer>();
		
		for(DDVariable variable:variables) {
			ArrayList<DDVariable> sumOutVars = new ArrayList<DDVariable>(variables);
			sumOutVars.remove(variable);
			
			def temp = probFn.sumOut(sumOutVars)
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
