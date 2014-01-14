package my.hearthtracking.app;

public class HearthBnetServersList {

	public class Server {
		public int id;
		public String name;
		public String label;
		
		public Server(String n, HearthBnetServersList list){
			String firstChar = (n.charAt(0) + "").toUpperCase();
			label = firstChar + n.substring(1, n.length());
			name = n.toLowerCase();
			id = list.getNextId();
		}
	}
	
	public int currentId = -1;
	public int total = 0;
	
	public Server[] servers = {
		new Server("NA", this),
		new Server("EU", this),
		new Server("Asia", this),
		new Server("CN", this),
	};
	
	public int getTotal(){
		return total;
	}
	
	public String getServerName(int id){
		
		return (id >= 0 && id < servers.length) ? servers[id].name : "";
	}
	
	
	public String getServerLabel(int id){
		String label = (id >= 0 && id < servers.length) ? 
				HearthLanguageManager.getInstance().t(servers[id].label) : "";
		
		return label;
	}
	public int getNextId(){
		++total;
		return ++currentId;
	}
}
