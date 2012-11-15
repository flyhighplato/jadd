package masg.problem.tag.simulator;

import masg.dd.pomdp.POMDP
import masg.dd.pomdp.agent.policy.Policy

public class TagGrid {
	
	TagAgent agent1, agent2;
	TagWumpus wumpus;
	
	int height = 5, width = 5;
	public TagGrid(int height, int width, TagAgent agent1, TagAgent agent2, TagWumpus wumpus) {
		this.height = height
		this.width = width
		this.agent1 = agent1
		this.agent2 = agent2
		this.wumpus = wumpus
	}
	
	public void draw()
	{
		for(int j=0;j<width;j++)
		{
			print("-----");
		}
		
		System.out.println();
		
		height.times { int i ->
			
			width.times { int j ->
				print "|"
				
				if(agent1.column == j && agent1.row == i)
					print "1"
				else
					print " "
				
				if(wumpus.column == j && wumpus.row == i)
					print "W"
				else
					print " ";
				
				print "  "
			}
			
			println "|"
			
			width.times { int j ->
				print "|"
				if(agent2.column == j && agent2.row == i)
					print "2"
				else
					print " "
				
				print " "
				
				print "  "
			}
			
			println "|"
			
			width.times { 
				print "-----"
			}
			
			println()
		}
	}
}
