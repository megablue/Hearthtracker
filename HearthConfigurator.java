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
