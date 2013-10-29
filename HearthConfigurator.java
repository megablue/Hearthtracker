import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class HearthConfigurator {
	private XStream xstream;

	public HearthConfigurator() {
		xstream = new XStream(new DomDriver());
		xstream.alias("hearthgame", HearthGame.class);
		xstream.alias("gamelanglist", HearthGame.class);
	}
	
	public <t> Object load(String path){
		String xmlString;
		Object obj = null;
		try {
			xmlString = HearthHelper.readFile(path);
			obj = xstream.fromXML(xmlString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return obj;
	}
	
	public <T> boolean save(Object obj, String path){
		if (obj == null) return false;
		String xmlString = xstream.toXML(obj);
		
		try {
			PrintWriter out = new PrintWriter(path);
			out.println(xmlString);
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		return false;
	}
	

}
