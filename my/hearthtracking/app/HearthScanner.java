package my.hearthtracking.app;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.sikuli.core.search.RegionMatch;
import org.sikuli.core.search.algorithm.TemplateMatcher;

import my.hearthtracking.app.HearthScannerSettings.Scanbox;

public class HearthScanner{
	private float scale = 1f;
	private List<Scanbox> scanBoxes = Collections.synchronizedList(new ArrayList<Scanbox>());
	
	private List<BufferedImage> gameScreens = Collections.synchronizedList(new ArrayList<BufferedImage>()); 
	private ConcurrentHashMap<String, String> scanboxHashes = new ConcurrentHashMap<String, String>();
	
	private ImagePHash pHash = new ImagePHash();

	private boolean generateBetterOffsets = true;

	private static final int DISTANCE_THRESHOLD = 16;
	
	public HearthScanner() {
		
	}
		
	public synchronized void setScale(float s){
		scale = s;
		clearCache();
		init();
	}

	public synchronized void init(){
		for(Scanbox sb : scanBoxes) {
			String key = sb.imgfile;
			String hash = scanboxHashes.get(key);

			if(hash == null){
				BufferedImage resizedTarget = HearthHelper.resizeImage(sb.target.getImage(), scale);
				hash = pHash.getHash(resizedTarget);
				scanboxHashes.put(key, hash);
			}
			
			System.out.println(
				sb.imgfile + ", "
				+ "hash: " + hash
			);
		}
	}
	
	public synchronized void insertFrame(BufferedImage screen){
		gameScreens.add(screen);
	}
	
	public synchronized void addScanbox(Scanbox sb){
		System.out.println(
			"Scanbox added: " + sb.imgfile + ", \t\t"
			+ "offset: " + sb.xOffset + ", " + sb.yOffset + ", \t"
			+ sb.width + "x" + sb.height 
		);
		
		scanBoxes.add(sb);
	}
	
	public synchronized void clearCache(){
		gameScreens.clear();
		scanboxHashes.clear();
	}
	
	private int scale(int n){
		return (int) Math.round(n * scale);
	}
	
	private int unscale(int scaledValue){
		return (int) Math.round(scaledValue / scale);
	}
	
	private int unscale(double scaledValue){
		return (int) Math.round(scaledValue / scale);
	}
	
	public synchronized void scan(){
		if(gameScreens.isEmpty()){
			return;
		}
		
		//grab one of the frame/screen
		BufferedImage screen = gameScreens.get(0);
		
		//region -> phash
		Hashtable<String, String> roiHashes = new Hashtable<String, String>();
		//region -> BufferedImage
		Hashtable<String, BufferedImage> roiSnaps = new Hashtable<String, BufferedImage>();
		
		//crop corresponding parts and generate hashes from ROIs
		for(Scanbox sb : scanBoxes){
			String key = scale(sb.xOffset) 	+ "x" + 
						 scale(sb.yOffset)	+ "x" + 
						 scale(sb.width) 	+ "x" + 
						 scale(sb.height);
			BufferedImage roiSnapshot = roiSnaps.get(key);
			String hash = roiHashes.get(key);
			
			//if hash is not found
			if(roiSnapshot == null || hash == null){

				//crop the corresponding part from game screen
				roiSnapshot = HearthHelper.cropImage(
					screen, 
					scale(sb.xOffset), 
					scale(sb.yOffset), 
					scale(sb.width), 
					scale(sb.height)
				);
				
				//insert into table
				roiSnaps.put(key, roiSnapshot);
				
				//generate a new hash
				hash = pHash.getHash(roiSnapshot);
				
				//insert into table
				roiHashes.put(key, hash);
			}
		}
		
		for(Scanbox sb : scanBoxes){
			String key = scale(sb.xOffset) 	+ "x" + 
						 scale(sb.yOffset)	+ "x" + 
						 scale(sb.width) 	+ "x" + 
						 scale(sb.height);

			String targetHash = scanboxHashes.get(sb.imgfile);
			String regionHash = roiHashes.get(key);
			//compare differences between screen region and target
			int distance = pHash.distance(targetHash, regionHash);
			
			System.out.println(sb.imgfile + "-" + key + ", distance: " + distance);
			
			long id = System.currentTimeMillis() % 1000;
			
			String file1 = String.format(HearthFilesNameManager.scannerImageCacheFile, key + "-" + id +"-target.png");
			String file2 = String.format(HearthFilesNameManager.scannerImageCacheFile, key + "-" + id +"-screen.png");
			
			BufferedImage target = sb.target.getImage();
			BufferedImage region = roiSnaps.get(key);
					
			if(distance < DISTANCE_THRESHOLD){		
				System.out.println("Possible match at " + scale(sb.xOffset) + ", " + scale(sb.yOffset));

				Rectangle rec = skFind(target, region, sb.matchQuality);

				if(rec == null){
					System.out.println("Double checked, It is a mismatch");
				} else{
					System.out.println("Double checked, Found on " + rec.x + ", " + rec.y);
				}
			} else {
				Rectangle rec = skFind(target, region, sb.matchQuality);
				
				if(rec == null){
					System.out.println("Not found.");
				} else{
					System.out.println("Found on " + rec.x + ", " + rec.y);
					sb.xOffset = sb.xOffset + unscale(rec.x);
					sb.yOffset = sb.yOffset + unscale(rec.y);
					sb.width   = unscale(rec.getWidth());
					sb.height  = unscale(rec.getHeight());
				}
			}
		}
		
		gameScreens.remove(0);
	}
	
	private Rectangle skFind(BufferedImage target, BufferedImage screenImage, float score) {
		score = (score == -1) ? 0.7f : score;
		
		if (screenImage.getWidth() < target.getWidth() || screenImage.getHeight() < target.getHeight()){
			return null;
		}
		
		List<RegionMatch> matches;
		Rectangle rec = null;
		matches = TemplateMatcher.findMatchesByGrayscaleAtOriginalResolution(screenImage, target, 1, score);
		
		if(matches.size() > 0){
			RegionMatch r = matches.get(0);
			rec = new Rectangle(r.x, r.y, r.width, r.height);
		}
	
		return rec;
	}
		
	public synchronized void pause(){
		
	}
	
	public synchronized void resume(){
		
	}
}
