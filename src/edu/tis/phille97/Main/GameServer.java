package edu.tis.phille97.Main;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Timer;
import java.util.TimerTask;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import edu.tis.phille97.Entities.Entity;
import edu.tis.phille97.Entities.Obstacle;
import edu.tis.phille97.Main.Objects.PlayerUpdateRequest;
import edu.tis.phille97.Main.Objects.PlayerUpdateResponse;
import edu.tis.phille97.Main.Objects.SomeRequest;
import edu.tis.phille97.Main.Objects.SomeResponse;

public class GameServer {
	private Server server = new Server();
	private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
	private final ArrayList<Entity> map_entities = new ArrayList<Entity>();
	
	private static final String WELCOME_MESSAGE = "Welcome to the developer server!";
	
	private static final String[] AVAVIBLE_PERMISSIONS = {"build"}; // The permissions currently inuse
	
	private static final String ADMIN_PASSWORD = "Sesamkatt123"; // SECRET!!!
	
	private static final int MAX_ALLOWED_CONNECTIONS = 100;
	
	@SuppressWarnings("unused")
	private BanHandler banhandler;
	
	
	private boolean started = false;
	public synchronized void stop(){
		if(!started)return;
		started = false;
		System.out.println("INFO: [Server] Stopping server!");
		this.kickAllClients("INFO: [Server] Server is stopping!");
		server.stop();
		System.out.println("INFO: [Server] Server stopped!");
	}
	
	public synchronized void start(int TCP_PORT, int UDP_PORT) throws IOException{
		banhandler = new BanHandler();
		if(started){ // Restart
			stop();
		}
		started = true;
		Kryo kryo = server.getKryo();
	    kryo.register(SomeRequest.class);
	    kryo.register(SomeResponse.class);
	    kryo.register(PlayerUpdateRequest.class);
	    kryo.register(PlayerUpdateResponse.class);
		server.start();
		
		Log.set(Log.LEVEL_INFO);
		
		server.bind(TCP_PORT, UDP_PORT);
	    
	    server.addListener(new Listener(){
	    	public void connected(Connection connection){
	    		try {
					clients.add(new ClientHandler(connection.getID(), connection));
				} catch (Exception e) {
					connection.close();
				}
	    	}
	    	
	    	public void received(Connection connection, Object object){
	    		for(ClientHandler ch : clients){
	    			if(ch.getID() == connection.getID()){
	    				if(object instanceof PlayerUpdateRequest){
		    				PlayerUpdateRequest req = (PlayerUpdateRequest)object;
		    				ch.X = req.X;
		    				ch.Y = req.Y;
		    			}else if(object instanceof SomeRequest){
		    				SomeRequest req = (SomeRequest)object;
		    				ch.handleRequest(req.text);
		    			}
	    			}
	    		}
	    	}
	    	
	    	public void disconnected(Connection connection) {
	    		ClientHandler toremove = null;
	    		for(ClientHandler ch : clients){
	    			if(ch.getID() == connection.getID()){
	    				toremove = ch;
	    			}
	    		}
	    		if(toremove != null) {
	    			GameServer.this.broadcastMessage("INFO: [Server] Player " + toremove.username + " disconnected!");
	    			GameServer.this.broadcast("#removePlayer#" + toremove.ID);
	    			
	    			if(server.getConnections().length == 0) {
	    				GameServer.this.map_entities.clear();
	    				System.out.println("INFO: [Server] Server is empty, so i will be removing all entities!");
	    			}
	    		}
	    		clients.remove(toremove);
	    	}
	    });
	    UpdatePlayersTask task = new UpdatePlayersTask();
	    Timer t = new Timer(true);
	    
	    t.scheduleAtFixedRate(task, 0, 20);
	}
	
	private boolean entityExist(int ID){
		for(Entity e : map_entities){
			if(e != null) if(e.getID() == ID) return true;
		}
		return false;
	}
	
