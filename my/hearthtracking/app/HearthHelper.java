package my.hearthtracking.app;

import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Date;
import javax.imageio.ImageIO;

public class HearthHelper {
	private static HearthLogger logger = HearthLogger.getInstance();

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
	
	public static BufferedImage loadImage(File imgFile){
		try {
			return ImageIO.read(imgFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
		
	public static void applyMaskImage(BufferedImage src, int x, int y, int w, int h){

		if(x < 0){
			x = 0;
		}
		
		if(y < 0){
			y = 0;
		}
		
		int maxOffsetX = x + w;
		int maxOffsetY = y + h;
		
		for(int xCounter = x; xCounter < maxOffsetX; xCounter++){
			for(int yCoutner = y; yCoutner < maxOffsetY; yCoutner++){
				
				if(yCoutner > src.getHeight() || xCounter > src.getWidth()){
					break;
				}
				
				src.setRGB(xCounter, yCoutner, 0);
			}
		}
	}
	
	public static BufferedImage cropImage(BufferedImage src, int x, int y, int w, int h){
		BufferedImage dest = src.getSubimage(x, y, w, h);
		return dest;
	}
	
	public static boolean bufferedImageToFile(BufferedImage src, String path){
		try {
			File outputFile = new File(path);
			ImageIO.write(src, "png", outputFile);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	static BufferedImage cloneImage(BufferedImage bi) {
		 ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	public static BufferedImage resizeImage(File imgFile, float scaleFactor){
		BufferedImage sourceImage = loadImage(imgFile);
		return resizeImage(sourceImage, scaleFactor);
	}
	
	public static BufferedImage resizeImage(BufferedImage sourceImage, float scaleFactor){
		int resizedWidth = (int) Math.round(sourceImage.getWidth() * scaleFactor);  
		Image thumbnail = sourceImage.getScaledInstance(resizedWidth, -1, Image.SCALE_SMOOTH);
		BufferedImage bufferedThumbnail = new BufferedImage(thumbnail.getWidth(null),
		                                                    thumbnail.getHeight(null),
		                                                    BufferedImage.TYPE_INT_RGB);
		bufferedThumbnail.getGraphics().drawImage(thumbnail, 0, 0, null);
		
		return bufferedThumbnail;
	}

		public static BufferedImage resizeImage(BufferedImage sourceImage, int width, int height){
		Image thumbnail = sourceImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage bufferedThumbnail = new BufferedImage(thumbnail.getWidth(null),
		                                                    thumbnail.getHeight(null),
		                                                    BufferedImage.TYPE_INT_RGB);
		bufferedThumbnail.getGraphics().drawImage(thumbnail, 0, 0, null);
		
		return bufferedThumbnail;
	}
	
	public static BufferedImage resizeImage(File imgFile, float scaleFactor, File outputFile){
		BufferedImage resizedBuffer = resizeImage(imgFile, scaleFactor);
		try {
			ImageIO.write(resizedBuffer, "png", outputFile);
			return resizedBuffer;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);

	public static BufferedImage imageToGrayscale(BufferedImage img) {
        colorConvert.filter(img, img);
        return img;
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
			boolean result = theDir.mkdir();  
		
			if(!result) {    
				logger.severe("Failed to create folder: " + folder);
			}
		}
	}
	
	public static String extractFolderPath(String path)
	{
		return path.substring(0,path.lastIndexOf(File.separator));
	}
	
	public static boolean fileExists(String file)
	{
		File theFile = new File(file);
		
		if (theFile.exists()) {
			return true;
		}
		
		return false;
	}
	
	public static int getHearthstoneHandle(){
		if(!getOSName().equals("win")){
			return 0;
		}
		
		return HearthWin32Helper.findWindow("UnityWndClass", "Hearthstone");
	}

	public static int[] getHearthstonePosition(){
		int[] pos = {0,0,0,0};

		if(!getOSName().equals("win")){
			return pos;
		}
		
		try {			
			int[] rect = HearthWin32Helper.getRect("UnityWndClass", "Hearthstone");
			
			pos[0] = rect[0];
			pos[1] = rect[1];
			pos[2] = rect[2];
			pos[3] = rect[3];
		} catch (Throwable e) {
			//System.out.println(e.getMessage());
		}
		
		return pos;
	}
		
	public static boolean isHSDetected(){
		if(!getOSName().equals("win")){
			return true;
		}
		
		if(getHearthstoneHandle() != 0){
			return true;
		}
		
		return false;
	}
	
	public static boolean isHearthstoneMinimized(){
		if(!getOSName().equals("win")){
			return false;
		}
		
		int handle = getHearthstoneHandle();
		boolean result = HearthWin32Helper.isWindowMinimized(handle);
		
		//always return false if handle not found
		//this is to make sure we don't need additional check
		//when we do "forced" scanning
		if(handle == 0){
			return false;
		}
		
		return result;
	}
	
	public static void openLink(String link){
		try {
			java.awt.Desktop.getDesktop().browse(new URL(link).toURI());
		}catch (Throwable e) {
			//e.printStackTrace();
		}
	}
	
	public static void openDonateLink(){
		openLink("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=YNFGYE9V386UQ");
	}
	
	public static String getPrettyText(Date date) {
	    long diff = (new Date().getTime() - date.getTime()) / 1000;
	    double dayDiff = Math.floor(diff / 86400);
	    HearthLanguageManager uiLang = HearthLanguageManager.getInstance();

	    if (diff < 0) {
	      return uiLang.t("in the future?");
	    } else if (diff < 60) {
	      return uiLang.t("moments ago");
	    } else if (diff < 120) {
	      return uiLang.t("one minute ago");
	    } else if (diff < 3600) {
	      return uiLang.t("%d minutes ago", diff / 60);
	    } else if (diff < 7200) {
	      return uiLang.t("one hour ago");
	    } else if (diff < 86400) {
	      return uiLang.t("%d hours ago", diff / 3600);
	    } else if (dayDiff == 1) {
	      return uiLang.t("yesterday");
	    } else if (dayDiff < 7) {
	      return uiLang.t("%d days ago", dayDiff);
	    } else {
	      return uiLang.t("%d weeks ago", Math.ceil(dayDiff / 7));
	    }
	 }
	
	public static String goesFirstToString(int goesFirst){
		switch(goesFirst){
			case 1:
				return "First";
	
			case 0:
				return "Second";
		}
		
		return "Unknown";
	}
	
	public static String gameModeToString(int mode){
		switch(mode){
			case HearthScannerManager.ARENAMODE:
				return "Arena";
	
			case HearthScannerManager.RANKEDMODE:
				return "Ranked";
		
			case HearthScannerManager.UNRANKEDMODE:
				return "Unranked";
			
			case HearthScannerManager.PRACTICEMODE:
				return"Practice";
				
			case HearthScannerManager.CHALLENGEMODE:
				return "Challenge";
		}
		
		return "Unknown mode";
	}
	
	public static String gameModeToStringLabel(int mode){
		HearthLanguageManager uiLang = HearthLanguageManager.getInstance();
		
		switch(mode){
			case HearthScannerManager.ARENAMODE:
				return uiLang.t("Arena");
	
			case HearthScannerManager.RANKEDMODE:
				return uiLang.t("Ranked");
		
			case HearthScannerManager.UNRANKEDMODE:
				return uiLang.t("Unranked");
			
			case HearthScannerManager.PRACTICEMODE:
				return uiLang.t("Practice");
				
			case HearthScannerManager.CHALLENGEMODE:
				return uiLang.t("Challenge");
		}
		
		return uiLang.t("Unknown mode");
	}

	public static String formatNumber(String format, Float number){
		return new DecimalFormat("0.00").format(number);
	}
}
