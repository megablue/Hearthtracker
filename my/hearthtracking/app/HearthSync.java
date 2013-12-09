package my.hearthtracking.app;

import static us.monoid.web.Resty.*;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import us.monoid.web.JSONResource;
import us.monoid.web.Resty;
import us.monoid.web.mime.MultipartContent;

public class HearthSync {
	
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
	
	public HearthSync() {
		tracker = new HearthTracker();
		
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
			data("key", getKey())
		);
		
		try {
			System.out.println(url);
			JSONResource jsonResult = resty.json(url, formData);
			printEJSON(jsonResult);
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private void printEJSON(JSONResource jsonResult){
		try {
			String result = (boolean) jsonResult.get("success") ? "Succeed" : "Failed";
			System.out.println("EJSON: " + jsonResult.get("message") + ", " + result);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public boolean syncArena(int cid, String myhero, int wins, 
			int losses, long timecaptured, int modified,
			int deleted, long lastmodified){
		
		String url = baseURL + "arena_sync/";
		
		MultipartContent formData = form(
			data("key", 			getKey()		+ ""),
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
			tracker.updateArenaResultSyncTime(cid, new Date().getTime());
			syncLog.lastSync = new Date().getTime();
			printEJSON(jsonResult);
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void syncAll(){
		boolean success = false;
		int errorCount = 0;
		int recordCount = 0;
		Date start = new Date();
		
		try {
			System.out.println("***Batch Sync Started***");
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
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		float totalSeconds = (float)(new Date().getTime() - start.getTime())/1000;
		float average = (float) totalSeconds / recordCount;
		
		System.out.println("Total time used: " + (float)(totalSeconds/60) + " mins");
		System.out.println("Total records processeed: " + recordCount);
		if(recordCount > 0){
			System.out.println("Average time per record: " + average + " seconds");
		}
		
		System.out.println("***Batch Sync Ended***");
		saveSyncLog();
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
