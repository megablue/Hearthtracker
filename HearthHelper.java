import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

public class HearthHelper {

	public static boolean isDevelopmentEnvironment() {
	    boolean isEclipse = true;
	    if (System.getenv("eclipseKelper") == null) {
	        isEclipse = false;
	    }
	    return isEclipse;
	}
	
	static String readFile(String path)  throws IOException {
		Charset encoding = Charset.forName("UTF8");
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
	
	public static BufferedImage resizeImage(File imgFile, float scaleFactor){
		try {
			BufferedImage sourceImage = ImageIO.read(imgFile);
			
			int resizedWidth = (int) (sourceImage.getWidth() * scaleFactor);  
			Image thumbnail = sourceImage.getScaledInstance(resizedWidth, -1, Image.SCALE_SMOOTH);
			BufferedImage bufferedThumbnail = new BufferedImage(thumbnail.getWidth(null),
			                                                    thumbnail.getHeight(null),
			                                                    BufferedImage.TYPE_INT_RGB);
			bufferedThumbnail.getGraphics().drawImage(thumbnail, 0, 0, null);
			
			return bufferedThumbnail;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static BufferedImage resizeImage(File imgFile, float scaleFactor, File outputFile){
		BufferedImage resizedBuffer = resizeImage(imgFile, scaleFactor);
		try {
			ImageIO.write(resizedBuffer, "png", outputFile);
			return resizedBuffer;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getArchFilename(String prefix) 
	{ 
		return prefix + "-" + getOSName() + "-" + getArchName() + ".jar";
	} 

	public static String getOSName() 
	{ 
	   String osNameProperty = System.getProperty("os.name"); 

	   if (osNameProperty == null) 
	   { 
	       throw new RuntimeException("os.name property is not set"); 
	   } 
	   else 
	   { 
	       osNameProperty = osNameProperty.toLowerCase(); 
	   } 

	   if (osNameProperty.contains("win")) 
	   { 
	       return "win"; 
	   } 
	   else if (osNameProperty.contains("mac")) 
	   { 
	       return "osx"; 
	   } 
	   else if (osNameProperty.contains("linux") || osNameProperty.contains("nix")) 
	   { 
	       return "linux"; 
	   } 
	   else 
	   { 
	       throw new RuntimeException("Unknown OS name: " + osNameProperty); 
	   } 
	} 

	public static String getArchName() 
	{ 
	   String osArch = System.getProperty("os.arch"); 

	   if (osArch != null && osArch.contains("64")) 
	   { 
	       return "64"; 
	   } 
	   else 
	   { 
	       return "32"; 
	   } 
	}

	public static void addJarToClasspath(File jarFile) 
	{
		try 
		{ 
			URL url = jarFile.toURI().toURL(); 
			URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader(); 
			Class<?> urlClass = URLClassLoader.class; 
			Method method = urlClass.getDeclaredMethod("addURL", new Class<?>[] { URL.class }); 
			method.setAccessible(true);         
			method.invoke(urlClassLoader, new Object[] { url });  
		} 
		catch (Throwable t) 
		{ 
			t.printStackTrace(); 
		}
	}
}
