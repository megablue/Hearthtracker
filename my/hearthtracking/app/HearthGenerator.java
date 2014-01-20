package my.hearthtracking.app;

import java.awt.image.BufferedImage;
import java.io.File;

import my.hearthtracking.app.HearthScannerSettings.Scanbox;

public class HearthGenerator {

	public static void main(String[] args) {
		HearthScannerSettings settings = new HearthScannerSettings();
		String targetScene = "myHero";
		
		for(Scanbox sb : settings.list){
			
			if(sb.scene.equals(targetScene)){
				System.out.println("Generating target image: " 
						+ sb.imgfile 
						+ " on offset X: " + sb.xOffset 
						+ " Y:" + sb.yOffset 
						+ ", size: " + sb.width + "x" + sb.height 
				);
				
				BufferedImage gameScreen = HearthHelper.loadImage(new File("./sample-images/" + sb.scene + "/" + sb.identifier + ".png"));
				BufferedImage viewport = HearthHelper.cropImage(gameScreen, 240, 0, 1440, 1080);
				BufferedImage targetRegion = HearthHelper.cropImage(viewport, sb.xOffset, sb.yOffset, sb.width, sb.height);
				HearthHelper.bufferedImageToFile(targetRegion, "./cache/" + sb.imgfile);
			}
		}
	}

}
