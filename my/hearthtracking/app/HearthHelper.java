package my.hearthtracking.app;

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
import java.util.Date;
import java.util.Calendar;
import java.util.logging.FileHandler;  
import java.util.logging.Logger;  
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;

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
		Charset encoding = Charset.forName("UTF-8");
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
	
	public static void createFolder(String folder)
	{
		File theDir = new File(folder);

		// if the directory does not exist, create it
		if (!theDir.exists()) {
			System.out.println("creating directory: " + folder);
			boolean result = theDir.mkdir();  
		
			if(result) {    
				System.out.println("DIR created");  
			}
		}
	}
	
	public static boolean fileExists(String file)
	{
		File theFile = new File(file);
		
		if (theFile.exists()) {
			return true;
		}
		
		return false;
	}
	
	
	public static int[] getHearthstonePosition(){
		int[] pos = {0,0,0,0};

		if(!getOSName().equals("win")){
			return pos;
		}
		
		try {			
			int[] rect = HearthWin32Helper.getRect("Hearthstone", "UnityWndClass");
			
			pos[0] = rect[0];
			pos[1] = rect[1];
			pos[2] = rect[2];
			pos[3] = rect[3];
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		return pos;
	}
	
	public static Logger getLogger(Level loglevel){
        Logger logger = Logger.getLogger("HearthTrackerLog");  
        FileHandler fh;
        int limit = 1000000; 
        int rotate = 10;
        Calendar cal = Calendar.getInstance();
        String logfile = cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DATE) + "-" + cal.get(Calendar.YEAR) + ".log";
        
        try {
            // This block configure the logger with handler and formatter  
            fh = new FileHandler("." + File.separator + "logs" + File.separator + logfile, limit, rotate, true);
            logger.addHandler(fh);  
            logger.setLevel(loglevel);  
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);     
        } catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        
        return logger;
	}
	
	public static String getPrettyText(Date date) {
	    long diff = (new Date().getTime() - date.getTime()) / 1000;
	    double dayDiff = Math.floor(diff / 86400);

	    if (diff < 0) {
	      return "in the future?";
	    } else if (diff < 60) {
	      return "moments ago";
	    } else if (diff < 120) {
	      return "one minute ago";
	    } else if (diff < 3600) {
	      return diff / 60 + " minutes ago";
	    } else if (diff < 7200) {
	      return "one hour ago";
	    } else if (diff < 86400) {
	      return diff / 3600 + " hours ago";
	    } else if (dayDiff == 1) {
	      return "yesterday";
	    } else if (dayDiff < 7) {
	      return dayDiff + " days ago";
	    } else {
	      return Math.ceil(dayDiff / 7) + " weeks ago";
	    }
	  }
}
