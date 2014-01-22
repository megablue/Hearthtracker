package my.hearthtracking.app;

import java.awt.image.BufferedImage;
import java.io.File;

import my.hearthtracking.app.HearthScannerSettings.Scanbox;

public class HearthGenerator {

	public static void main(String[] args) {
		HearthScannerSettings settings = new HearthScannerSettings();
		String targetScene = "arenaLive";
		
		for(Scanbox sb : settings.list){
			
			if(targetScene.equals(sb.scene)){
				generateTarget(sb);
			}
		}
		
		//generateLinearMotionOffsets(5, 491, 524, 519, 488);
	}
	
	private static void generateTarget(Scanbox sb){
		System.out.println("Generating target image: " 
				+ sb.imgfile 
				+ " on offset X: " + sb.xOffset 
				+ " Y:" + sb.yOffset 
				+ ", size: " + sb.width + "x" + sb.height 
		);
		
		BufferedImage gameScreen = HearthHelper.loadImage(new File("./sample-images/" + sb.scene + "/" + sb.identifier + ".png"));
		BufferedImage viewport = HearthHelper.cropImage(gameScreen, 240, 0, 1440, 1080);
		BufferedImage targetRegion = HearthHelper.cropImage(viewport, sb.xOffset, sb.yOffset, sb.width, sb.height);
		HearthHelper.bufferedImageToFile(targetRegion, "./cache/" + sb.identifier + ".png");
	}
	
	private static void generateLinearMotionOffsets(int step, int startX, int startY, int endX, int endY){
		double m = (endY - startY)/(endX - startX);
		double y = startY;
		double x = startX;
		double b = y - m * x;
		//double distance = Math.sqrt(Math.pow((endX - startX),2) + Math.pow((endY - startY),2));
		
		System.out.println("Slope: " + m);
		System.out.println("b: " + b);
		
		for(int n = startX; n <= endX; n+=step){
			double yy = (m*n + b);
			System.out.println( n + ", " + (int)yy);
		}
	}

}
