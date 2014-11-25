package my.hearthtracking.app;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.NoSuchFileException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class HearthConfigurator {
	private XStream xstream;
	private static HearthLogger logger = HearthLogger.getInstance();

	public HearthConfigurator() {
		xstream = new XStream(new DomDriver());
		xstream.alias("HearthUpdaterLog", 				HearthUpdaterLog.class);
		xstream.alias("HearthDatabase", 				HearthDatabase.class);
		xstream.alias("HearthGameLang", 				HearthGameLang.class);
		xstream.alias("HearthGameLangList", 			HearthGameLangList.class);
		xstream.alias("HearthHeroesList", 				HearthHeroesList.class);
		xstream.alias("Hero", 							HearthHeroesList.Hero.class);
		xstream.alias("HearthScannerSettings", 			HearthScannerSettings.class);
		xstream.alias("Scanbox",						HearthScannerSettings.Scanbox.class);
		xstream.alias("HearthResolutionsList", 			HearthResolutionsList.class);
		xstream.alias("Resolution", 					HearthResolutionsList.Resolution.class);
		xstream.alias("Sync", 							HearthSyncLog.class);
		xstream.alias("HearthLanguage", 				HearthLanguage.class);
		xstream.alias("HearthDecks", 					HearthDecks.class);
		xstream.alias("HearthSetting", 					HearthSetting.class);
		xstream.alias("HearthULangsList", 				HearthULangsList.class);
		
		//omit fields below as those should be initialized on runtime
		xstream.omitField(HearthScannerSettings.Scanbox.class, "target");
		xstream.omitField(HearthScannerSettings.Scanbox.class, "unScaledTarget");
		xstream.omitField(HearthDecks.class, "instance");
	}
	
	public <t> Object load(String path){
		String xmlString;
		Object obj = null;
		
		try {
			HearthHelper.createFolder(HearthHelper.extractFolderPath(path));
			xmlString = HearthHelper.readFile(path);
			obj = xstream.fromXML(xmlString);
			logger.finest("path: \"" + path + "\" loaded");
		} 
		catch (NoSuchFileException e){ } 
		catch (Throwable e) {
			e.printStackTrace();
			logger.severe(HearthConfigurator.class.getName() + "->load(), " + "path: " + path + ", " + e.getMessage());
		}
		
		return obj;
	}
	
	public <T> boolean save(Object obj, String path){
		if (obj == null) return false;
		String xmlString = xstream.toXML(obj);

		HearthHelper.createFolder(HearthHelper.extractFolderPath(path));
		
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
			out.write(xmlString);
			out.close();
			
			logger.finest("path: \"" + path + "\" saved");
			return true;
		} catch (NoSuchFileException e){
			return false;
		}catch (Throwable e) {
			e.printStackTrace();
			logger.severe(HearthConfigurator.class.getName() + "->save(), " + "path: \"" + path + "\", " + e.getMessage());
		}
		
		return false;
	}
	

}
