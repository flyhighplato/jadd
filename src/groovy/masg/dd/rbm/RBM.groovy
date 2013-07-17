package masg.dd.rbm

import masg.dd.AlgebraicDD
import masg.dd.operations.LogisticSigmoidOperation
import masg.dd.operations.RandomBinaryOperation
import masg.dd.variables.DDVariable

class RBM {
	List<RBMLayer> layers = []
	List trainCases = []
	List condVars = []
	
	def learningRate = 0.9d
	def learningRateMult = 0.1d
	
	def augmentWithTrainingCase(AlgebraicDD fn, int layerNum, int trainCaseIx) {
		AlgebraicDD temp = new AlgebraicDD(layers[layerNum].visVars,1.0d)
		
		if(fn) {
			temp = temp.multiply(fn)
		}
		if(trainCases[layerNum + 1]) {
			temp = temp.multiply(trainCases[layerNum + 1][trainCaseIx])
		}
		
		return temp
	}
	
	def getValue(input) {
		
		AlgebraicDD visibleIn = layers[0].activateHidden(input)
		(1..<layers.size()).each { ix ->
			
			visibleIn = visibleIn.sumOut(visibleIn.variables - layers[ix].visVars)
			visibleIn = layers[ix].activateHidden(visibleIn)
			
			
		}
		
		//visibleIn = visibleIn.sumOut(visibleIn.variables - layers[0].visVars)
		AlgebraicDD visibleOut = visibleIn
		
		((layers.size()-1)..0).each { ix ->
			visibleOut = layers[ix].activateVis(visibleOut)
			//visibleOut = visibleOut.sumOut(visibleOut.variables - layers[ix].hidVars)
			
		}
		
		visibleOut
	}
	
	def generateVisible() {
		Random r = new Random()
		int maxLayerIx = layers.size() - 1
		AlgebraicDD hiddenIn = new AlgebraicDD(layers[maxLayerIx].hidVars, 0, {r.nextDouble()})
		//hiddenIn = hiddenIn.oper(new RandomBinaryOperation())
		
		(maxLayerIx..0).each { ix ->
			hiddenIn = hiddenIn.sumOut(hiddenIn.variables - layers[ix].hidVars)
			
			//if(ix < 0) {
				hiddenIn = layers[ix].activateVis(hiddenIn)
			//}
			//else {
				//hiddenIn = layers[ix].activateVisProbs(hiddenIn)
			//}
		}
		
		hiddenIn
	}
	
	def updateLayerRBM(double learningRate, int layerIx, int trainCaseIx) {
		
		AlgebraicDD visibleIn = trainCases[0][trainCaseIx]
		
		(0..<layerIx).each { ix ->
			visibleIn = layers[ix].activateHiddenProbs(visibleIn)
			//visibleIn = visibleIn.sumOut(visibleIn.variables-layers[ix].hidVars)
			//visibleIn = augmentWithTrainingCase(visibleIn, ix, trainCaseIx)
			
		}
		
		AlgebraicDD activatedHidden = layers[layerIx].activateHiddenProbs(visibleIn)
		
		AlgebraicDD reflectedVisibleIn = visibleIn
		AlgebraicDD reflectedActivatedHidden = activatedHidden
		
		1.times {
			reflectedVisibleIn = layers[layerIx].activateVisProbs(reflectedActivatedHidden)
			reflectedVisibleIn = reflectedVisibleIn.normalize()
			reflectedActivatedHidden = layers[layerIx].activateHiddenProbs(reflectedVisibleIn)
		}
		
		/*double multiplier = 1.0d;
		layers[layerIx].visVars.each { DDVariable v ->
			multiplier *= v.numValues
		}
		reflectedVisibleIn = reflectedVisibleIn.minus(visibleIn).multiply(multiplier)*/
		
		
		layers[layerIx].updateCD(learningRate, visibleIn, activatedHidden, reflectedVisibleIn, reflectedActivatedHidden)
		
	}
	
	def update() {
		double tempLearningRate = learningRate
		
		
		(0..<layers.size()).each { layerIx ->
			10.times { time ->
				(0..<trainCases[0].size()).each { trainCaseIx ->
					println "($time) rbm: $layerIx -> $trainCaseIx"
					updateLayerRBM( learningRate, layerIx, trainCaseIx )
				}
			}	
		}
		
		(0..<layers.size()).each { layerIx ->
			layers[layerIx].untieGenRecWeightFns()
		}
		
		/*3.times { time ->
			(0..<layers.size()).each { layerIx ->
				(0..<trainCases[0].size()).each { trainCaseIx ->
					println "$time generative: $layerIx -> $trainCaseIx"
					
					AlgebraicDD visibleIn = trainCases[layerIx][trainCaseIx]
					visibleIn = augmentWithTrainingCase(visibleIn, layerIx, trainCaseIx)
					
					(0..<layerIx).each { ix ->
						visibleIn = layers[ix].activateHidden(visibleIn)
						visibleIn = augmentWithTrainingCase(visibleIn, layerIx, trainCaseIx)
					}
					
					layers[layerIx].updateGenerative(tempLearningRate, visibleIn)
				}
			}
			
			((layers.size()-1)..0).each { layerIx ->
				(0..<trainCases[0].size()).each { trainCaseIx ->
					println "$time recognition: $layerIx -> $trainCaseIx"
					
					AlgebraicDD hidden = layers[layerIx].activateVis()
					((layers.size()-1)..<layerIx).each { ix ->
						hidden = layers[ix].activateVis(hidden)
					}
					layers[layerIx].updateRecognition(tempLearningRate, hidden)
				}
			}
			
			tempLearningRate *= learningRate
		}*/
	}
}
