package edu.tis.phille97.Entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


public class Obstacle extends Entity{
	private boolean solid = false;
	
	private int x, y, height, width, conveyor_speed = 0;
	private BufferedImage image = null;
	private Color color;
	
	private String ability = ""; //(jump|deco|speed|platform)
	
	private int ID;
	
	private boolean trampoline = false, conveyor = false;
	
	public Obstacle(int ID, int x, int y, int height, int width, Color color, int conveyor_speed){
		this.ID = ID;
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
		this.color = color;
		this.solid = true;
		this.conveyor = true;
		this.conveyor_speed = conveyor_speed;
		
		if(this.conveyor) this.ability = "speed";
	}
	
	public Obstacle(int ID, int x, int y, int height, int width, Color color, boolean solid, boolean trampoline){
		this.ID = ID;
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
		this.color = color;
		this.solid = solid;
		this.trampoline = trampoline;
		
		if(trampoline) this.ability = "jump";
		else if(solid) this.ability = "platform";
		else this.ability = "deco";
	}
	
	public String getAbility(){
		return this.ability;
	}
	
	public int getID(){
		return this.ID;
	}
	
	public Color getColor(){
		return color;
	}
	
	public boolean isConveyor(){
		return this.conveyor;
	}
	
	public int getConveyorSpeed(){
		return this.conveyor_speed;
	}
	
	public boolean isSolid(){
		return this.solid;
	}
	
	public boolean isTrampoline(){
		return this.trampoline;
	}
	
	public void print(Graphics2D g2) {
		if(this.image != null){
			g2.drawImage(image, x, y, width, height, null);
			return;
		}
		Color prev = g2.getColor();
		g2.setColor(this.color);
		g2.fillRect(x, y, width, height);
		g2.setColor(prev);
	}
	
	public void tick(){
		
	}

	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public int getWidth() {
		return this.width;
	}
}
