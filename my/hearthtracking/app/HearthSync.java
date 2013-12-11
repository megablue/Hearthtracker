package my.hearthtracking.app;

import static us.monoid.web.Resty.*;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.google.gson.Gson;

import us.monoid.web.JSONResource;
import us.monoid.web.Resty;
import us.monoid.web.mime.MultipartContent;

public class HearthSync {
	
	private static final int BATCH_SIZE = 50;
	private static HearthConfigurator config = new HearthConfigurator();
	private String secretKey = "";
	private HearthTracker tracker;
	private static String API_VERSION = "v0";
	private String baseURL = HearthHelper.isDevelopmentEnvironment() 
			? "http://192.168.0.104:3000/api/" + API_VERSION + "/" 
			: "http://hearthtracking.com/api/" + API_VERSION + "/";
	private HearthSyncLog syncLog;
	
	private Resty resty = new Resty();
	
	private HearthHeroesList heroesList;
	
	private int nounce = 0;
	
	private Gson gson = new Gson();
	
	public class ArenaResult {
		public int cid;
		public String myhero;
		public int wins;
		public int losses;
		public long timecaptured;
		public int modified;
		public int deleted;
		public long lastmodified;
	}
	
	public class MatchResult {
		public int cid;
		public String myhero;
		public String opphero;
		public String goes;
		public int win;
		public long starttime;
		public int totaltime;
		public String mode;
		public int deleted;
		public int modified;
		public long lastmodified;
	}
	
	public HearthSync() {
		tracker = new HearthTracker();
		nounce = generateNounce();
		
		heroesList = (HearthHeroesList) config.load("." + File.separator + "configs" + File.separator + "heroes.xml");
		syncLog = (HearthSyncLog) config.load("." + File.separator + "configs" + File.separator + "sync.xml");
		
		if(syncLog == null){
			syncLog = new HearthSyncLog();
			saveSyncLog();
		}
		
		if(heroesList == null){
			heroesList = new HearthHeroesList();
			config.save(heroesList, "." + File.separator + "configs" + File.separator + "heroes.xml");
		}
		
		setKey(syncLog.secretKey);
	}
	
	private int generateNounce(){
	   int range = (1000000000) + 1;     
	   return (int)(Math.random() * range) + 1;
	}
	
	private int getNounce(){
		return nounce++;
	}
	
	private void saveSyncLog(){
		config.save(syncLog, "." + File.separator + "configs" + File.separator + "sync.xml");
	}
	
	public void setKey(String key){
		secretKey = key;
	}
	
	private String getKey(){
		return secretKey;
	}
		
