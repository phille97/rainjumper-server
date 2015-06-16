package edu.tis.phille97.Main;

import java.io.File;
import java.io.IOException;

public class BanHandler {
	private final File gameDirectory = new File("RainServer/");
	
	public BanHandler() {
		if(gameDirectory.exists()){
			System.out.println("Game directory found.");
		}else{
			gameDirectory.mkdir();
			System.out.println("A Game directory was created.");
		}
		
	}
	
	public synchronized void writeIPban(String addresstoban, String reason) throws IOException{
		// TODO
	}
}
