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
	
	public void draw() {
		draw(new BufferedWriter(new PrintWriter(System.out)))	
	}
	
	public void draw(BufferedWriter w)
	{
		for(int j=0;j<width;j++)
		{
			w.write("-----");
		}
		
		w.newLine();
		
		height.times { int i ->
			
			width.times { int j ->
				w.write "|"
				
				if(agent1.column == j && agent1.row == i)
					w.write "1"
				else
					w.write " "
				
				if(wumpus.column == j && wumpus.row == i)
					w.write "W"
				else
					w.write " ";
				
				w.write "  "
			}
			
			w.write "|"
			w.newLine()
			
			width.times { int j ->
				w.write "|"
				if(agent2.column == j && agent2.row == i)
					w.write "2"
				else
					w.write " "
				
				w.write " "
				
				w.write "  "
			}
			
			w.write "|"
			w.newLine()
			
			width.times { 
				w.write "-----"
			}
			
			w.newLine()
		}
		
		w.flush()
	}
}
