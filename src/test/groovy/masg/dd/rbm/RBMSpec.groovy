package masg.dd.rbm

import java.util.List;

import masg.dd.AlgebraicDD
import masg.dd.CondProbDD
import masg.dd.operations.LogisticSigmoidOperation
import masg.dd.pomdp.POMDP
import masg.dd.pomdp.agent.belief.BeliefRegion
import masg.dd.pomdp.agent.policy.RandomPolicy
import masg.dd.representation.DDElement
import masg.dd.representation.serialization.DDElementReader
import masg.dd.representation.serialization.DDElementWriter
import masg.dd.variables.DDVariable
import masg.dd.variables.DDVariableSpace
import spock.lang.Specification
import masg.dd.context.DDContext
import masg.dd.dbn.POMDPBayesianNetworkStep
import masg.problem.tag.TagProblemPOMDP

class RBMSpec extends Specification {
	int gridHeight = 5, gridWidth = 5
	int numNeurons = 5
	
	TagProblemPOMDP p = new TagProblemPOMDP()
	
	Random r = new Random()
	def "rbm fun"() {
		when:
		
		POMDP p = p.getPOMDP()
		BeliefRegion reg = new BeliefRegion(1000,100,p,new RandomPolicy(p))
		
		DDVariable neur1 = new DDVariable(0,"neur1",2)
		DDVariable neur2 = new DDVariable(0,"neur2",2)
		DDVariable neur3 = new DDVariable(0,"neur3",2)
		DDVariable neur4 = new DDVariable(0,"neur4",2)
		DDVariable neur5 = new DDVariable(0,"neur5",2)
		DDVariable neur6 = new DDVariable(0,"neur6",2)
		DDVariable neur7 = new DDVariable(0,"neur7",2)
		DDVariable neur8 = new DDVariable(0,"neur8",2)
		DDVariable neur9 = new DDVariable(0,"neur9",2)
		DDVariable neur10 = new DDVariable(0,"neur10",2)
		DDVariable neur11 = new DDVariable(0,"neur11",2)
		DDVariable neur12 = new DDVariable(0,"neur12",2)
		
		DDVariable row = new DDVariable(0,"a1_row", gridHeight)
		DDVariable rowPrime = new DDVariable(0,"a1_row'", gridHeight)
		DDVariable col = new DDVariable(0,"a1_col", gridWidth)
		DDVariable colPrime = new DDVariable(0,"a1_col'", gridWidth)
		
		DDVariable wRow = new DDVariable(0,"w_row", gridHeight)
		DDVariable wRowPrime = new DDVariable(0,"w_row'", gridHeight)
		DDVariable wCol = new DDVariable(0,"w_col", gridWidth)
		DDVariable wColPrime = new DDVariable(0,"w_col'", gridWidth)
		
		def visVars1 = [row, col, wRow, wCol, rowPrime, colPrime, wRowPrime, wColPrime] 
		def visVars2 = p.getActions()
		
		def hidVars1 = [neur1, neur2, neur3, neur4, neur5]
		def hidVars2 = [neur6, neur7, neur8, neur9, neur10]
		
		BufferedReader reader
		DDElementReader elReader
		
		
		String filePath = System.getProperty("user.dir") + "/weights-"+ visVars1.collect{it.name}.join("_") + ".weights"
		
		def trainingExamples1 = []
		AlgebraicDD divisor
		
		for(int i=1;i<reg.beliefSamples.size();++i) { 
			AlgebraicDD dd = new AlgebraicDD(visVars1,1.0d)
			dd = reg.beliefSamples[i-1].beliefFn.multiply(dd)
			dd = reg.beliefSamples[i].beliefFn.prime().multiply(dd)
			
			divisor = dd.sumOut(p.getStatesPrime())
			dd = dd.div(divisor)
			
			trainingExamples1 << dd
		}
		
		def trainingExamples2 = []
		for(int i=1;i<reg.beliefSamples.size();++i) {
			AlgebraicDD dd = new AlgebraicDD(visVars2,0,reg.actions[i-1])
			trainingExamples2 << dd
		}

		DDVariableSpace visibleVariableSpace = new DDVariableSpace(visVars1)
		DDVariableSpace hiddenVariableSpace = new DDVariableSpace(hidVars1)
		
		AlgebraicDD weightMatrix1
		AlgebraicDD weightMatrix2
		
		weightMatrix2 = new AlgebraicDD(visVars2 + hidVars1 + hidVars2,0.0d)
		
		Closure hiddenMatrixClosure = {Map variables -> (1.0d + r.nextGaussian())/2.0d}
		AlgebraicDD hiddenMatrix = new AlgebraicDD(hidVars1,0,hiddenMatrixClosure)
		
		if(!new File(filePath).exists()) {
			//Closure weightMatrixClosure = {Map variables -> (r.nextGaussian())/10.0d}
			//weightMatrix = new AlgebraicDD(visVars1 + hidVars1,0,weightMatrixClosure)
			weightMatrix1 = new AlgebraicDD(visVars1 + hidVars1,0.0d)
		}
		else {
			reader = new BufferedReader(new FileReader(filePath));
			elReader = new DDElementReader(reader)
			weightMatrix1 = new AlgebraicDD(elReader.read(0))
		}

		def totError = 0.0d
		
		def layerNum = -1
		
		def learningRate = 0.9d
		2.times { time ->
			Collections.shuffle(trainingExamples1, new Random())
			trainingExamples1.eachWithIndex{ AlgebraicDD visibleVector, ix ->
				
				def trainVector = visibleVector.sumOut(p.statesPrime)
				
				AlgebraicDD result = weightMatrix1.multiply(trainVector).sumOut(hidVars1)
				result = result.oper(new LogisticSigmoidOperation())
				
				divisor = result.sumOut(p.statesPrime)
				result = result.div(divisor)
				
				def expect = visibleVector.sumOut(p.states).normalize()
				
				def predBefore = result.multiply(trainVector).sumOut( p.states).normalize()
				
				println "$time > $ix"
				//println "EXPECT:"
				//println expect
				
				//println "BEFORE:"
				//println predBefore
					
				AlgebraicDD hiddenActivationVector = weightMatrix1.multiply(visibleVector)
				hiddenActivationVector = hiddenActivationVector.sumOut(visVars1)
				
				if(layerNum == 1) {
					visibleVector = new AlgebraicDD(visVars2 + hidVars1,1.0d).multiply(hiddenActivationVector).multiply(trainingExamples2[ix])
					hiddenActivationVector = weightMatrix2.multiply(visibleVector)
					hiddenActivationVector = hiddenActivationVector.sumOut(visVars2 + hidVars1)
				}
				
				hiddenActivationVector = hiddenActivationVector.oper(new LogisticSigmoidOperation())
				
				
				AlgebraicDD posGradient = new AlgebraicDD(visVars1 + hidVars1,1.0d)
				posGradient = posGradient.multiply(visibleVector)
				posGradient = posGradient.multiply(hiddenActivationVector)

				AlgebraicDD visibleActivationVector
				
				/*if(layerNum == 0) {
					
				}*/
				
				visibleActivationVector = weightMatrix1.multiply(hiddenActivationVector)
				visibleActivationVector = visibleActivationVector.sumOut(hidVars1)
				visibleActivationVector = visibleActivationVector.oper(new LogisticSigmoidOperation())
				
				AlgebraicDD hiddenActivationVectorPrime = weightMatrix1.multiply(visibleActivationVector)
				hiddenActivationVectorPrime = hiddenActivationVectorPrime.sumOut(visVars1)
				hiddenActivationVectorPrime = hiddenActivationVectorPrime.oper(new LogisticSigmoidOperation())
				
				AlgebraicDD negGradient = new AlgebraicDD(visVars1 + hidVars1,1.0d)
				negGradient = negGradient.multiply(hiddenActivationVectorPrime)
				negGradient = negGradient.multiply(visibleActivationVector)
				
				AlgebraicDD update = posGradient.minus(negGradient)
				update = update.multiply(learningRate)
				
				weightMatrix1 = weightMatrix1.plus(update)
				
				
				result = weightMatrix1.multiply(trainVector).sumOut(hidVars1)
				result = result.oper(new LogisticSigmoidOperation())
				
				divisor = result.sumOut(p.statesPrime)
				result = result.div(divisor)
				
				AlgebraicDD predNow = result.multiply(trainVector).sumOut(/*p.observations + p.actions +*/ p.states).normalize()
				
				//println "AFTER:"
				//println predNow
				
				AlgebraicDD diff = predNow.minus(predBefore)
				diff = diff.multiply(diff)
				
				double dist = diff.totalWeight
				
				totError += dist
				println "distance: ${dist}"
				
				println()
				println()
			}
			
			learningRate *= 0.1d
		}
		
		println "Total dist: $totError"
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath,false));
		DDElementWriter elWriter = new DDElementWriter(weightMatrix1.function)
		elWriter.write(writer)
		writer.flush()
		writer.close()
		
		then:
			true
	}
	
	private sample(List<AlgebraicDD> fns, def vars = []) {
		def sample = [:]
		
		fns.each { AlgebraicDD dd ->
			def varsTemp = dd.variables
			
			if(vars) {
				varsTemp = vars.intersect(dd.variables)
			}
			
			varsTemp.each { DDVariable varCurr ->
				double thresh = r.nextDouble()
				double accumProb = 0.0d
				int varVal = 0
				
				AlgebraicDD singleVarDD = dd.sumOut(dd.variables - varCurr)
				
				while(accumProb <= thresh && varVal < varCurr.numValues) {
					sample[varCurr] = varVal
					accumProb += singleVarDD.getValue(sample)
					varVal ++
				}
				
			}
			
		}
		
		sample
	}
}
