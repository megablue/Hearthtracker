import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class HearthUpdater {
	XStream xstream = new XStream(new DomDriver());
	boolean foundUpdate = false;
	String message = "";
	int[] version = {0, 0, 0}; 
	
	public HearthUpdater() {
		
	}
	
	public void check(){
		URL url;
		BufferedReader in;
		HearthUpdaterLog log;
		
		try {
			url = new URL("http://hearthver.herokuapp.com/update/index.xml");
			in = new BufferedReader(
		                new InputStreamReader(
		                url.openStream()));
			
			log = (HearthUpdaterLog) xstream.fromXML(in);
			
			if(log.version[0] > HearthUI.version[0] 
				&& log.version[1] > HearthUI.version[1]
				&& log.version[1] > HearthUI.version[1]	){
				foundUpdate = true;
				message = log.message;
				version = log.version;
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasUpdate(){
		return foundUpdate;
	}
	
	public int[] getUpdateVersion(){
		return version;
	}
	
	public String getUpdateMessage(){
		return message;
	}
}
