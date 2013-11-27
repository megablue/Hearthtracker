import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class HearthConfigurator {
	private XStream xstream;

	public HearthConfigurator() {
		xstream = new XStream(new DomDriver());
	}
	
	public <t> Object load(String path){
		String xmlString;
		Object obj = null;
		
		try {
			HearthHelper.createFolder(path.substring(0,path.lastIndexOf(File.separator)));
			xmlString = HearthHelper.readFile(path);
			obj = xstream.fromXML(xmlString);
		} catch (IOException e) {
			
		}
		return obj;
	}
	
	public <T> boolean save(Object obj, String path){
		if (obj == null) return false;
		String xmlString = xstream.toXML(obj);

		try {
			HearthHelper.createFolder(path.substring(0, path.lastIndexOf(File.separator)));
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
