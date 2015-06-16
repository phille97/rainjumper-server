package edu.tis.phille97.Entities;
import java.awt.Graphics2D;


public class Entity {
	private boolean solid = false;
	private int ID;
	
	public boolean isObstacle(){
		return this.solid;
	}
	
	public int getID(){
		return this.ID;
	}
	
	public void tick(){
		
	}
	
	public void print(Graphics2D g){
		
	}
}
