package masg.problem.tag.simulator;

import java.util.HashMap;

import masg.dd.pomdp.POMDP;
import masg.dd.pomdp.agent.policy.Policy;
import masg.dd.pomdp.agent.policy.RandomPolicy;
import masg.dd.variables.DDVariable

public class TagWumpus extends TagAgent {

	Random random = new Random()
	
	public TagWumpus(POMDP pomdp) {
		rowVar = new DDVariable('w_row',5)
		colVar = new DDVariable('w_col',5)
		
		row = random.nextInt(5)
		column = random.nextInt(5)
	}
	
	public moveRandomly(int height, int width) {
		//North/South
		if(random.nextBoolean()) {
			//North
			if(random.nextBoolean()) {
				if(row < height-1)
					row++;
			}
			//South
			else {
				if(row > 0)
					row --;
			}
		}
		else {
			//East
			if(random.nextBoolean()) {
				if(column < width-1)
					column++;
			}
			//West
			else {
				if(column > 0)
					column--;
			}
		}
	}

}
