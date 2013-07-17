package masg.dd.rbm

import masg.dd.AlgebraicDD
import masg.dd.operations.LogisticSigmoidOperation
import masg.dd.operations.RandomBinaryOperation

class RBMLayer {
	def visVars
	def hidVars
	AlgebraicDD weightFn
	
	AlgebraicDD recognitionWeightFn
	AlgebraicDD generativeWeightFn
	
	Random r = new Random()
	
	AlgebraicDD getGenerativeWeightFn() {
		generativeWeightFn?:weightFn
	}
	
	AlgebraicDD getRecognitionWeightFn() {
		recognitionWeightFn?:weightFn
	}
	
	def untieGenRecWeightFns() {
		generativeWeightFn = weightFn.multiply(1.0d)
		recognitionWeightFn = weightFn.multiply(1.0d)
	}
	
	def activateHiddenProbs(AlgebraicDD visFn) {
		AlgebraicDD hiddenActivationVector = getRecognitionWeightFn().multiply(visFn)
		hiddenActivationVector = hiddenActivationVector.sumOut(visVars)
		hiddenActivationVector = hiddenActivationVector.oper(new LogisticSigmoidOperation())
		
		hiddenActivationVector
	}
	
	def activateHidden(AlgebraicDD visFn) {
		AlgebraicDD hiddenActivationVector = activateHiddenProbs(visFn)
		hiddenActivationVector = hiddenActivationVector.oper(new RandomBinaryOperation())
		
		hiddenActivationVector
	} 
	
	def activateVisProbs(AlgebraicDD hidFn) {
		
		if(hidFn == null) {
			hidFn = new AlgebraicDD(hidVars, 0, {r.nextDouble()})
			//hidFn = hidFn.oper(new RandomBinaryOperation())
		}
		
		AlgebraicDD visibleActivationVector = getGenerativeWeightFn().multiply(hidFn)
		visibleActivationVector = visibleActivationVector.sumOut(hidVars)
		visibleActivationVector = visibleActivationVector.oper(new LogisticSigmoidOperation())
		
		visibleActivationVector
	}
	
	def activateVis(AlgebraicDD hidFn) {
		AlgebraicDD visibleActivationVector = activateVisProbs(hidFn)
		visibleActivationVector = visibleActivationVector.oper(new RandomBinaryOperation())
		
		visibleActivationVector
	}
	
	def updateCD(double learningRate, AlgebraicDD visible, AlgebraicDD hidden, AlgebraicDD visiblePrime, AlgebraicDD hiddenPrime) {
		
		AlgebraicDD posGradient = new AlgebraicDD(visVars + hidVars,1.0d)
		posGradient = posGradient.multiply( visible )
		posGradient = posGradient.multiply( hidden )
		
		AlgebraicDD negGradient = new AlgebraicDD(visVars + hidVars,1.0d)
		negGradient = negGradient.multiply( visiblePrime )
		negGradient = negGradient.multiply( hiddenPrime )
		
		AlgebraicDD update = posGradient.minus(negGradient)
		update = update.multiply(learningRate)
		
		
		double maxDiff = posGradient.maxAbsDiff(negGradient)
		AlgebraicDD diff = posGradient.minus(negGradient)
		diff = diff.multiply(diff)
		double dist = Math.sqrt(diff.totalWeight)
		
		weightFn = weightFn.plus(update)
		
		println "Diff: ${maxDiff}"
		println "Dist: ${dist}"
	} 
	
	def updateGenerative(double learningRate, AlgebraicDD visibleIn) {
		
		AlgebraicDD hiddenProbs = recognitionWeightFn.multiply(visibleIn)
		hiddenProbs = hiddenProbs.oper(new LogisticSigmoidOperation())
		
		AlgebraicDD hidden = hiddenProbs.oper(new RandomBinaryOperation())
		
		AlgebraicDD update = new AlgebraicDD(visVars + hidVars,1.0d)
		update = update.multiply(hidden.minus(hiddenProbs))
		update = update.multiply(visibleIn)
		update = update.multiply(learningRate)
		
		println "Gen update total: ${update.totalWeight}"
		
		generativeWeightFn = generativeWeightFn.plus(update)
	}
	
	def updateRecognition(double learningRate, AlgebraicDD hidden) {
		
		AlgebraicDD visibleProbs = generativeWeightFn.multiply(hidden)
		visibleProbs = visibleProbs.oper(new LogisticSigmoidOperation())
		
		AlgebraicDD visible = visibleProbs.oper(new RandomBinaryOperation())
		
		AlgebraicDD update = new AlgebraicDD(visVars + hidVars,1.0d)
		update = update.multiply(visible.minus(visibleProbs))
		update = update.multiply(hidden)
		update = update.multiply(learningRate)
		
		println "Rec update total: ${update.totalWeight}"
		recognitionWeightFn = recognitionWeightFn.plus(update)
	}
	
	
}
