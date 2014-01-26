package my.hearthtracking.app;

public class HearthDecks {
	private static HearthDecks instance = null;

	public String[] list = { 
			"Awesome Deck #1", 
			"Awesome Deck #2", 
			"Awesome Deck #3", 
			"Awesome Deck #4", 
			"Awesome Deck #5", 
			"Awesome Deck #6", 
			"Awesome Deck #7", 
			"Awesome Deck #8", 
			"Awesome Deck #9", 
	};

	public HearthDecks() {
		if(instance == null){
			instance = this;
		}
	}

	public void setInstance(HearthDecks instance){
		HearthDecks.instance = instance;
	}

	public static HearthDecks getInstance() {
		if(instance == null){
			instance = new HearthDecks();
		}
		
		return instance;
	}
	
	public void rearrange(){
		String [] newList = new String[list.length];
		int y = 0;
		
		for(int i = 0; i < list.length; i++){
			
			if(!list[i].equals("")){
				newList[y++] = list[i];
			}
		}
		
		for(; y < list.length; y++){
			newList[y] = "";
		}
		
		list = newList;
	}
}
