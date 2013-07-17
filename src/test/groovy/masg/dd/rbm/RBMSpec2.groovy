package masg.dd.rbm

import java.util.Random;

import masg.dd.AlgebraicDD
import masg.dd.operations.LogisticSigmoidOperation
import masg.dd.operations.RandomBinaryOperation
import masg.dd.pomdp.POMDP
import masg.dd.pomdp.agent.belief.BeliefRegion
import masg.dd.pomdp.agent.policy.RandomPolicy
import masg.dd.variables.DDVariable
import masg.problem.tag.TagProblemPOMDP;
import spock.lang.Specification

class RBMSpec2 extends Specification{
	int gridHeight = 5, gridWidth = 5
	int numNeurons = 5
	
	TagProblemPOMDP p = new TagProblemPOMDP()
	
	Random r = new Random()
	def "rbm fun"() {
		when:
		
		int numSamples = 100
		POMDP p = p.getPOMDP()
		BeliefRegion reg = new BeliefRegion(numSamples,100,p,new RandomPolicy(p))
		
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
		DDVariable neur13 = new DDVariable(0,"neur13",2)
		DDVariable neur14 = new DDVariable(0,"neur14",2)
		DDVariable neur15 = new DDVariable(0,"neur15",2)
		DDVariable neur16 = new DDVariable(0,"neur16",2)
		DDVariable neur17 = new DDVariable(0,"neur17",2)
		DDVariable neur18 = new DDVariable(0,"neur18",2)
		DDVariable neur19 = new DDVariable(0,"neur19",2)
		DDVariable neur20 = new DDVariable(0,"neur20",2)
		DDVariable neur21 = new DDVariable(0,"neur21",2)
		
		DDVariable row = new DDVariable(0,"a1_row", gridHeight)
		DDVariable rowPrime = new DDVariable(0,"a1_row'", gridHeight)
		DDVariable col = new DDVariable(0,"a1_col", gridWidth)
		DDVariable colPrime = new DDVariable(0,"a1_col'", gridWidth)
		
		DDVariable a2row = new DDVariable(0,"a2_row", gridHeight)
		DDVariable a2rowPrime = new DDVariable(0,"a2_row'", gridHeight)
		DDVariable a2col = new DDVariable(0,"a2_col", gridWidth)
		DDVariable a2colPrime = new DDVariable(0,"a2_col'", gridWidth)
		
		DDVariable wRow = new DDVariable(0,"w_row", gridHeight)
		DDVariable wRowPrime = new DDVariable(0,"w_row'", gridHeight)
		DDVariable wCol = new DDVariable(0,"w_col", gridWidth)
		DDVariable wColPrime = new DDVariable(0,"w_col'", gridWidth)
		
		Random r = new Random()
		def trainingExamples = []
		
		/* States prime */
		/*def visVars1 = [rowPrime, colPrime, wRowPrime, wColPrime]

		for(int i=1;i<reg.beliefSamples.size();++i) {
			AlgebraicDD dd = new AlgebraicDD(visVars1 - p.getStates(),1.0d)
			dd = reg.beliefSamples[i].beliefFn.prime().multiply(dd).normalize()
			
			trainingExamples << dd
		}*/
		
		/* States */
		/*def visVars1 = [row, col, wRow, wCol]
		
		for(int i=1;i<reg.beliefSamples.size();++i) {
			AlgebraicDD dd = new AlgebraicDD(visVars1 - p.getStatesPrime(),1.0d)
			dd = reg.beliefSamples[i-1].beliefFn.multiply(dd).normalize()
			
			trainingExamples << dd
		}*/
		
		/*Action observation*/
		def visVars1 = p.getObservations() + p.getActions()
		/*for(int i=1;i<reg.beliefSamples.size();++i) {
			AlgebraicDD ddObs = new AlgebraicDD(p.getObservations(),0,reg.observations[i-1])
			AlgebraicDD ddAct = new AlgebraicDD(p.getActions(),0,reg.actions[i-1])
			
			AlgebraicDD dd = new AlgebraicDD(visVars1,1.0d)
			
			dd = dd.multiply(ddObs)
			dd = dd.multiply(ddAct)
			
			trainingExamples << dd
		}*/
		
		def hidVars1 = [neur1, neur2, neur3, neur4, neur5, neur6, neur7, neur19]
		def hidVars2 = [neur8, neur9, neur10, neur11, neur12, neur13, neur20]
		def hidVars3 = [neur14, neur15, neur16, neur17, neur18, neur21]
		
		AlgebraicDD exciteMatrix = new AlgebraicDD(p.actions + p.observations, 0, {1.0d/(r.nextGaussian()*10.0d)})
		
		AlgebraicDD inhibitMatrix = new AlgebraicDD(p.actions + p.actions.collect{it.getPrimed()} + p.observations + p.observations.collect{it.getPrimed()}, 0, {1.0d/(r.nextGaussian()*10.0d)})
		
		for(int i=1;i<reg.beliefSamples.size();++i) {
			AlgebraicDD ddObs = new AlgebraicDD(p.getObservations(),0,reg.observations[i-1])
			AlgebraicDD ddAct = new AlgebraicDD(p.getActions(),0,reg.actions[i-1])
			
			AlgebraicDD exciteProb = exciteMatrix.multiply(ddAct).sumOut(ddAct.variables)
			exciteProb = exciteProb.oper(new LogisticSigmoidOperation())
			
			AlgebraicDD inhibitProb = inhibitMatrix.multiply(ddAct).multiply(ddAct.prime()).multiply(exciteProb).multiply(exciteProb.prime())
			inhibitProb = inhibitMatrix.sumOut(ddObs.variables + ddAct.variables).unprime().sumOut(ddAct.variables)
			inhibitProb = inhibitProb.oper(new LogisticSigmoidOperation())
			
			AlgebraicDD notInhibitProb = new AlgebraicDD(ddObs.variables,1.0d)
			notInhibitProb = notInhibitProb.minus(inhibitProb)
			
			AlgebraicDD fireProb = exciteProb.multiply(notInhibitProb)
			
			AlgebraicDD exciteUpdate = new AlgebraicDD(p.actions + p.observations, 0.0d)
			AlgebraicDD diff = ddObs.minus(exciteProb)
			exciteUpdate = exciteUpdate.plus(diff)
			exciteMatrix = exciteMatrix.plus(exciteUpdate)
			
			AlgebraicDD temp = new AlgebraicDD(inhibitMatrix.variables, 1.0d)
			temp = temp.multiply(ddAct)
			temp = temp.multiply(ddObs)
			temp = temp.multiply(ddAct.prime())
			temp = temp.multiply(ddObs.prime())
			temp = temp.normalize()
			
			AlgebraicDD inhibitUpdate = new AlgebraicDD(inhibitMatrix.variables, 0.0d)
			
			AlgebraicDD notCofiring = new AlgebraicDD(inhibitMatrix.variables, 1.0d)
			notCofiring = notCofiring.minus( temp)
			
			AlgebraicDD posUpdate = notCofiring.multiply(exciteProb).multiply(exciteProb.prime())
			AlgebraicDD negUpdate = temp.multiply(inhibitProb)
			negUpdate = negUpdate.multiply(inhibitProb.prime())
			
			inhibitUpdate = inhibitUpdate.plus( posUpdate )
			inhibitUpdate = inhibitUpdate.minus( negUpdate )
			inhibitMatrix = inhibitMatrix.plus( inhibitUpdate )
			
			AlgebraicDD ddObsNew = fireProb.normalize()
			AlgebraicDD dist = ddObsNew.minus(ddObs)
			dist = dist.multiply(dist).sumOut(ddObs.variables)
			
			println dist.totalWeight
			
			if(i > 90) {
				println "test"
			}
		}
		
		/*RBM rbm = new RBM()
		
		AlgebraicDD layer1Weights = new AlgebraicDD( visVars1 + hidVars1, 0, {1.0d/(r.nextGaussian()*10.0d)} )
		RBMLayer layer = new RBMLayer( visVars:visVars1, hidVars:hidVars1, weightFn: layer1Weights )
		rbm.layers << layer
		
		AlgebraicDD layer2Weights = new AlgebraicDD( hidVars1 + hidVars2, 0, {1.0d/(r.nextGaussian()*10.0d)} )
		layer = new RBMLayer( visVars:hidVars1, hidVars:hidVars2, weightFn: layer2Weights )
		rbm.layers << layer
		
		AlgebraicDD layer3Weights = new AlgebraicDD( hidVars2 + hidVars3, 0, {1.0d/(r.nextGaussian()*10.0d)} )
		layer = new RBMLayer( visVars:hidVars2, hidVars:hidVars3, weightFn: layer3Weights )
		rbm.layers << layer
		
		rbm.trainCases = [trainingExamples, [], []]
		
		rbm.update()
		
		DDVariable actVar = p.getActions()[0]
		DDVariable wpresVar = new DDVariable(0,"w_pres",2)
		HashMap point = new HashMap()
		point.put(actVar, 0)
		point.put(wpresVar, 0)
		
		AlgebraicDD input = new AlgebraicDD(visVars1,1.0d)
		input = input.normalize()
		input = input.plus(new AlgebraicDD([actVar,wpresVar],0,point))
		
		AlgebraicDD dd
		100.times {
			println "Sampling reconstruction #$it"
			if(dd) {
				dd = dd.plus(rbm.getValue(input))
			} else {
				dd = rbm.getValue(input)
			}
			
		}
		
		//dd = dd.normalize()
		
		
		dd = dd.restrict(point)
		println dd.normalize()*/
		then:
			true
	}
}