	public boolean checkAccessKey(){
		String url = baseURL + "key_check/";

		MultipartContent formData = form(
			data("key", getKey()),
			data("nounce", getNounce() + "")
		);
		
		try {
			System.out.println(url);
			JSONResource jsonResult = resty.json(url, formData);
			System.out.print(jsonResult.toString());
			printEJSON(jsonResult);
		} catch (Throwable e) {
			System.out.println(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	private void printEJSON(JSONResource jsonResult){
		try {
			String result = (boolean) jsonResult.get("success") ? "Succeed" : "Failed";
			System.out.println("EJSON: " + jsonResult.get("message") + ", " + result);
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
	}
	
	public boolean syncArena(int cid, String myhero, int wins, 
			int losses, long timecaptured, int modified,
			int deleted, long lastmodified){
		
		String url = baseURL + "arena_sync/";
		boolean success = false;
		
		MultipartContent formData = form(
			data("key", 			getKey()		+ ""),
			data("nounce", 			getNounce() 	+ ""),
			data("cid", 			cid				+ ""),
			data("myhero", 			myhero			+ ""),
			data("wins", 			wins 			+ ""),
			data("losses", 			losses 			+ ""),
			data("timecaptured",	timecaptured	+ ""),
			data("modified", 		modified 		+ ""),
			data("deleted", 		deleted 		+ ""),
			data("lastmodified", 	lastmodified 	+ "")
		);
			
		try {
			System.out.println("Attempting to sync arena result (" + cid + ")");
			System.out.println("URL: " + url);
			JSONResource jsonResult = resty.json(url, formData);
			
			success = (boolean) jsonResult.get("success");
			
			if(success){
				tracker.updateArenaResultSyncTime(cid, new Date().getTime());
			}
			
			syncLog.lastSync = new Date().getTime();
			printEJSON(jsonResult);
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
		
		return success;
	}
	
	public boolean syncMatch(int cid, String myhero, String opphero, 
			String goes, int win, long starttime, int totaltime, String mode,
			int deleted, int modified, long lastmodified){
		
		String url = baseURL + "match_sync/";
		
		JSONResource jsonResult;
		boolean success = false;
		
		MultipartContent formData = form(
				data("key", 			getKey()		+ ""),
				data("nounce", 			getNounce() 	+ ""),
				data("cid", 			cid				+ ""),
				data("myhero", 			myhero			+ ""),
				data("opphero", 		opphero			+ ""),
				data("goes", 			goes			+ ""),
				data("win", 			win 			+ ""),
				data("starttime", 		starttime 		+ ""),
				data("totaltime",		totaltime		+ ""),
				data("mode", 			mode 			+ ""),
				data("deleted", 		deleted 		+ ""),
				data("modified", 		modified 		+ ""),
				data("lastmodified", 	lastmodified 	+ "")
		);
		
		try {
			System.out.println("Attempting to sync match result (" + cid + ")");
			System.out.println("URL: " + url);
			jsonResult = resty.json(url, formData);
			success = (boolean) jsonResult.get("success");
			
			if(success){
				tracker.updateMatchResultSyncTime(cid, new Date().getTime());
			}
			
			syncLog.lastSync = new Date().getTime();
			printEJSON(jsonResult);
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
		
		return success;
	}
	
	//sync all records with single query mode
	public void syncAll(){
		boolean success = false;
		int errorCount = 0;
		int recordCount = 0;
		Date start = new Date();

		try {
			System.out.println("***Sync All Started***");
			ResultSet rs = tracker.getUnsyncArenaResults();
			
			while(rs.next()){
				recordCount++;
				
				success = syncArena(
					rs.getInt("id"),
					heroesList.getHeroLabel(rs.getInt("heroid")),
					rs.getInt("wins"),
					rs.getInt("losses"),
					rs.getLong("timecaptured"),
					rs.getInt("modified"),
					rs.getInt("deleted"),
					rs.getLong("lastmodified")
				);
				
				if(success){
					syncLog.lastSync = new Date().getTime();
					saveSyncLog();
				} else {
					System.out.println("Failed to sync arena result ( " + rs.getInt("id") + " )");
					errorCount++;
				}
				
				if(errorCount > 10){
					break;
				}
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		try {
			ResultSet rs = tracker.getUnsyncMatchResults();

			while(rs.next()){
				recordCount++;
					
				success = syncMatch(
					rs.getInt("id"),
					heroesList.getHeroLabel(rs.getInt("myheroid")),
					heroesList.getHeroLabel(rs.getInt("oppheroid")),
					HearthReader.goesFirstToString(rs.getInt("goesfirst")),
					rs.getInt("win"),
					rs.getLong("starttime"),
					rs.getInt("totaltime"),
					HearthReader.gameModeToString(rs.getInt("mode")),
					rs.getInt("deleted"),
					rs.getInt("modified"),
					rs.getLong("lastmodified")
				);
				
				if(success){
					syncLog.lastSync = new Date().getTime();
					saveSyncLog();
				} else {
					System.out.println("Failed to sync arena result ( " + rs.getInt("id") + " )");
					errorCount++;
				}
				
				if(errorCount > 10){
					break;
				}
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		float totalSeconds = (float)(new Date().getTime() - start.getTime())/1000;
		float average = (float) totalSeconds / recordCount;
		
		System.out.println("Total time used: " + (float)(totalSeconds/60) + " mins");
		System.out.println("Total records processeed: " + recordCount);
		if(recordCount > 0){
			System.out.println("Average time per record: " + average + " seconds");
		}
		
		System.out.println("***Sync All Ended***");
		saveSyncLog();
	}
	
	public boolean syncWithJsonString(String url, String jsonString){
		JSONResource jsonResult;
		
		MultipartContent formData = form(
				data("key", 			getKey()		+ ""),
				data("nounce", 			getNounce() 	+ ""),
				data("json", 			jsonString		+ "")
		);
		
		//System.out.println(jsonString);
		//System.out.println(url);
		
		try {
			jsonResult = resty.json(url, formData);
			printEJSON(jsonResult);
			return (boolean) jsonResult.get("success");
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
		
		return false;
	}
		
	public void syncArenaBatch(){
		//boolean success = false;
		int recordCount = 0;
		//Date start = new Date();
		ArenaResult[] ar = new ArenaResult[BATCH_SIZE];
		int[] resultIds = new int[BATCH_SIZE];
		int index = -1;
		String url = baseURL + "arena_batch_sync/";

		System.out.println("***Arena Batch Sync Started***");
		try {
			ResultSet rs = tracker.getUnsyncArenaResults();
			
			while(rs.next()){
				index = recordCount%BATCH_SIZE;
				
				if(ar[index] == null){
					ar[index] = new ArenaResult();
				}
				
				resultIds[index] 		= rs.getInt("id");
				ar[index].cid 			= rs.getInt("id");
				ar[index].myhero 		= heroesList.getHeroLabel(rs.getInt("heroid"));
				ar[index].wins 			= rs.getInt("wins");
				ar[index].losses 		= rs.getInt("losses");
				ar[index].timecaptured	= rs.getLong("timecaptured");
				ar[index].modified		= rs.getInt("modified");
				ar[index].deleted		= rs.getInt("deleted");
				ar[index].lastmodified	= rs.getLong("lastmodified");
				
				if(recordCount != 0 && index == (BATCH_SIZE - 1)){
					try {
						String json = gson.toJson(ar);
						if(syncWithJsonString(url, json)){
							tracker.updateArenaResultSyncTime(resultIds, new Date().getTime());
						}
					} catch (Throwable e) {
						System.out.println(e.getMessage());
					}
				}

				++recordCount;
			}
			
			//flush the remaining
			if(index > -1 && index != (BATCH_SIZE - 1)){
				ArenaResult[] arResized = new ArenaResult[index+1];
				int[] resultIdsResized = new int[index+1];
				System.arraycopy(ar, 0, arResized, 0, index+1);
				System.arraycopy(resultIds, 0, resultIdsResized, 0, index+1);
				
				String json;
				try {
					json = gson.toJson(arResized);
					if(syncWithJsonString(url, json)){
						tracker.updateArenaResultSyncTime(resultIdsResized, new Date().getTime());
					}
				} catch (Throwable e) {
					System.out.println(e.getMessage());
				}
			}
	
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("Affected records: " + recordCount);
		System.out.println("***Arena Batch Sync Ended***");
	}
	
	public void syncMatchBatch(){
		//boolean success = false;
		int recordCount = 0;
		//Date start = new Date();
		MatchResult[] ar = new MatchResult[BATCH_SIZE];
		int[] resultIds = new int[BATCH_SIZE];
		int index = -1;
		String url = baseURL + "match_batch_sync/";

		System.out.println("***Match Batch Sync Started***");
		try {
			ResultSet rs = tracker.getUnsyncMatchResults();
			
			while(rs.next()){
				index = recordCount%BATCH_SIZE;
				
				if(ar[index] == null){
					ar[index] = new MatchResult();
				}
				
				resultIds[index] 		= rs.getInt("id");
				ar[index].cid 			= rs.getInt("id");
				ar[index].myhero 		= heroesList.getHeroLabel(rs.getInt("myheroid"));
				ar[index].opphero		= heroesList.getHeroLabel(rs.getInt("oppheroid"));
				ar[index].goes			= HearthReader.goesFirstToString(rs.getInt("goesfirst"));
				ar[index].win			= rs.getInt("win");
				ar[index].starttime		= rs.getLong("starttime");
				ar[index].totaltime		= rs.getInt("totaltime");
				ar[index].mode			= HearthReader.gameModeToString(rs.getInt("mode"));
				ar[index].deleted		= rs.getInt("deleted");
				ar[index].modified		= rs.getInt("modified");
				ar[index].lastmodified	= rs.getLong("lastmodified");
				
				if(recordCount != 0 && index == (BATCH_SIZE - 1)){
					try {
						String json = gson.toJson(ar);
						if(syncWithJsonString(url, json)){
							tracker.updateMatchResultSyncTime(resultIds, new Date().getTime());
						}
					} catch (Throwable e) {
						System.out.println(e.getMessage());
					}
				}

				++recordCount;
			}
			
			//flush the remaining
			if(index > -1 && index != (BATCH_SIZE - 1)){
				MatchResult[] arResized = new MatchResult[index+1];
				int[] resultIdsResized = new int[index+1];
				System.arraycopy(ar, 0, arResized, 0, index+1);
				System.arraycopy(resultIds, 0, resultIdsResized, 0, index+1);
				
				String json;
				try {
					json = gson.toJson(arResized);
					if(syncWithJsonString(url, json)){
						tracker.updateMatchResultSyncTime(resultIdsResized, new Date().getTime());
					}
				} catch (Throwable e) {
					System.out.println(e.getMessage());
				}
			}
	
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("Affected records: " + recordCount);
		System.out.println("***Match Batch Sync Ended***");
	}
	
	public void test(){
		String url = baseURL + "test/";
		int[] array = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};		
		System.out.println(url);
		
		try {
			String jsondata = gson.toJson(array);
			System.out.println(jsondata);
			
			MultipartContent formData = form(
				data("key", getKey()),
				data("nounce", getNounce() + ""),
				data("json", jsondata)
			);
			
			resty.json(url, formData);
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
	}
	
//	private void sleep(){
//		long sleepTime;
//		Date lastScan = new Date();
//
//		sleepTime = setting.scanInterval - (new Date().getTime() - lastScan.getTime());
//		
//		if(sleepTime > 0){
//			Thread.sleep(sleepTime);
//		}
//	}
}
