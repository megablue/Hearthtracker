package my.hearthtracking.app;

import static us.monoid.web.Resty.*;

import java.io.File;
import java.io.IOException;
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
	
	private Resty resty = new Resty(Option.timeout(5000));
	
	private HearthHeroesList heroesList = null;
	
	private int nonce = 0;
	
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
		public String server;
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
		public String server;
	}
	
	public HearthSync() {
		tracker = new HearthTracker();
		nonce = generateNonce();

		syncLog = (HearthSyncLog) config.load("." + File.separator + "configs" + File.separator + "sync.xml");
		
		if(syncLog == null){
			syncLog = new HearthSyncLog();
			saveSyncLog();
		}

		
		setKey(syncLog.secretKey);
	}
	
	private void loadHeroes(){
		if(heroesList != null){
			return;
		}
		
		heroesList = (HearthHeroesList) config.load("." + File.separator + "configs" + File.separator + "heroes.xml");
		
		if(heroesList == null){
			heroesList = new HearthHeroesList();
			config.save(heroesList, "." + File.separator + "configs" + File.separator + "heroes.xml");
		}
	}
	
	private int generateNonce(){
	   int range = (1000000000) + 1;     
	   return (int)(Math.random() * range) + 1;
	}
	
	private int getNonce(){
		return nonce++;
	}
	
	private void saveSyncLog(){
		config.save(syncLog, "." + File.separator + "configs" + File.separator + "sync.xml");
	}
	
	public void setKey(String key){
		secretKey = key;
	}
	
	public String getKey(){
		return secretKey;
	}
	
	public void saveKey(){
		syncLog.secretKey = secretKey;
		config.save(syncLog, "." + File.separator + "configs" + File.separator + "sync.xml");
	}
	
	public void invalidateKey(){
		syncLog.secretKey = "";
		config.save(syncLog, "." + File.separator + "configs" + File.separator + "sync.xml");
	}
	
	public boolean isValidKeyFormat(){
		return syncLog.secretKey.length() == 48 ? true : false;
	}
	
	public boolean isTimeout(){
		if(syncLog.timeoutError > 0 && new Date().getTime() < syncLog.timeoutRetry){
			return true;
		}
		
		return false;
	}
	
	public long getTimeout(){
		return syncLog.timeoutRetry;
	}
	
	public void setTimeout(){
		++syncLog.timeoutError;
		syncLog.timeoutRetry = new Date().getTime() + (60 * 60 * 1000) * syncLog.timeoutError;
		saveSyncLog();
	}
	
	public void resetTimeout(){
		syncLog.timeoutError = 0;
		syncLog.timeoutRetry = 0;
		saveSyncLog();
	}
		
	public boolean checkAccessKey(){
		if(isTimeout()){
			return false;
		}
		
		String url = baseURL + "key_check/";
		boolean result = false;

		MultipartContent formData = form(
			data("key", getKey()),
			data("nonce", getNonce() + "")
		);
		
		try {
			JSONResource jsonResult = resty.json(url, formData);
			printEJSON(jsonResult);

			if("true".equals(jsonResult.get("success").toString())){
				saveKey();
				result = true;
			} else {
				
				if(!"Invalid nonce.".equals(jsonResult.get("message"))){
					invalidateKey();
				}
			}
			
			resetTimeout();
		} catch (IOException e) {
			System.out.println("io exception, " + e.getMessage());
			setTimeout();
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
		
		return result;
	}
	
	private void printEJSON(JSONResource jsonResult){
		try {
			String result = (boolean) jsonResult.get("success") ? "Succeed" : "Failed";
			System.out.println("EJSON: " + jsonResult.get("message") + ", " + result);
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
	}
	
	public boolean syncWithJsonString(String url, String jsonString){
		if(isTimeout()){
			return false;
		}
		
		JSONResource jsonResult;
		
		MultipartContent formData = form(
				data("key", 			getKey()		+ ""),
				data("nonce", 			getNonce() 		+ ""),
				data("json", 			jsonString		+ "")
		);
		
		//System.out.println(jsonString);
		//System.out.println(url);
		
		try {
			jsonResult = resty.json(url, formData);
			printEJSON(jsonResult);
			
			syncLog.lastSync = new Date().getTime();
			saveSyncLog();
			resetTimeout();
			return "true".equals(jsonResult.get("success").toString());
		} catch (IOException e) {
			System.out.println("io exception, " + e.getMessage());
			setTimeout();
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
		
		return false;
	}
	
	public long getLastSync(){
		return syncLog.lastSync;
	}
	
	public int getUnsyncArenaCount(){
		try {
			return tracker.getUnsyncArenaResultsCount();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public int getUnsyncMatchCount(){
		try {
			return tracker.getUnsyncMatchResultsCount();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
		
	public boolean syncArenaBatch(){
		//boolean success = false;
		int recordCount = 0;
		//Date start = new Date();
		ArenaResult[] ar = new ArenaResult[BATCH_SIZE];
		int[] resultIds = new int[BATCH_SIZE];
		int index = -1;
		String url = baseURL + "arena_batch_sync/";
		int errorCount = 0;
		
		loadHeroes();

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
				ar[index].myhero 		= heroesList.getHeroName(rs.getInt("heroid"));
				ar[index].wins 			= rs.getInt("wins");
				ar[index].losses 		= rs.getInt("losses");
				ar[index].timecaptured	= rs.getLong("timecaptured");
				ar[index].modified		= rs.getInt("modified");
				ar[index].deleted		= rs.getInt("deleted");
				ar[index].lastmodified	= rs.getLong("lastmodified");
				ar[index].server		= rs.getString("server");
				
				if(recordCount != 0 && index == (BATCH_SIZE - 1)){
					try {
						String json = gson.toJson(ar);
						if(syncWithJsonString(url, json)){
							tracker.updateArenaResultSyncTime(resultIds, new Date().getTime());
						}else{
							errorCount++;
						}
					} catch (Throwable e) {
						System.out.println(e.getMessage());
						errorCount++;
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
					}else{
						errorCount++;
					}
				} catch (Throwable e) {
					System.out.println(e.getMessage());
					errorCount++;
				}
			}
	
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("Affected records: " + recordCount);
		System.out.println("***Arena Batch Sync Ended***");
		
		if(errorCount > 0){
			return false;
		}
		
		return true;
	}
	
	public boolean syncMatchBatch(){
		//boolean success = false;
		int recordCount = 0;
		//Date start = new Date();
		MatchResult[] ar = new MatchResult[BATCH_SIZE];
		int[] resultIds = new int[BATCH_SIZE];
		int index = -1;
		String url = baseURL + "match_batch_sync/";
		int errorCount = 0;

		loadHeroes();
		
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
				ar[index].myhero 		= heroesList.getHeroName(rs.getInt("myheroid"));
				ar[index].opphero		= heroesList.getHeroName(rs.getInt("oppheroid"));
				ar[index].goes			= HearthReader.goesFirstToString(rs.getInt("goesfirst"));
				ar[index].win			= rs.getInt("win");
				ar[index].starttime		= rs.getLong("starttime");
				ar[index].totaltime		= rs.getInt("totaltime");
				ar[index].mode			= HearthReader.gameModeToString(rs.getInt("mode")).toLowerCase();
				ar[index].deleted		= rs.getInt("deleted");
				ar[index].modified		= rs.getInt("modified");
				ar[index].lastmodified	= rs.getLong("lastmodified");
				ar[index].server		= rs.getString("server");
				
				if(recordCount != 0 && index == (BATCH_SIZE - 1)){
					try {
						String json = gson.toJson(ar);
						if(syncWithJsonString(url, json)){
							tracker.updateMatchResultSyncTime(resultIds, new Date().getTime());
						}else{
							errorCount++;
						}
					} catch (Throwable e) {
						System.out.println(e.getMessage());
						errorCount++;
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
					}else{
						errorCount++;
					}
				} catch (Throwable e) {
					System.out.println(e.getMessage());
					errorCount++;
				}
			}
	
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("Affected records: " + recordCount);
		System.out.println("***Match Batch Sync Ended***");
		
		if(errorCount > 0){
			return false;
		}
		
		return true;
	}
	
//	public void test(){
//		String url = baseURL + "test/";
//		int[] array = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};		
//		System.out.println(url);
//		
//		try {
//			String jsondata = gson.toJson(array);
//			System.out.println(jsondata);
//			
//			MultipartContent formData = form(
//				data("key", getKey()),
//				data("nonce", getnonce() + ""),
//				data("json", jsondata)
//			);
//			
//			resty.json(url, formData);
//		} catch (Throwable e) {
//			System.out.println(e.getMessage());
//		}
//	}
}
