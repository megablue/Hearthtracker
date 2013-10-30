
public class HearthHeroesList {
	
	public class Hero {
		public int id;
		public String name;
		public String label;
		
		public Hero(String n, HearthHeroesList list){
			String firstChar = (n.charAt(0) + "").toUpperCase();
			name = n;
			label = firstChar + name.substring(1, name.length()); 
			id = list.getNextId();
		}
	}
	
	public int currentId = -1;
	public int totalHeroes = 0;
	
	public Hero[] heroes = {
		new Hero("mage", this),
		new Hero("hunter", this),
		new Hero("warrior", this),
		new Hero("shaman", this),
		new Hero("druid", this),
		new Hero("priest", this),
		new Hero("rogue", this),
		new Hero("paladin", this),
		new Hero("warlock", this),
	};
	
	public int getTotal(){
		return totalHeroes;
	}
	
	public int getHeroId(String heroName){
		heroName = heroName.toLowerCase();
		
		for(int i = 0; i < heroes.length; i++){
			if(heroes[i].name.equals(heroName)){
				return heroes[i].id;
			}
		}
		
		return -1;
	}
	
	public String getHeroName(int heroId){
		for(int i = 0; i < heroes.length; i++){
			if(heroes[i].id == heroId){
				return heroes[i].name;
			}
		}
		
		return "Unknown";
	}
	
	public String getHeroLabel(int heroId){
		for(int i = 0; i < heroes.length; i++){
			if(heroes[i].id == heroId){
				return heroes[i].label;
			}
		}
		return "Unknown";
	}
	
	public int getNextId(){
		++totalHeroes;
		return ++currentId;
	}

}
