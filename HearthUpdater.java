import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class HearthUpdater {
	XStream xstream = new XStream(new DomDriver());
	private boolean foundUpdate = false;
	private String message = "";
	private int[] version = {0, 0, 0}; 
	private long updated = 0;
	private static HearthConfigurator config = new HearthConfigurator();
	HearthUpdaterLog updateLog;
	
	public HearthUpdater(){
		updateLog = (HearthUpdaterLog) config.load("." + File.separator + "configs" + File.separator + "update.xml");
		if(updateLog == null){
			updateLog = new HearthUpdaterLog();
			config.save(updateLog, "." + File.separator + "configs" + File.separator + "update.xml");
		}
		this.compare();
	}
	
	public boolean lastCheckExpired(){
		if( (new Date().getTime()/1000) - (updateLog.lastCheck/1000) < 12 * 60 * 60 ){
			return false;
		}
		
		return true;
	}
	
	public void check(){
		URL url;
		BufferedReader in;
		
		try {
			url = new URL("http://hearthver.herokuapp.com/update/index.xml");
			in = new BufferedReader(
		                new InputStreamReader(
		                url.openStream()));
			
			updateLog = (HearthUpdaterLog) xstream.fromXML(in);
			updateLog.lastCheck = new Date().getTime();
			
			this.compare();
			
			if(updateLog != null){
				config.save(updateLog, "." + File.separator + "configs" + File.separator + "update.xml");
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void compare(){
		if( 
				(updateLog.version[0] > HearthUI.version[0])
				|| (updateLog.version[0] >= HearthUI.version[0] && updateLog.version[1] > HearthUI.version[1])
				|| (updateLog.version[0] >= HearthUI.version[0] && updateLog.version[1] >= HearthUI.version[1] && updateLog.version[2] > HearthUI.version[2])
		){
			foundUpdate = true;
			message = updateLog.message;
			version = updateLog.version;
			updated = updateLog.updated;
		}
	}
	
	public boolean hasUpdate(){
		return foundUpdate;
	}
	
	public int[] getUpdateVersion(){
		return version;
	}
	
	public String getUpdateVersionString(){
		return version[0] + "." + version[1] + "." + version[2];
	}
	
	public String getUpdateMessage(){
		return message;
	}
	
	public long getLastUpdated(){
		return updated;
	}
}
