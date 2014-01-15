package my.hearthtracking.app;

import java.util.HashMap;
import java.util.Map;

public class HearthULangsList {
	public Map<String, String> map = new HashMap<String, String>();
	
	public HearthULangsList(){
		if(map.size() == 0){
			map.put("繁體中文", "traditional-chinese");
			map.put("简体中文", "simplified-chinese");
			map.put("English", "english");
		}
	}	
}
