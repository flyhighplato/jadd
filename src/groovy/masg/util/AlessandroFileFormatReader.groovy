package masg.util

import java.util.HashMap;

import masg.dd.pomdp.agent.policy.FSCPolicyNode;
import masg.dd.variables.DDVariableSpaceIterator
import masg.problem.tag.TagProblemPOMDP

class AlessandroFileFormatReader {
	TagProblemPOMDP problem = new TagProblemPOMDP()
	
	def readFile(String path) {
		File aFile = new File(path);
		
		int numNodes = -1;
		
		int numBlankLinesInARow = 0;
		boolean readingTransition = false;
		boolean readingEmission = false;
		
		DDVariableSpaceIterator obsSpace = problem.getPOMDP().observationSpace.iterator()
		
		def obsToTransitionFn = [:]
		def emissionFn = []
		def obs
		
		aFile.readLines().each{ String line ->
			
			if(line.length()==0) {
				numBlankLinesInARow++;
			}
			else {
				
				if(line.contains("n_nodes")) {
					numNodes = Integer.parseInt(line.split(" = ")[1])
				}
				else if(line.contains("transition_fn")) {
					readingTransition = true;
					readingEmission = false;
				}
				else if(line.contains("emission_fn")) {
					readingEmission = true;
					readingTransition = false;
				}
				else if(numBlankLinesInARow>1) {
					readingTransition = false;
					readingEmission = false;
				}
				else {
					if(readingTransition) {
						if(numBlankLinesInARow==1) {
							obs = obsSpace.next()
							obsToTransitionFn[obs] = []
						}
						
						obsToTransitionFn[obs] << line.split(",").collect{ Double.parseDouble(it) }
					}
					
					if(readingEmission) {
						emissionFn << line.split(",").collect{ Double.parseDouble(it) }
					}
				}
				
				numBlankLinesInARow=0;
				
			}
			
		}
		
		ArrayList<FSCPolicyNode> nodes = new ArrayList<FSCPolicyNode>()
		
		numNodes.times {
			def n =  new FSCPolicyNode()
			n.index = it
			nodes << n
		}
		
		obsToTransitionFn.each { o, matrix ->
			matrix.eachWithIndex{ matrixRow, nodeIx ->
				
				FSCPolicyNode thisNode = nodes[nodeIx];
				HashMap<FSCPolicyNode,Double> dist = new HashMap<FSCPolicyNode,Double>();
				double normVal = matrixRow.sum();
				
				matrixRow.eachWithIndex { val, otherNodeIx ->
					if(val>0.0d) {
						FSCPolicyNode otherNode = nodes[otherNodeIx];
						
						dist[otherNode] = val/normVal;
					}
				}
				
				thisNode.transitionFn[o] = dist;
				
				normVal = emissionFn[nodeIx].sum()
				problem.getPOMDP().actionSpace.eachWithIndex{ action, actionIx ->
					if( emissionFn[nodeIx][actionIx]> 0.0d) {
						thisNode.actionDistributionFn[action] = emissionFn[nodeIx][actionIx]/normVal
					}
				}
				
				
			}
		}
		
		
		
		return nodes
		
	}
}
