package my.hearthtracking.app;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class HearthLanguageManager {
	private static HearthLanguageManager instance = null;
	String defaultLang = "english";
	String currentLang = defaultLang;
	boolean langLoaded = false;
	boolean overrided = false;
	boolean isDirty = false;
	public Map<String, String> map = null;
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
			//load the default language file
			hLang = (HearthLanguage) config.load(String.format(HearthFilesNameManager.uiLangFile, defaultLang));
			
			//if the default language file doesn't work
			if(hLang == null){
				//we will use the actual string in the code
				hLang = new HearthLanguage();
			}
			
			isDirty = true;
		}
		
		map = hLang.map;
		
		loadOverride();
	}
	
	private void loadOverride(){
		if(map == null){
			return;
		}
		
		HearthLanguage hLang = (HearthLanguage) config.load(String.format(HearthFilesNameManager.uiLangFile, "customOverride"));
		
		if(hLang == null || hLang.map == null){
			return;
		}
		
		Iterator<Entry<String, String>> it = hLang.map.entrySet().iterator();
		String value = "";
		
	    while (it.hasNext()) {
	        Map.Entry <String, String> pairs = it.next();
	        value = map.get(pairs.getKey());
	        
	        //replace the original values
	        if(value != null){
	        	map.put(pairs.getKey(), pairs.getValue());
	        }
	    }
	    
	    overrided = true;
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
		
		formatted = String.format(format, vals);
		
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