	private boolean clientExist(int ID){
		for(ClientHandler ch : clients){
			if(ch != null) if(ch.getID() == ID) return true;
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private ClientHandler getClientbyName(String username){
		// TODO Make so none can have the same username!
		return null;
	}
	
	private ClientHandler getClient(int clientID){
		for(ClientHandler ch : clients){
			if(ch.getID() == clientID) return ch;
		}
		return null;
	}
	
	private boolean permissionExist(String permissiontocheck){
		for(String aviperm : AVAVIBLE_PERMISSIONS){
			if(aviperm.equals(permissiontocheck)) return true;
		}
		return false;
	}
	
	public void givePermission(int clientID, String perm){
		if(clientExist(clientID)){
			this.getClient(clientID).givePermission(perm);
		}
	}
	
	public synchronized void kickClient(int clientID, String info){
		if(!clientExist(clientID)) {
			System.out.println("INFO: [Server] No client with id: " + clientID + ".");
			return;
		}
		ClientHandler toremove = null;
		for(ClientHandler ch : clients){
			if(ch != null){
				if(ch.getID() != clientID) continue;
				try {
					ch.kick(info);
				} catch (IOException e) {
					System.err.println("ERROR: [Server] " + e.getMessage());
				}
				toremove = ch;
			}
		}
		if(toremove != null){
			clients.remove(toremove);
		}else{
			System.out.println("INFO: [Server] No client with id: " + clientID + ".");
		}
	}
	public synchronized void kickAllClients(String info){
		while(!this.listAllplayers().equals("")){
			for(int i = 0; i < clients.size(); i++){
				kickClient(clients.get(i).getID(), info);
			}
		}
		System.out.println("INFO: [Server] All clients have been kicked!");
	}
	public synchronized void kickAllClients(){
		kickAllClients("You have been kicked from the server!");
	}
	public synchronized String listAllplayers(){
		String list = "";
		for(Connection ch : server.getConnections()){
			if(ch != null)list += ">" + ch.getID() + ">" + ch.getRemoteAddressTCP().getHostName() + "\n";
		}
		return list;
	}
	public synchronized void banUser(String username, String reason){
		// TODO
	}
	public synchronized void pardonBannedUser(String username){
		// TODO
	}
	
	/*
	public synchronized void broadcastEntityChange(int entityID){
		// From server  blockID#colour#x#y#width#height#ability(jump|deco|speed|platform)
		// From client  colour(GRAY|CYAN|ORANGE|GREEN)#x#y#width#height#ability(jump|deco|speed|platform)
		for(Entity e : map_entities){
			// TODO Sync other entities than obstacles
			if(e instanceof Obstacle){
				if(e.getID() == entityID){
					
				}
			}
		}
	}
	*/
	public synchronized void broadcast(String line){
		SomeResponse response = new SomeResponse();
		response.text = line;
		server.sendToAllTCP(response);
	}
	public synchronized void broadcastMessage(String line){
		System.out.println("INFO: [Server] Broadcast: " + line);
		broadcast("#msg#" + line);
	}
	public synchronized void sendMessage(int clientID, String line){
		send(clientID, "#msg#" + line);
	}
	public synchronized void send(int clientID, String line){
		SomeResponse response = new SomeResponse();
		response.text = line;
		server.sendToTCP(clientID, response);
	}
	
	public static int getMaxAllowedConnections() {
		return MAX_ALLOWED_CONNECTIONS;
	}

	private class UpdatePlayersTask extends TimerTask {
		
		@Override
		public void run() {
			broadcast("#reqPOS#");
			try {
				for(ClientHandler ch : clients){
					if(ch != null) if(ch.running) {
						PlayerUpdateResponse response = new PlayerUpdateResponse();
						response.ID = ch.getID();
						response.X = ch.getX();
						response.Y = ch.getY();
						response.USERNAME = ch.getUsername();
						server.sendToAllUDP(response);
					}
				}
			} catch(ConcurrentModificationException e){
				System.out.println(e.getMessage());
			}
		}
		
	}
	
	
	
	private class ClientHandler {
		private final int ID;
		private boolean running = false;
		private String username = "No Username";
		
		private ArrayList<String> permissions = new ArrayList<String>();
		
		private boolean OP = false; // !!!WARNING!!! If a Client have gained OP he or she have full control over the server!!!
		private boolean isOP() {
			return this.OP;
		}
		
		private int X = 0, Y = 0;
		
		public int getX(){
			return this.X;
		}
		public int getY(){
			return this.Y;
		}
		
		public String getUsername(){
			return this.username;
		}
		
		public int getID(){
			return this.ID;
		}
		
		public void givePermission(String permission){
			if(GameServer.this.permissionExist(permission)){
				if(this.gotPermission(permission)){
					System.out.println("INFO: [Server] Client " + this.ID + " already have permission: " + permission);
				}else{
					this.sendMessage("You have gained permission: " + permission);
					permissions.add(permission);
				}
			}
		}
		
		public void kick(String reason) throws IOException{
			SomeResponse response = new SomeResponse();
			response.text = "#close#" + reason;
			
			server.sendToTCP(ID, response);
			
			this.running = false;
		}
		
		public ClientHandler(int clientID, Connection connection) throws Exception {
			this.ID = clientID;
			
			GameServer.this.broadcastMessage("Player " + this.ID + " connected to the server!");
			this.running = true;
			
			Thread.sleep(500);
			
			this.givePermission("build");
			
			for(ClientHandler ch : clients){
				//#newPlayer#ID#USERNAME#X#Y
				if(ch != null) if(!ch.equals(this)) send("#newPlayer#" + ch.ID + "#" + ch.username + "#" + ch.getX() + "#" + ch.getY());
			}
			for(Entity e : map_entities){
				if(e instanceof Obstacle){
					Obstacle o = (Obstacle) e;
					Color c = o.getColor();
					// (GRAY|CYAN|ORANGE|GREEN)
					String colour = "";
					if(c == Color.GRAY) colour = "GRAY";
					else if(c == Color.CYAN) colour = "CYAN";
					else if(c == Color.ORANGE) colour = "ORANGE";
					else if(c == Color.GREEN) colour = "GREEN";
					
					this.send("#addBlock#" + o.getID() + "#" + colour + "#" + o.getX() + "#" + o.getY() + "#" + o.getWidth() + "#" + o.getHeight() + "#" + o.getAbility());
				}
			}
			this.send("#msg#" + WELCOME_MESSAGE);
		}
		
		public synchronized void send(String line){
			SomeResponse response = new SomeResponse();
			response.text = line;
			server.sendToTCP(ID, response);
		}
		
		public void sendMessage(String line){
			send("#msg#" + line);
		}
		
		private void handleRequest(String line){
			if(line.startsWith("#msg#")){
				line = line.replaceFirst("#msg#", "");
				GameServer.this.broadcastMessage(username + ": " + line);

				return;
			}else if(line.startsWith("#updatePOS#")){
				
				line = line.replaceFirst("#updatePOS#", "");
				int i = line.indexOf("#");
				String x = line.substring(0, i);
				String y = line.substring(i+1);
				x = x.trim();
				y = y.trim();
				this.X = Integer.parseInt(x);
				this.Y = Integer.parseInt(y);
				return;
				
			}else if(line.startsWith("#addBlock#")){
				if(!allowedToBuild()) {
					this.sendMessage("You are not allowed to build here!");
					return;
				}
				line = line.replaceFirst("#addBlock#", "");
				int i1 = line.indexOf("#");
				int i2 = line.indexOf("#", i1+1);
				int i3 = line.indexOf("#", i2+1);
				int i4 = line.indexOf("#", i3+1);
				int i5 = line.indexOf("#", i4+1);
				String colour = line.substring(0, i1);
				int x = Integer.parseInt(line.substring(i1+1, i2));
				int y = Integer.parseInt(line.substring(i2+1, i3));
				int width = Integer.parseInt(line.substring(i3+1, i4));
				int height = Integer.parseInt(line.substring(i4+1, i5));
				String ability = line.substring(i5+1);
				int blockID = GameServer.this.map_entities.size() + 1;
				while(entityExist(blockID)){ // Make sure no block have the same ID
					blockID++;
				}
				// From server  #addBlock#blockID#colour(GRAY|CYAN|ORANGE|GREEN)#x#y#width#height#ability(jump|deco|speed|platform)
				// From client  #addBlock#colour(GRAY|CYAN|ORANGE|GREEN)#x#y#width#height#ability(jump|deco|speed|platform)
				Color color = Color.white;
				
				if(colour.equals("GRAY"))color = Color.GRAY;
				else if(colour.equals("GREEN")) color = Color.GREEN;
				else if(colour.equals("ORANGE")) color = Color.ORANGE;
				else if(colour.equals("CYAN")) color = Color.CYAN;
				
				if (ability.equalsIgnoreCase("platform")) {
					map_entities.add(new Obstacle(blockID, x, y, height, width, color, true, false));// Long Platform block
				}else if(ability.equalsIgnoreCase("deco")){
					map_entities.add(new Obstacle(blockID, x, y, height, width, color, false, false));// Short decoration block
				}else if(ability.equalsIgnoreCase("jump")){
					map_entities.add(new Obstacle(blockID, x, y, height, width, color, true, true));// Long Jump block
				}else if(ability.equalsIgnoreCase("speed")){
					map_entities.add(new Obstacle(blockID, x, y, height, width, color, 6));// Long Speed block
				}
				broadcast("#addBlock#" + blockID + "#" + colour + "#" + x + "#" + y + "#" + width + "#" + height + "#" + ability);

				return;
			}else if(line.startsWith("#removeBlock#")){ // #removeBlock#ID
				if(!allowedToBuild()) {
					this.sendMessage("You are not allowed to build here!");
					return;
				}
				line = line.replaceFirst("#removeBlock#", "");
				ArrayList<Entity> toRemove = new ArrayList<Entity>();
				
				for(Entity e : map_entities){
					if(e.getID() == Integer.parseInt(line)){
						toRemove.add(e);
					}
				}
				for(Entity e : toRemove){
					broadcast("#removeBlock#" + e.getID());
					map_entities.remove(e);
				}

				return;
			}else if(line.startsWith("#gainOP#")){ // #gainOP#password
				if(this.isOP()) {
					this.sendMessage("You are already OP!");
					return;
				}
				line = line.replaceFirst("#gainOP#", "");
				if(line.equals(ADMIN_PASSWORD)){
					this.OP = true;
					this.sendMessage("You have gained OP!");
				}
				
				return;
			}else if(line.startsWith("#clearallentities#")){
				if(this.allowedToBuild() || this.isOP()){
					for(Entity e : map_entities){
						GameServer.this.broadcast("#removeBlock#" + e.getID());
					}
					map_entities.clear();
				}
				return;
			}else if(line.startsWith("#username#")){
				line = line.replaceFirst("#username#", "");
				this.username = line;
				this.send("#yourID#" + this.ID);
				return;
			}else if(line.startsWith("#whatAbout#")){
				line = line.replaceFirst("#whatAbout#", "");
				boolean found = false;
				for(ClientHandler ch : clients) {
					if(ch.getID() == Integer.parseInt(line)){
						found = true;
					}
				}
				if(!found){
					send("#removePlayer#" + line);
				}
				return;
			}
			
			// Invalid request!
			this.sendMessage("Badly formed request: " + line + " at: " + this);
			
		}
		
		private boolean allowedToBuild() {
			if(this.isOP()) return true;
			if(this.gotPermission("build")) return true;
			return false;
		}
		private boolean gotPermission(String string) {
			for(String perm : permissions){
				if(perm.equalsIgnoreCase(string)) return true;
			}
			return false;
		}
	}
}