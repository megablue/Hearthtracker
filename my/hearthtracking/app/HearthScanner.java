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
	private static final float PHASH_MIN_SCORE = 0.8f;
	
	//store the most current scale value
	private float scale = 1f;
	
	//list of assigned scanboxes
	private List<Scanbox> scanBoxes = Collections.synchronizedList(new ArrayList<Scanbox>());
	
	//list of frames
	private List<BufferedImage> gameScreens = Collections.synchronizedList(new ArrayList<BufferedImage>()); 
	
	//scanbox target image -> phash
	private ConcurrentHashMap<String, String> scanboxHashes = new ConcurrentHashMap<String, String>();
	
	//used to store recognition results
	private List<SceneResult> sceneResults = Collections.synchronizedList(new ArrayList<SceneResult>());
	
	//list of queries
	private List<String> queries = Collections.synchronizedList(new ArrayList<String>());

	//pHash generator
	private ImagePHash pHash = new ImagePHash();

	//whatever to save the self-corrected offsets
	private boolean generateBetterOffsets = true;
	
	
	public class SceneResult{
		String scene;
		String result;
		
		public SceneResult(String s, String r){
			scene = s;
			result = r;
		}
	}
	
	public HearthScanner() {
		
	}
	
	/**
	 * Initialze or Reinistalize the scanner when scale/resolution/scanboxes are changed
	 *
	 * @param  scale  the scale of the game screen in relative to the base resolution height (usually 1080)
	 */
	public synchronized void initScale(float scale){
		this.scale = scale;
		resetFrames();
		_init();
	}

	public synchronized void _init(){
		for(Scanbox sb : scanBoxes) {
			String masked = sb.mask != null ? "masked" : "";
			String key = masked + "-" + sb.imgfile;
			String hash = scanboxHashes.get(key);

			if(hash == null){
//				long startBench = System.currentTimeMillis();
				
				hash = pHash.getHash(sb.target.getImage());
				
//				long benchDiff = (System.currentTimeMillis() - startBench);
//				System.out.println("getHash() time spent: " + benchDiff + " ms");

				scanboxHashes.put(key, hash);
			}
			
			System.out.println(
				sb.imgfile + ", "
				+ "hash: " + hash
			);
		}
	}
	
	public float getPHashScore(String hash, int distance){
		int max = hash.length();
		float score = 1 - ((float)distance/max);
		return score;
	}
	
	public void insertFrame(BufferedImage screen){
		synchronized(gameScreens){
			gameScreens.add(screen);
		}
	}
		
	public void addScanbox(Scanbox sb){
		System.out.println(
			"Scanbox added: " + sb.imgfile + ", \t\t"
			+ "offset: " + sb.xOffset + ", " + sb.yOffset + ", \t"
			+ sb.width + "x" + sb.height 
		);
		
		synchronized(scanBoxes){
			scanBoxes.add(sb);
		}
	}
	
	public void addQuery(String scene){
		
		//make sure that we don't add the same query twice
		boolean found = queryExists(scene);
		
		synchronized(queries){
			if(!found){
				queries.add(scene);
			}
		}
	}
		
	private boolean queryExists(String scene){
		boolean found = false;
		
		synchronized(queries){
			for(String q : queries){
				if(q.equals(scene)){
					found = true;
					break;
				}
			}
		}
		
		return found;
	}
	
	public List<SceneResult> getQueryResults(){
		synchronized(sceneResults){
			if(sceneResults.isEmpty()){
				return null;
			}
			
			
			List<SceneResult> results = Collections.synchronizedList(new ArrayList<SceneResult>());
			
			for(SceneResult sr : sceneResults){
				//copy the results to the new list
				results.add(sr);
			}
			
			//clear existing
			sceneResults.clear();

			return results;
		}
	}
	
	public void resetQuery(){
		synchronized(queries){
			queries.clear();
		}
	}
	
	public void resetFrames(){
		
		synchronized(gameScreens){
			gameScreens.clear();
		}
		
		synchronized(scanboxHashes){
			scanboxHashes.clear();
		}
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
	
	public void scan(){
		BufferedImage screen = null;
		
		//wait for sync and pop the earliest frame
		synchronized(gameScreens){
			if(gameScreens.isEmpty()){
				return;
			}
			screen = HearthHelper.cloneImage(gameScreens.get(0));
			gameScreens.remove(0);
			System.out.println("Frames in queue: " + gameScreens.size());
		}
		
		if(screen == null){
			System.out.println("scan(), screen is null. something gone horribly wrong!");
			return;
		}
		
		//region -> phash
		Hashtable<String, String> roiHashes = new Hashtable<String, String>();
		//region -> BufferedImage
		Hashtable<String, BufferedImage> roiSnaps = new Hashtable<String, BufferedImage>();
		
		//crop corresponding parts and generate hashes from ROIs
		for(Scanbox sb : scanBoxes){
			String masked = sb.mask != null ? "masked" : "";
			String key = scale(sb.xOffset) 	+ "x" + 
						 scale(sb.yOffset)	+ "x" + 
						 scale(sb.width) 	+ "x" + 
						 scale(sb.height)	+ "x" +
						 masked;
			BufferedImage roiSnapshot = roiSnaps.get(key);
			String hash = roiHashes.get(key);
			
			//if hash or snapshot not found
			if(roiSnapshot == null || hash == null){

				//crop the corresponding part from game screen
				roiSnapshot = HearthHelper.cropImage(
					screen, 
					scale(sb.xOffset), 
					scale(sb.yOffset), 
					scale(sb.width), 
					scale(sb.height)
				);

				//if mask is defined
				if(sb.mask != null){
					HearthHelper.applyMaskImage(
						roiSnapshot, 
						scale(sb.mask.xOffset), 
						scale(sb.mask.yOffset), 
						scale(sb.mask.width), 
						scale(sb.mask.height)
					);
				}
				
				//insert into table
				roiSnaps.put(key, roiSnapshot);
				
				//generate a new hash
				hash = pHash.getHash(roiSnapshot);
				
				//insert into table
				roiHashes.put(key, hash);
			}
		}
		
		for(Scanbox sb : scanBoxes){
			boolean found = false;
			String masked = sb.mask != null ? "masked" : "";
			String key = scale(sb.xOffset) 	+ "x" + 
						 scale(sb.yOffset)	+ "x" + 
						 scale(sb.width) 	+ "x" + 
						 scale(sb.height)	+ "x" +
						 masked;

			String scanboxHashKey = masked + "-" + sb.imgfile;

			String targetHash = scanboxHashes.get(scanboxHashKey);
			String regionHash = roiHashes.get(key);
			//compare differences between screen region and target
			int distance = pHash.distance(targetHash, regionHash);
			
			System.out.println(sb.imgfile + "-" + key + ", distance: " + distance);
			
			// long id = System.currentTimeMillis() % 1000;
			// String file1 = String.format(HearthFilesNameManager.scannerImageCacheFile, key + "-" + id +"-target.png");
			// String file2 = String.format(HearthFilesNameManager.scannerImageCacheFile, key + "-" + id +"-screen.png");
			
			BufferedImage target = sb.target.getImage();
			BufferedImage region = roiSnaps.get(key);
			float score = getPHashScore(targetHash, distance);
			
			//if the score greater or equals the minimum threshold
			if(score >= PHASH_MIN_SCORE){		
				System.out.println("Possible match at " + scale(sb.xOffset) + ", " + scale(sb.yOffset) + " with score of " + score);

				Rectangle rec = skFind(target, region, sb.matchQuality);

				if(rec == null){
					System.out.println("Double checked, It is a mismatch");
				} else{
					found = true;
					System.out.println("Double checked, Found on " + rec.x + ", " + rec.y);
				}
			} else {
				Rectangle rec = skFind(target, region, sb.matchQuality);
				
				if(rec == null){
					System.out.println("Not found.");
				} else{
					found = true;
					System.out.println("Found on " + rec.x + ", " + rec.y);

					//try to make the offsets as precise as possible
					//a self-correct mechanism
					if(generateBetterOffsets){
						sb.xOffset = sb.xOffset + unscale(rec.x);
						sb.yOffset = sb.yOffset + unscale(rec.y);
						sb.width   = unscale(rec.getWidth());
						sb.height  = unscale(rec.getHeight());
					}
				}
			}
			
			if(found){
				
				if(queryExists(sb.scene)){
					System.out.println("Query found: adding scene to query results");
					insertSceneResult(sb.scene, sb.identifier);
				} else {
					System.out.println("Query not found.");
				}
				
			}
		}
	}
	
	private void insertSceneResult(String scene, String result){
		synchronized(sceneResults){
			sceneResults.add(new SceneResult(scene, result));
		}
	}

	private Rectangle skFind(BufferedImage target, BufferedImage screenImage, float score) {
		score = (score == -1) ? 0.7f : score;
		
		if (screenImage.getWidth() < target.getWidth() || screenImage.getHeight() < target.getHeight()){
			return null;
		}
		
		List<RegionMatch> matches;
		Rectangle rec = null;
		int limit = 1;
		matches = TemplateMatcher.findMatchesByGrayscaleAtOriginalResolution(screenImage, target, limit, score);
		
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
