package my.hearthtracking.app;

import java.util.HashMap;
import java.util.Map;

public class HearthLanguageManager {
	private static HearthLanguageManager instance = null;
	String currentLang = "english";
	boolean langLoaded = false;
	boolean isDirty = false;
	public Map<String, String> map = new HashMap<String, String>();
	private HearthConfigurator config = new HearthConfigurator();

	public static HearthLanguageManager getInstance() {
		if(instance == null){
			instance = new HearthLanguageManager();
		}
		
		return instance;
	}
	
	public HearthLanguageManager() {
		if(instance == null){
			instance = this;
		}
	}
	
	public void loadLang(String lang){
		langLoaded = true;
		currentLang = lang.toLowerCase();
		
		HearthLanguage hLang = (HearthLanguage) config.load(String.format(HearthFilesNameManager.uiLangFile, currentLang));
		
		//if the language xml file is not found
		if(hLang == null){
			//create a new skeleton object
			hLang = new HearthLanguage();
			isDirty = true;
		}
		
		map = hLang.map;
	}
	
	private String getFormat(String orgFormat){
		String format = map.get(orgFormat);
		return format;
	}
	
	private boolean isKeyValueExists(String originalString, String format){
		if(format == null){
			map.put(originalString, originalString);
			return false;
		}
		
		return true;
	}
	
	public String t(String str, Object...vals){
		String format = getFormat(str);
		String formatted = "";
		
		if(!isKeyValueExists(str, format)){
			format = getFormat(str);
			isDirty = true;
		}
		
		if(vals.length == 0){
			formatted = format;
		} else {
			formatted = String.format(format, vals);
		}
		
		return formatted;
	}
	
	public void dispose(){
		if(!isDirty || !langLoaded){
			return;
		}
		
		HearthLanguage hLang = new HearthLanguage();
		
		hLang.map = map;

		config.save(hLang, String.format(HearthFilesNameManager.uiLangFile, currentLang));
	}

}
