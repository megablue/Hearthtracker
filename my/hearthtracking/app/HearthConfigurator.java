package my.hearthtracking.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.NoSuchFileException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class HearthConfigurator {
	private XStream xstream;

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
		
		xstream.omitField(HearthScannerSettings.Scanbox.class, "target");
		xstream.omitField(HearthScannerSettings.Scanbox.class, "unScaledTarget");
	}
	
	public <t> Object load(String path){
		String xmlString;
		Object obj = null;
		
		try {
			HearthHelper.createFolder(path.substring(0,path.lastIndexOf(File.separator)));
			xmlString = HearthHelper.readFile(path);
			obj = xstream.fromXML(xmlString);
		} catch (NoSuchFileException e){
			return obj; 
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	public <T> boolean save(Object obj, String path){
		if (obj == null) return false;
		String xmlString = xstream.toXML(obj);

		HearthHelper.createFolder(path.substring(0, path.lastIndexOf(File.separator)));
		
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
			out.write(xmlString);
			out.close();
			return true;
		} catch (NoSuchFileException e){
			return false;
		}catch (Throwable e) {
			e.printStackTrace();
		}
		
		return false;
	}
	

}
