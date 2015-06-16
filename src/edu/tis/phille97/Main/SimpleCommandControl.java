package edu.tis.phille97.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

public class SimpleCommandControl {
	private GameServer gameServer = new GameServer();
	
	public static void main(String[] args){
		new SimpleCommandControl();
	}
	
	public SimpleCommandControl(){
		
		try {
			gameServer.start(25560, 25560);
		} catch (IOException e) {
			System.out.println("Problem when trying to start server on port: 25560 and/or 5477");
			return;
		}
		CommandHandler thread = new CommandHandler();
		thread.start();
	}
	
	private class CommandHandler extends Thread {
		
		@Override
		public void run(){
			while(true){
				
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		        System.out.print("");
		        String line = "";
				try {
					line = br.readLine();
				} catch (IOException e1) {
					e1.printStackTrace();
					gameServer.stop();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {}
					System.exit(-1);
				}
				
				if(line.equals("list")){
					System.out.println("Clients:" + gameServer.listAllplayers());
					continue;
				}else if(line.equals("kickall")){
					gameServer.kickAllClients();
					continue;
				}else if(line.equals("stop")){
					gameServer.stop();
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {}
					System.out.println("Good bye!");
					System.exit(-1);
				}else if(line.equals("start")){
					try {
						gameServer.start(Integer.parseInt(JOptionPane.showInputDialog("Enter the TCP port that the server will listen to.\nDefault port is: 25560")), Integer.parseInt(JOptionPane.showInputDialog("Enter the UDP port that the server will listen to.\nDefault port is: 25560")));
					} catch (NumberFormatException e) {
						System.out.println("Server could not start! Invalid port number.");
						e.printStackTrace();
					} catch (IOException e){
						System.out.println("Problem when trying to start server! Make sure no other program is using that port!");
						e.printStackTrace();
					}
					continue;
				}else if(line.equals("pos")){
					gameServer.broadcast("#reqPOS#");
					continue;
				}else if(line.equals("help")){
					System.out.println("Avavible commands:");
					System.out.println("kickall, stop, list, pos");
					continue;
				}
				System.out.println("Command not found!");
			}
			
		}
	}
}
