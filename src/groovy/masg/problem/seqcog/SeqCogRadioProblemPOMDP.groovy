package masg.problem.seqcog

import masg.dd.pomdp.POMDP
import masg.problem.builder.POMDPProblemBuilder


class SeqCogRadioProblemPOMDP {
	
	int numChannels = 3;
	//int numQueues = numChannels;
	int maxMessageLength = 5;
	int maxMessageDeadline = maxMessageLength;
	
	double occToIdleChance = 0.3d;
	double idleToOccChance = 0.5d;
	
	POMDP p;
	
	public SeqCogRadioProblemPOMDP() {
		POMDPProblemBuilder builder = new POMDPProblemBuilder();
		
		//ACTIONS
		builder.addAction("act", numChannels);
		
		//OBSERVATIONS
		numChannels.times {
			builder.addObservation("ch${it}_looks_occ",2);
		}
		
		//STATES
		numChannels.times {
			builder.addState("ch${it}_is_occ",2);
		}
		
		numChannels.times {
			builder.addState("q${it}_length",maxMessageLength);
		}
		
		numChannels.times {
			builder.addState("q${it}_deadline",maxMessageDeadline);
		}
		
		builder.setInitialBelief { Map variables ->
			boolean isInitialState = true;
			
			if(isInitialState) {
				numChannels.times {
					isInitialState = isInitialState && variables["q${it}_length"]==0;
					isInitialState = isInitialState && variables["q${it}_deadline"]==0;
				}
			}
			
			if(isInitialState) {
				return 1.0d/Math.pow(2.0d, numChannels);
			}
			else {
				return 0.0d;
			}
		}
		
		def stateArgs = []
		numChannels.times {
			stateArgs << "ch${it}_is_occ".toString()
			stateArgs << "q${it}_length".toString()
			stateArgs << "q${it}_deadline".toString()
		}
		
		builder.setRewardFunction(stateArgs , ["act"]) { Map variables ->
			
			double reward = 0.0d
			numChannels.times {
				if(variables["act"]==it && variables["ch${it}_is_occ"]==0 && variables["q${it}_length"]==1 && variables["q${it}_deadline"]>0) {
					reward += 10.0d;
				}
				else if (variables["q${it}_length"]==1>variables["q${it}_deadline"]){
					reward += -1000.0d;
				}
				else {
					reward +=-1.0d;
				}
			}
			
			return reward;
		}
		
		numChannels.times { chNum ->
			builder.addTransition(["ch${chNum}_is_occ"], ["act"], ["ch${chNum}_is_occ'"]) { Map variables ->
				
				//Occupied
				if(variables["ch${chNum}_is_occ"] == 1) {
					if(variables["ch${chNum}_is_occ'"] == 1) {
						return 1.0d - occToIdleChance
					}
					else {
						return occToIdleChance
					}
				}
				//Not occupied
				else if(variables["ch${chNum}_is_occ"] == 0) {
					if(variables["ch${chNum}_is_occ'"] == 1) {
						return idleToOccChance
					}
					else {
						return 1.0d - idleToOccChance
					}
				}
			}
			
			builder.addTransition( ["ch${chNum}_is_occ","q${chNum}_length","q${chNum}_deadline"], ["act"], ["q${chNum}_length'","q${chNum}_deadline'"] ) { Map variables ->
				
				int lengthOld = variables["q${chNum}_length"]
				int lengthNew = variables["q${chNum}_length'"]
				
				int deadlineOld = variables["q${chNum}_deadline"]
				int deadlineNew = variables["q${chNum}_deadline'"]
				
				boolean isOccupied = (variables["ch${chNum}_is_occ"]==1)
				
				int act = variables["act"]
				
				//Message dies when it's length exceeds deadline and a new (valid) one is created
				if((lengthOld>deadlineOld || lengthOld==0) && lengthNew<=deadlineNew) {
					return 1.0d/( maxMessageDeadline - (double)lengthNew );
				}
				else if( act == chNum && !isOccupied ) {
					if(lengthOld-1 == lengthNew && deadlineNew == deadlineOld-1) {
						return 1.0d;
					}
				}
				else if( act != chNum || isOccupied ) {
					if(lengthOld == lengthNew && deadlineNew == deadlineOld-1) {
						return 1.0d;
					}
				}
				
				
				return 0.0d;
			}
			
			builder.addObservation(["ch${chNum}_is_occ'"], ["act"], ["ch${chNum}_looks_occ"]) { Map variables ->
				
				if(variables["act"]==chNum) {
					if(variables["ch${chNum}_looks_occ"]==variables["ch${chNum}_is_occ'"]) {
						return 1.0d;
					}
					else {
						return 0.0d;
					}
				}
				else {
					if(variables["ch${chNum}_looks_occ"]==variables["ch${chNum}_is_occ'"]) {
						return 0.5d;
					}
					else {
						return 0.5d;
					}
				}
			}
		}
		
		p = builder.buildPOMDP();
		
	}
	
	public POMDP getPOMDP() {
		return p;
	}
}
