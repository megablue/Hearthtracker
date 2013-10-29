import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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


}
