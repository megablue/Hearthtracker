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
import my.hearthtracking.app.HearthScannerSettings.Scanbox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class HearthScanner{
	private HearthLogger logger = HearthLogger.getInstance();
	
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
	private List<HearthScanResult> sceneResults = Collections.synchronizedList(new ArrayList<HearthScanResult>());
	
	//list of queries
	private List<String> queries = Collections.synchronizedList(new ArrayList<String>());

	//pHash generator
	private HearthImagePHash pHash = new HearthImagePHash(PHASH_SIZE, PHASH_MIN_SIZE);
	
	//Surf 
	private HearthImageSurf surf = new HearthImageSurf();

	//whatever to save the self-corrected offsets
	private boolean generateBetterOffsets = true;
	
	private boolean scanStarted = false;
	
	private volatile boolean shutdown = false;
	
	private volatile boolean threadRunning = false; 
	
	private long idleTime = 50;
	
	private long counter = 0;
	
	private long frameCount = 0;
		
	/**
	 * Initialze or Reinistalize the scanner when scale/resolution/scanboxes are changed
	 *
	 * @param  scale  the scale of the game screen in relative to the base resolution height (usually 1080)
	 */
	public synchronized void initScale(float scale){
		this.scale = scale;
		scanboxHashes.clear();
		resetFrames();
		resetScanboxHashes();
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
				scanboxHashes.putIfAbsent(key, hash);
			}
			
			logger.finest(sb.imgfile + ", " + "hash: " + hash);
		}
	}
		
	public void insertFrame(BufferedImage screen){
		synchronized(gameScreens){
			if(gameScreens.size() <= FRAMES_LIMIT){
				gameScreens.add(screen);
				frameCount++;
			}
		}
	}
		
	public void addScanbox(Scanbox sb){
		logger.finest(
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
	
	public List<HearthScanResult> getQueryResults(){
		synchronized(sceneResults){
			if(sceneResults.isEmpty()){
				return null;
			}

			List<HearthScanResult> results = Collections.synchronizedList(new ArrayList<HearthScanResult>());
			
			for(HearthScanResult sr : sceneResults){
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
	}
	
	public void resetScanboxHashes(){
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
	
	public void checkTableSizes(){
		logger.finest("gameScreens: " + gameScreens.size());
		logger.finest("scanboxHashes: " + scanboxHashes.size());
		logger.finest("scanBoxes: " + allScanboxes.size());
		logger.finest("sceneResults: " + sceneResults.size());
		logger.finest("queries: " + queries.size());
	}
		
	private void startScan(){
		if(scanStarted){
			return;
		}
		
		Runnable runnable = new Runnable() {
 			public void run() {
 				threadRunning = true;
 				
 				logger.fine("startScan() started.");
 				
 				while(!shutdown){
 					checkTableSizes();
 					process();
 				}
 				
 				logger.fine("startScan() ended.");
 				
 				threadRunning = false;
 				scanStarted = false;
		    }
		};
	
		new Thread(runnable).start();
		scanStarted = true;
	}
	
	private void process(){
		BufferedImage screen = null;
		
		if(gameScreens.isEmpty()){
			try {
				Thread.sleep(idleTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return;
		}
		
		//wait for sync and pop the earliest frame
		synchronized(gameScreens){
			screen = HearthHelper.cloneImage(gameScreens.get(0));
			gameScreens.remove(0);
			
			if(gameScreens.size() > 1){
				logger.finest("Frames in queue: " + gameScreens.size());
			}
		}
		
		if(screen == null){
			logger.severe("scan(), screen is null. something gone horribly wrong!");
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
					logger.severe("Something gone horibbly wrong! Attempting to create too much thread!");
				}
								
				if(jobs[threadCounter] == null){
					jobs[threadCounter] = new  ScannerJob(threadCounter, screen);
					logger.finest("Creating scan thread #" + threadCounter + ".");
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
					logger.finest("Firing scan thread #" + i + ".");
				}
			}
			
			//signal jobs to shutdown once finished
			executor.shutdown();
			
			try {
				executor.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.severe("One or more of the scan thread timeout!");
				e.printStackTrace();
			}
			
			logger.finest("All scan threads finished!");
		}
		
		logger.finest("scanner->process() time spent: " + (System.currentTimeMillis() - startBench) + " ms");
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
					logger.severe("Something went horribly wrong! Trying to crop a larger area than the source image" );
					continue;
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
			logger.finest("Scanner Thread [" + threadId + "]" + " started");
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

			//if the score greater or equals the minimum threshold
			if(score >= PHASH_MIN_SCORE){	
				logger.fine("Thread [" + threadId + "] " + "Possible match at " + scale(sb.xOffset) 
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
					
					logger.fine("Thread [" + threadId + "] " + "Surf score: " + HearthHelper.formatNumber("0.00", surfScore));

					if(surfScore < sb.matchQuality){
						found = true;
					}

				} else {
					found = true;
				}
				
				//if found we will compare colors as well
				if(found && sb.matchColor){
					float colorScore = pHash.getRGBScore(targetHash, regionHash);
					
					logger.fine("Thread [" + threadId + "] " + "Color score: " + HearthHelper.formatNumber("0.00", colorScore));
					
					if(colorScore > sb.colorScore){
						found = true;
					} else{
						found = false;
					}
				}
			}
			
			if(found){
				logger.fine("Thread [" + threadId + "] " + "Result added: scene \"" + sb.scene + "\", identifier: " + sb.identifier);
				insertSceneResult(sb.scene, sb.identifier, score);
			}
		}
		
		if(MAX_THREADS > 1){
			logger.finest("Scanner Thread [" + threadId + "]" + " ended");
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
			logger.finest(
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
			sceneResults.add(new HearthScanResult(scene, result, score, frameCount));
		}
	}

	public void dispose(){
		shutdown = true;
	}
	
	//pause will block until job threads ended
	public void pause(){
		shutdown = true;
		
		logger.fine("HearthScanner() paused.");
		
		while(threadRunning){
			try {
				logger.fine("HearthScanner() waiting...");
				Thread.sleep(100);
			} catch (InterruptedException e) { }
		}
		
		shutdown = false;
	}
	
	public synchronized void resume(){
		logger.info("HearthScanner() resumed.");
		startScan();
	}
}
