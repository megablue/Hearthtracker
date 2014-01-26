package my.hearthtracking.app;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;

import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.struct.feature.Match;
import boofcv.struct.feature.SurfFeature;
import boofcv.struct.image.ImageFloat32;
import my.hearthtracking.app.HearthScanner.SceneResult;
import my.hearthtracking.app.HearthScannerSettings.Scanbox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class HearthScanner{
	private static final boolean 	DEBUGMODE = HearthHelper.isDevelopmentEnvironment();
	private static final int		FRAMES_LIMIT = 3;
	private static final int		PHASH_SIZE = 32;
	private static final int		PHASH_MIN_SIZE = 16;
	private static final float		PHASH_MIN_SCORE = 0.75f;
	
	//number of threads
	private int MAX_THREADS = 1;
	
	//store the most current scale value
	private float scale = 1f;
	
	//list of assigned scanboxes
	private List<Scanbox> allScanboxes = Collections.synchronizedList(new ArrayList<Scanbox>());
	
	//list of frames
	private List<BufferedImage> gameScreens = Collections.synchronizedList(new ArrayList<BufferedImage>()); 
	
	//scanbox target image -> phash
	private ConcurrentHashMap<String, String> scanboxHashes = new ConcurrentHashMap<String, String>();
	
	//to cache SURF's Describer
	private ConcurrentHashMap<String, DetectDescribePoint<ImageFloat32,SurfFeature>> surfCaches = new ConcurrentHashMap<String, DetectDescribePoint<ImageFloat32,SurfFeature>>();
	
	//used to store recognition results
	private List<SceneResult> sceneResults = Collections.synchronizedList(new ArrayList<SceneResult>());
	
	//list of queries
	private List<String> queries = Collections.synchronizedList(new ArrayList<String>());

	//pHash generator
	private HearthImagePHash pHash = new HearthImagePHash(PHASH_SIZE, PHASH_MIN_SIZE);
	
	//Surf 
	private HearthImageSurf surf = new HearthImageSurf();

	//whatever to save the self-corrected offsets
	private boolean generateBetterOffsets = true;
	
	private boolean scanStarted = false;
	
	private volatile static boolean shutdown = false;
	
	private volatile static boolean threadRunning = false; 
	
	private long idleTime = 50;
	
	private long counter = 0;

	public class SceneResult{
		String scene;
		String result;
		float score;
		BufferedImage match = null;
		
		public SceneResult(String s, String r, float scr){
			scene = s;
			result = r;
			score = scr;
		}
		
		public SceneResult(String s, String r, float scr, BufferedImage i){
			scene = s;
			result = r;
			match = i;
		}
	}
		
	/**
	 * Initialze or Reinistalize the scanner when scale/resolution/scanboxes are changed
	 *
	 * @param  scale  the scale of the game screen in relative to the base resolution height (usually 1080)
	 */
	public synchronized void initScale(float scale){
		this.scale = scale;
		scanboxHashes.clear();
		resetFrames();
		_init();
		startScan();
	}
	
	public synchronized void _init(){
		for(Scanbox sb : allScanboxes) {
			String masked = sb.mask != null ? "masked" : "";
			String key = masked + "-" + sb.imgfile;
			String hash = scanboxHashes.get(key);

			if(hash == null){
				hash = pHash.getHash(sb.target);
				scanboxHashes.put(key, hash);
			}
			
			System.out.println(
				sb.imgfile + ", "
				+ "hash: " + hash
			);
		}
	}
		
	public void insertFrame(BufferedImage screen){
		synchronized(gameScreens){
			if(gameScreens.size() <= FRAMES_LIMIT){
				gameScreens.add(screen);
			}
		}
	}
		
	public void addScanbox(Scanbox sb){
		System.out.println(
			"Scanbox added: " + sb.imgfile + ", \t\t"
			+ "offset: " + sb.xOffset + ", " + sb.yOffset + ", \t"
			+ sb.width + "x" + sb.height 
		);
		
		synchronized(allScanboxes){
			allScanboxes.add(sb);
		}
	}
	
	public void clearScanboxes(){
		synchronized(allScanboxes){
			allScanboxes.clear();
		}
	}
	
	public void subscribe(String scene){
		//make sure that we don't add the same query twice
		boolean found = queryExists(scene);
		
		synchronized(queries){
			if(!found){
				queries.add(scene);
			}
		}
	}
	
	public void unsubscribe(String target){
		synchronized(queries){
			Iterator<String> it = queries.iterator();
			
			while(it.hasNext()){
				String scene = it.next();
				if(scene.equals(target)){
					it.remove();
					break;
				}
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
	
	public void checkTableSizes(){
		System.out.println("gameScreens: " + gameScreens.size());
		System.out.println("scanboxHashes: " + scanboxHashes.size());
		System.out.println("scanBoxes: " + allScanboxes.size());
		System.out.println("sceneResults: " + sceneResults.size());
		System.out.println("queries: " + queries.size());
	}
		
	private void startScan(){
		if(scanStarted){
			return;
		}
		
		Runnable runnable = new Runnable() {
 			public void run() {
 				threadRunning = true;
 				
 				while(!shutdown){
 					checkTableSizes();
 					process();
 				}
 				
 				threadRunning = false;
 				scanStarted = false;
		    }
		};
		
		new Thread(runnable).start();
		scanStarted = true;
	}
	
	private void process(){
		BufferedImage screen = null;
		
		//wait for sync and pop the earliest frame
		synchronized(gameScreens){
			if(gameScreens.isEmpty()){
				try {
					Thread.sleep(idleTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return;
			}
			screen = HearthHelper.cloneImage(gameScreens.get(0));
			gameScreens.remove(0);
			
			if(gameScreens.size() > 1){
				System.out.println("Frames in queue: " + gameScreens.size());
			}
		}
		
		if(screen == null){
			System.out.println("scan(), screen is null. something gone horribly wrong!");
			return;
		}
		
		long startBench = System.currentTimeMillis();

		//if the number of thread is less than 2
		if(MAX_THREADS < 2){
			int threadID = 0;
			
			//region -> phash
			Hashtable<String, String> roiHashes = new Hashtable<String, String>();
			
			//region -> BufferedImage
			Hashtable<String, BufferedImage> roiSnaps = new Hashtable<String, BufferedImage>();
			
			//prepare the scan
			prepScan(threadID, screen, allScanboxes, roiHashes, roiSnaps);
			
			//we can just start the scan() process using the current thread
			scan(threadID, screen, allScanboxes, roiHashes, roiSnaps);

		} else {
			int boxesPerThread = (int) Math.ceil( (allScanboxes.size())/ (double) MAX_THREADS);
			
			ScannerJob[] jobs = new ScannerJob[MAX_THREADS];
			ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
			
			int threadCounter = 0;
			int boxesCounter = 0;
			
			for(Scanbox sb : allScanboxes){
				
				if(threadCounter >= MAX_THREADS){
					System.out.println("Something gone horibbly wrong! Attempting to create too much thread!");
				}
								
				if(jobs[threadCounter] == null){
					jobs[threadCounter] = new  ScannerJob(threadCounter, screen);
					System.out.println("Creating scan thread #" + threadCounter + ".");
				}
				
				jobs[threadCounter].addScanbox(sb);
				
				if(boxesCounter != 0 && boxesCounter % boxesPerThread == 0){
					++threadCounter;
				}
				
				++boxesCounter;
			}
			
			for(int i = 0; i < jobs.length; i++){
				if(jobs[i] != null){
					executor.execute(jobs[i]);
					System.out.println("Firing scan thread #" + i + ".");
				}
			}
			
			//signal jobs to shutdown once finished
			executor.shutdown();
			
			try {
				executor.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				System.out.println("One or more of the scan thread timeout!");
				e.printStackTrace();
			}
			
			System.out.println("All scan threads finished!");
		}
		
		
		System.out.println("scanner->process() time spent: " + (System.currentTimeMillis() - startBench) + " ms");
	}
	
	private void prepScan(int threadId, BufferedImage screen, List<Scanbox> scanBoxes, Hashtable<String, String> roiHashes, Hashtable<String, BufferedImage> roiSnaps){
		//crop corresponding parts and generate hashes from ROIs
		for(Scanbox sb : scanBoxes){
			
			//skips if query doesn't exists
			if(!queryExists(sb.scene)){
				continue;
			}
			
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
				
				if(( scale(sb.xOffset) + scale(sb.width)) > screen.getWidth() || ( scale(sb.yOffset) + scale(sb.height)) > screen.getHeight()){
					System.out.println("Something went horribly wrong! Trying to crop a larger area than the source image" );
				}
				
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
				
				//generate hashes for ROI
				hash = pHash.getHash(roiSnapshot);
				
				//insert into table
				roiHashes.put(key, hash);
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public void scan(int threadId, BufferedImage screen, List<Scanbox> scanBoxes, Hashtable<String, String> roiHashes, Hashtable<String, BufferedImage> roiSnaps){
		
		if(MAX_THREADS > 1){
			System.out.println("Scanner Thread [" + threadId + "]" + " started");
		}	
		
		for(Scanbox sb : scanBoxes){
			
			//skips if query doesn't exists
			if(!queryExists(sb.scene)){
				continue;
			}
			
			boolean found = false;
			String masked = sb.mask != null ? "xmasked" : "";
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

			//convert distance to score
			float score = pHash.getPHashScore(targetHash, distance);

			if(score >= 0.7){
				System.out.println(
					"Thread [" + threadId + "] "
					+ sb.scene + " "
					+ sb.imgfile 
					+ "-" + key 
					+ ", score: " 
					+ HearthHelper.formatNumber("0.00", score)
				);
			}

			//if the score greater or equals the minimum threshold
			if(score >= PHASH_MIN_SCORE){	
				System.out.println("Thread [" + threadId + "] " + "Possible match at " + scale(sb.xOffset) 
					+ ", " + scale(sb.yOffset) 
					+ " with score of " + HearthHelper.formatNumber("0.00", score)
				);

				if(sb.resolveConflict){
					BufferedImage target = sb.unScaledTarget;
					BufferedImage region = roiSnaps.get(key);
					
					//if the viewport ratio is diff
					if(this.scale != 1f){
						//scale region to match the original target ratio
						region = HearthHelper.resizeImage(region, 1f/this.scale);
					}

					float surfScore = surf.compare(target, region);
					
					System.out.println("Thread [" + threadId + "] " + "Surf score: " + HearthHelper.formatNumber("0.00", surfScore));

					if(surfScore < sb.matchQuality){
						found = true;
					}

				} else {
					found = true;
				}
				
				//if found we will compare colors as well
				if(found && sb.matchColor){
					float colorScore = pHash.getRGBScore(targetHash, regionHash);
					
					System.out.println("Thread [" + threadId + "] " + "Color score: " + HearthHelper.formatNumber("0.00", colorScore));
					
					if(colorScore > 0.9){
						found = true;
					} else{
						found = false;
					}
				}
			}
			
			if(found){
				System.out.println("Thread [" + threadId + "] " + "Query found: scene \"" + sb.scene + "\" added to query results");
				insertSceneResult(sb.scene, sb.identifier, score);
			}
		}
		
		if(MAX_THREADS > 1){
			System.out.println("Scanner Thread [" + threadId + "]" + " ended");
		}
	}
	
	public class ScannerJob implements Runnable {
		private List<Scanbox> scanBoxes = Collections.synchronizedList(new ArrayList<Scanbox>());
		private int myThreadId = 0;
		BufferedImage myScreen = null;
		
		//region -> phash
		Hashtable<String, String> roiHashes = new Hashtable<String, String>();
		
		//region -> BufferedImage
		Hashtable<String, BufferedImage> roiSnaps = new Hashtable<String, BufferedImage>();
		
		public ScannerJob(int threadId, BufferedImage screen){
			myThreadId = threadId;
			myScreen = screen;
		}
		
		public void addScanbox(Scanbox sb){
			System.out.println(
				"Thread[" + myThreadId + "]" + " Scanbox added: " + sb.imgfile + ", \t\t"
				+ "offset: " + sb.xOffset + ", " + sb.yOffset + ", \t"
				+ sb.width + "x" + sb.height 
			);
			
			synchronized(scanBoxes){
				scanBoxes.add(sb);
			}
		}
		
	    public void run() {
	    	synchronized(this){
				prepScan(myThreadId, myScreen, scanBoxes, roiHashes, roiSnaps);
	    		scan(myThreadId, myScreen, scanBoxes, roiHashes, roiSnaps);
	    		notify();
	    	}
	    }
	}
	
	private void insertSceneResult(String scene, String result, float score){
		synchronized(sceneResults){
			//insert
			sceneResults.add(new SceneResult(scene, result, score));
		}
	}
	
	private void insertSceneResult(String scene, String result, float score, BufferedImage region){
		synchronized(sceneResults){
			sceneResults.add(new SceneResult(scene, result, score, region));
		}
	}
	
	public void dispose(){
		shutdown = true;
	}
	
	//pause will block until job threads ended
	public synchronized void pause(){
		shutdown = true;
		
		while(threadRunning){
			try {
				System.out.println("waiting...");
				Thread.sleep(100);
			} catch (InterruptedException e) { }
		}
		
		System.out.println("paused");
		
		shutdown = false;
	}
	
	public synchronized void resume(){
		startScan();
	}
	
//	private Rectangle skFind(BufferedImage target, BufferedImage screenImage, float score) {
//		score = (score == -1) ? 0.7f : score;
//		
//		if (screenImage.getWidth() < target.getWidth() || screenImage.getHeight() < target.getHeight()){			
//			//dirty fix
//			target = HearthHelper.resizeImage(target, Math.round(screenImage.getWidth()),Math.round(screenImage.getHeight()));
//		}
//		
//		List<RegionMatch> matches;
//		Rectangle rec = null;
//		int limit = 1;
//		matches = TemplateMatcher.findMatchesByGrayscaleAtOriginalResolution(screenImage, target, limit, score);
//		
//		if(matches.size() > 0){
//			RegionMatch r = matches.get(0);
//			rec = new Rectangle(r.x, r.y, r.width, r.height);
//		}
//	
//		return rec;
//	}
	
//	private Rectangle skFind(BufferedImage target, BufferedImage screenImage, float score) {
//		//score = (score == -1) ? 0.5f : score;
//		Rectangle rec = null;
//		
////		Finder finder = new Finder(
////				new ScreenImage(new Rectangle(0,0, screenImage.getWidth(), screenImage.getHeight()), screenImage), 
////				new Region(0,0,	screenImage.getWidth(), screenImage.getHeight())
////		);
//		
//		Finder finder = new Finder(screenImage);
//		
//		Pattern pattern = new Pattern(target);
//		
//		pattern.similar(0.1f);
//		
//		finder.findAll(pattern);
//		
//		if(finder.hasNext()){
//			Match match = finder.next();
//		
//			System.out.println("finder.hasNext()");
//			
//			if(match != null){
//				rec = new Rectangle(match.x, match.y, match.w, match.h);
//				
//				System.out.println("match finder.hasNext()");
//			}
//		}
//		
//		return rec;
//	}
}
