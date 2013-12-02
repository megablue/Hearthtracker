package my.hearthtracking.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.h2.jdbcx.JdbcDataSource;

public class Tracker {	
	Connection conn;
	boolean isDirty = true;
	boolean isWorking = false;
	boolean testMode = false;
	Date lastWrite = new Date();
	String liveScore = "";
	String liveHero = "";
	String liveWinrate = "";
	String liveGamestats = "";
	HearthDatabase dbSetting;
	private static HearthConfigurator config = new HearthConfigurator();
	private Statement stat;
	
	public Tracker(){
		
		try {
			this.initDB();
			if(testMode){
				this.selfTest();
			}
			isWorking = true;
		} catch (SQLException e) {
			e.printStackTrace();
			isWorking = false;
		}
	}
	
	public boolean isWorking(){
		return isWorking;
	}
	
	private void initDB() throws SQLException{
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:.//data/database");
		ds.setUser("tracker");
		ds.setPassword("tracker");
		conn = ds.getConnection();
		stat = conn.createStatement();
		this.createTables();
	}
	
	private void selfTest() throws SQLException{
		this.truncateDB();
		this.saveArenaResult(0, 9, 0, new Date().getTime(), false);
		this.saveArenaResult(0, 9, 0, new Date().getTime(), false);
		this.saveArenaResult(0, 9, 9, new Date().getTime(), false);
		assert this.getOverallWinRate(HearthReader.ARENAMODE) == 75.0f;
		
		this.saveMatchResult(HearthReader.ARENAMODE, 0, 0, 1, 1, new Date().getTime(), 0, false);
		this.saveMatchResult(HearthReader.ARENAMODE, 1, 0, 1, 0, new Date().getTime(), 0, false);
		this.saveMatchResult(HearthReader.ARENAMODE, 2, 0, 0, 0, new Date().getTime(), 0, false);
		this.saveMatchResult(HearthReader.ARENAMODE, 3, 0, 0, 1, new Date().getTime(), 0, false);
		this.saveMatchResult(HearthReader.ARENAMODE, 4, 0, 0, 1, new Date().getTime(), 0, false);
		this.saveMatchResult(HearthReader.ARENAMODE, 5, 0, 0, 1, new Date().getTime(), 0, false);
		
		assert this.getWinRateByGoesFirst(HearthReader.ARENAMODE) == 50.0f;
		assert this.getWinRateByGoesSecond(HearthReader.ARENAMODE) == 75.0f;
	}
	
	private void truncateDB() throws SQLException{
		stat.execute("TRUNCATE TABLE ARENARESULTS");
		stat.execute("TRUNCATE TABLE MATCHES");
	}
	
	public void closeDB(){
		try {
			stat.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void createTables() throws SQLException{
		ResultSet rs;
		boolean newdb = false;
		
		dbSetting = (HearthDatabase) config.load("." + File.separator + "data" + File.separator + "database.xml");
		
		if(dbSetting == null){
			dbSetting = new HearthDatabase();
			config.save(dbSetting, "." + File.separator + "data" + File.separator + "database.xml");
		}
		
		if(dbSetting.version > 0){
			return;
		}
		
		rs = stat.executeQuery("select count(*) from information_schema.tables WHERE table_name = 'MATCHES'");
		
		if(rs.next() && rs.getInt("COUNT(*)") == 1){
			//dirty fix for database.xml which I forgot to save the change of version 
			//if the app created a database for the first time on v1.1.0 and v1.1.1 
			dbSetting.version = 1;
			config.save(dbSetting, "." + File.separator + "data" + File.separator + "database.xml");
		}

		rs = stat.executeQuery("select count(*) from information_schema.tables where table_name = 'ARENARESULTS'");
		 
		if(rs.next()){
			if(rs.getInt("COUNT(*)") == 0){
				stat.execute("create table arenaResults( "
								+"id int primary key auto_increment, "
								+"heroId int, "
								+"wins int, "
								+"losses int, "
								+"timeCaptured BIGINT DEFAULT 0, "
								+"modified int DEFAULT 0, "
								+"lastmodified BIGINT DEFAULT 0, "
								+"deleted int DEFAULT 0, "
								+"submitted int DEFAULT 0 "
								+ ")"
						);
				stat.execute("CREATE INDEX heroId ON arenaResults(heroId, DELETED)");
				newdb = true;
			}
		}
		 
		rs = stat.executeQuery("select count(*) from information_schema.tables WHERE table_name = 'ARENAMATCHES'");
		 
		if(rs.next() && rs.getInt("COUNT(*)") == 0){
			stat.execute("create table MATCHES("
							+"id int primary key auto_increment, "
							+"myHeroId int, "
							+"oppHeroId int, "
							+"goesFirst int, win int, "
							+"startTime BIGINT DEFAULT 0, "
							+"totalTime int,"
							+"mode int,"
							+"modified int DEFAULT 0, "
							+"lastmodified BIGINT DEFAULT 0, "
							+"deleted int DEFAULT 0, "
							+"submitted int DEFAULT 0 "
							+")"
						);
			stat.execute("CREATE INDEX myHeroId ON MATCHES(myHeroId, deleted)");
			stat.execute("CREATE INDEX DELETED ON MATCHES(DELETED)");
			stat.execute("CREATE INDEX MODE ON MATCHES(MODE,DELETED)");
			stat.execute("CREATE INDEX MYHERO_MODE ON MATCHES(MYHEROID, MODE, DELETED)");
			stat.execute("CREATE INDEX OPPHERO_MODE ON MATCHES(OPPHEROID, MODE, DELETED)");
			stat.execute("CREATE INDEX HEROES_MODE ON MATCHES(MYHEROID, OPPHEROID, MODE, DELETED)");
			newdb = true;
		}
		
		if(newdb){
			dbSetting.version = 1;
			config.save(dbSetting, "." + File.separator + "data" + File.separator + "database.xml");
			return;
		}
		
		//database schema upgrade from v1.0.X to v1.1.0
		if(!newdb && dbSetting.version == 0){
			stat.execute("ALTER TABLE ARENARESULTS ADD timeCapturedX BIGINT");
			ResultSet result = stat.executeQuery("SELECT id, timeCaptured FROM ARENARESULTS");
			Statement stat2 = conn.createStatement();
			
			while(result.next()){
				long time = result.getTimestamp("TIMECAPTURED").getTime();
				int id = result.getInt("id");
				
				stat2.execute(
						"UPDATE ARENARESULTS SET "
						+"TIMECAPTUREDX='" + time + "' "
						+"WHERE id=" + id
					);
			}
			stat.execute("ALTER TABLE ARENARESULTS DROP COLUMN timeCaptured");
			stat.execute("ALTER TABLE ARENARESULTS ALTER COLUMN timeCapturedx RENAME to timeCaptured");
			
			stat.execute("ALTER TABLE ARENAMATCHES ADD startTimeX BIGINT");
			result = stat.executeQuery("SELECT id, startTime FROM ARENAMATCHES");
			
			while(result.next()){
				long time = result.getTimestamp("STARTTIME").getTime();
				int id = result.getInt("id");
				
				stat2.execute(
						"UPDATE ARENAMATCHES SET "
						+"STARTTIMEX='" + time + "' "
						+"WHERE id=" + id
					);
			}
			stat.execute("ALTER TABLE ARENAMATCHES DROP COLUMN STARTTIME");
			stat.execute("ALTER TABLE ARENAMATCHES ALTER COLUMN STARTTIMEX RENAME to STARTTIME");
			
			
			stat.execute("ALTER TABLE ARENARESULTS ADD MODIFIED INT");
			stat.execute("ALTER TABLE ARENARESULTS ADD DELETED INT");
			stat.execute("ALTER TABLE ARENARESULTS ADD LASTMODIFIED BIGINT");
			stat.execute("ALTER TABLE ARENARESULTS ADD SUBMITTED INT");
			stat.execute("ALTER TABLE ARENARESULTS ALTER COLUMN DELETED SET DEFAULT 0");
			stat.execute("ALTER TABLE ARENARESULTS ALTER COLUMN MODIFIED SET DEFAULT 0");
			stat.execute("ALTER TABLE ARENARESULTS ALTER COLUMN SUBMITTED SET DEFAULT 0");
			stat.execute("ALTER TABLE ARENARESULTS ALTER COLUMN LASTMODIFIED SET DEFAULT 0");
						
			stat.execute("DROP INDEX IF EXISTS heroId");
			stat.execute("CREATE INDEX heroId ON arenaResults(heroId, DELETED)");
			
			stat.execute("ALTER TABLE ARENAMATCHES RENAME to MATCHES");
			stat.execute("ALTER TABLE MATCHES ADD MODE INT");
			stat.execute("ALTER TABLE MATCHES ADD DELETED INT");
			stat.execute("ALTER TABLE MATCHES ADD MODIFIED INT");
			stat.execute("ALTER TABLE MATCHES ADD LASTMODIFIED BIGINT");
			stat.execute("ALTER TABLE MATCHES ADD SUBMITTED INT");
			stat.execute("ALTER TABLE MATCHES ALTER COLUMN MODE SET DEFAULT " + HearthReader.ARENAMODE);
			stat.execute("ALTER TABLE MATCHES ALTER COLUMN DELETED SET DEFAULT 0");
			stat.execute("ALTER TABLE MATCHES ALTER COLUMN MODIFIED SET DEFAULT 0");
			stat.execute("ALTER TABLE MATCHES ALTER COLUMN SUBMITTED SET DEFAULT 0");
			stat.execute("ALTER TABLE MATCHES ALTER COLUMN LASTMODIFIED SET DEFAULT 0");
			
			stat.execute("DROP INDEX IF EXISTS myHeroId");
			stat.execute("CREATE INDEX myHeroId ON MATCHES(myHeroId, deleted)");
			stat.execute("CREATE INDEX DELETED ON MATCHES(DELETED)");
			stat.execute("CREATE INDEX MODE ON MATCHES(MODE,DELETED)");
			stat.execute("CREATE INDEX MYHERO_MODE ON MATCHES(MYHEROID, MODE, DELETED)");
			stat.execute("CREATE INDEX OPPHERO_MODE ON MATCHES(OPPHEROID, MODE, DELETED)");
			stat.execute("CREATE INDEX HEROES_MODE ON MATCHES(MYHEROID, OPPHEROID, MODE, DELETED)");
			
			stat.execute("UPDATE MATCHES SET MODE=" + HearthReader.ARENAMODE);
			stat.execute("UPDATE MATCHES SET MODIFIED=0");
			stat.execute("UPDATE MATCHES SET LASTMODIFIED=STARTTIME");
			stat.execute("UPDATE MATCHES SET DELETED=0");
			stat.execute("UPDATE MATCHES SET SUBMITTED=0");
			stat.execute("UPDATE ARENARESULTS SET MODIFIED=0");
			stat.execute("UPDATE ARENARESULTS SET SUBMITTED=0");
			stat.execute("UPDATE ARENARESULTS SET DELETED=0");
			stat.execute("UPDATE ARENARESULTS SET LASTMODIFIED=timeCaptured");
			
			//save upgraded version
			dbSetting.version = 1;
			config.save(dbSetting, "." + File.separator + "data" + File.separator + "database.xml");
		}
	}
	
	public boolean isDirty(){

		if(isDirty){
			isDirty = false;
			return true;
		}
		
		return false;
	}
	
	public void saveArenaResult(int heroId, int wins, int losses, long time, boolean modified) throws SQLException{
		int mod = modified ? 1 : 0;
		String sql = "INSERT INTO arenaResults(heroId, wins, losses, modified, timeCaptured, lastModified)"
				+"VALUES(" + heroId + "," 
				+ wins + "," 
				+ losses + ", " 
				+ mod + ", " 
				+ time + ", " 
				+ time + ")";
		stat.execute(sql);
		isDirty = true;
	}
	
	public void saveModifiedArenaResult(int id, int heroId, int wins, int losses, long time) throws SQLException{
		long modTime = new Date().getTime();
		String table = "ARENARESULTS";
		ResultSet rs = this.getMatch(id);
		int modified = 0;
		
		if(rs.next()){
			if(rs.getInt("HEROID") != -1 && rs.getInt("HEROID") != heroId){
				modified = 1;
			}
			
			if(rs.getInt("WINS") != wins){
				modified = 1;
			}
			
			if(rs.getInt("LOSSES") != losses){
				modified = 1;
			}
		}
		
		String sql = "UPDATE " + table
					+ " SET heroid=" +  heroId + ", "
					+ " wins=" + wins + ", "
					+ " losses=" + losses + ", "
					+ " timeCaptured='" + time + "', "
					+ " lastModified=" + modTime + ", "
					+ " modified=" + modified
					+ " WHERE id=" + id;
		stat.execute(sql);
		isDirty = true;
	}
	
	public void saveMatchResult(int mode, int myHeroId, int oppHeroId, int goesFirst, int win, long startTime, int totalTime, boolean modified) throws SQLException{
		String table = "MATCHES";
		int mod = modified ? 1 : 0;
		
		String sql = "INSERT INTO " + table +"(myHeroId, oppHeroId, goesFirst, win, startTime, lastModified, mode, modified, totalTime) " 
					+ "VALUES(" + myHeroId + "," 
					+ oppHeroId + "," 
					+ goesFirst + "," 
					+ win + ","
					+ startTime + ","
					+ startTime + ","
					+ mode + ","
					+ mod + ","
					+ totalTime + ")";

		stat.execute(sql);
		isDirty = true;
	}
	
	public void deleteModifiedArenaResult(int id) throws SQLException{
		long modTime = new Date().getTime();
		String table = "ARENARESULTS";

		String sql = "UPDATE " + table
					+ " SET deleted=1, "
					+ " lastmodified=" + modTime
					+ " WHERE id=" + id;
		stat.execute(sql);
		isDirty = true;
	}
	
	public void saveModifiedMatchResult(int id, int mode, int myHeroId, int oppHeroId, int goesFirst, int win, Long startTime, int totalTime) throws SQLException{
		long modTime = new Date().getTime();
		String table = "MATCHES";
		ResultSet rs = this.getMatch(id);
		int modified = 0;
		
		if(rs.next()){
			if(rs.getInt("MYHEROID") != -1 && rs.getInt("MYHEROID") != myHeroId){
				modified = 1;
			}
			
			if(rs.getInt("win") != win){
				modified = 1;
			}
		}
		
		String sql = "UPDATE " + table
					+ " SET myheroid=" +  myHeroId + ", "
					+ " oppheroid=" + oppHeroId + ", "
					+ " mode=" + mode + ", "
					+ " goesFirst=" + goesFirst + ", "
					+ " win=" + win + ", "
					+ " startTime='" + startTime + "', "
					+ " totalTime=" + totalTime + ", "
					+ " lastmodified=" + modTime + ", "
					+ " modified=" + modified
					+ " WHERE id=" + id;
		stat.execute(sql);
		isDirty = true;
	}
	
	public void deleteMatchResult(int id) throws SQLException{
		long modTime = new Date().getTime();
		String table = "MATCHES";

		String sql = "UPDATE " + table
					+ " SET deleted=1, "
					+ " lastmodified='" + modTime + "' "
					+ " WHERE id=" + id;
		stat.execute(sql);
		isDirty = true;
	}
	
	public int getTotalRunsByHero(int mode, int heroid) throws SQLException{
		ResultSet rs;
		int total = 0;

		if(mode == HearthReader.ARENAMODE){
			rs = stat.executeQuery("select count(*) as TOTAL from ARENARESULTS WHERE heroid = " + heroid + " AND DELETED=0");
			
			if(rs.next()){
				total += rs.getInt("TOTAL");
			}
		} else {
			
			if(mode == HearthReader.CHALLENGEMODE || mode == HearthReader.PRACTICEMODE){
				rs = stat.executeQuery("select count(*) as TOTAL from MATCHES WHERE myheroid = " + heroid 
						+ " AND (MODE=" + HearthReader.CHALLENGEMODE + " OR MODE=" + HearthReader.PRACTICEMODE +") AND DELETED=0"
					);
			} else {
				rs = stat.executeQuery("select count(*) as TOTAL from MATCHES WHERE myheroid = " + heroid 
						+ " AND (MODE=" + mode + ") AND DELETED=0"
					);
			}
			

			
			if(rs.next()){
				total += rs.getInt("TOTAL");
			}
		}
		
		return total;
	}
	
	public float getWinRateByHero(int mode, int heroId) throws SQLException{
		ResultSet rs;
		int wins = 0;
		int losses = 0;
		float winrate = -1;
		boolean found = false;
		
		if(mode == HearthReader.ARENAMODE){
			rs = stat.executeQuery("select wins,losses from ARENARESULTS where heroId = " + heroId + " AND DELETED=0");
			
			while(rs.next()){
				found = true;
				wins += rs.getInt("WINS");
				losses += rs.getInt("LOSSES");
			}
		} else {
			
			if(mode == HearthReader.CHALLENGEMODE || mode == HearthReader.PRACTICEMODE){
				rs = stat.executeQuery("select win FROM MATCHES where MYHEROID = " + heroId 
						+ " AND( MODE=" + HearthReader.CHALLENGEMODE
						+ " OR MODE=" + HearthReader.PRACTICEMODE
						+ ") AND DELETED=0"
				);
			} else {
				rs = stat.executeQuery("select win FROM MATCHES where MYHEROID = " + heroId + " AND MODE=" + mode + " AND DELETED=0");
			}
			
			while(rs.next()){
				found = true;
				
				if(rs.getInt("WIN") == 1){
					wins += 1;
				} else {
					losses += 1;
				}
			}
		}
		
		if(found){
			winrate = (float) wins/(wins+losses);
		}

		return winrate;
	}
	
	public float getWinRateByHeroSpecial(int mode, int heroId) throws SQLException{
		ResultSet rs;
		int sevenplus = 0;
		int arenacount = 0;
		float winrate = -1;
		boolean found = false;
		
		if(mode == HearthReader.ARENAMODE){
			rs = stat.executeQuery("select wins from ARENARESULTS where heroId = " + heroId + " AND DELETED=0");
			
			while(rs.next()){
				found = true;
				arenacount += 1;

				if(rs.getInt("WINS") >= 7){
					sevenplus += 1;
				}
			}
		}
		
		if(found){
			winrate = (float) sevenplus/arenacount;
		}
		

		return found ? winrate : -1;
	}
	
	public float getOverallWinRate(int mode) throws SQLException{
		ResultSet rs;
		int wins = 0;
		int losses = 0;
		float winrate = -1;
		boolean found = false;
		
		if(mode == HearthReader.ARENAMODE){
			rs = stat.executeQuery("select wins,losses from ARENARESULTS WHERE DELETED=0");
			
			while(rs.next()){
				found = true;
				wins += rs.getInt("WINS");
				losses += rs.getInt("LOSSES");
			}
		} else {
			
			if(mode == HearthReader.CHALLENGEMODE || mode == HearthReader.PRACTICEMODE){
				rs = stat.executeQuery("select WIN from MATCHES WHERE (MODE=" + HearthReader.CHALLENGEMODE + " OR MODE=" + HearthReader.PRACTICEMODE + ") AND DELETED=0");
			} else {
				rs = stat.executeQuery("select WIN from MATCHES WHERE MODE=" + mode + " AND DELETED=0");
			}

			while(rs.next()){
				found = true;
				
				if(rs.getInt("WIN") == 1){
					wins += 1;
				} else {
					losses += 1;
				}	
			}
		}
		
		if(found){
			winrate = (float) wins/(wins+losses) * 100;
		}

		return winrate;
	}
	
	public int getTotalWins(int mode) throws SQLException{
		ResultSet rs;
		int total = 0;

		if(mode == HearthReader.ARENAMODE){
			rs = stat.executeQuery("select wins,losses from ARENARESULTS WHERE " + " DELETED = 0");
			while(rs.next()){
				total += rs.getInt("WINS");
			}
		} else {
			if(mode == HearthReader.CHALLENGEMODE || mode == HearthReader.PRACTICEMODE){
				rs = stat.executeQuery("select WIN from MATCHES WHERE "
						+ "(mode=" + HearthReader.CHALLENGEMODE
						+ " OR mode=" + HearthReader.PRACTICEMODE
						+ " )AND DELETED = 0");
			} else {
				rs = stat.executeQuery("select WIN from MATCHES WHERE mode=" + mode + " AND DELETED = 0");
			}
			
			while(rs.next()){
				total += rs.getInt("WIN");
			}
		}
			
		return total;
	}
	
	public int getTotalWinsByHero(int mode, int heroId) throws SQLException{
		ResultSet rs;
		int total = 0;

		if(mode == HearthReader.ARENAMODE){
			rs = stat.executeQuery("select wins,losses from ARENARESULTS WHERE heroid=" + heroId + " AND DELETED = 0");
			while(rs.next()){
				total += rs.getInt("WINS");
			}
		} else {
			
			if(mode == HearthReader.CHALLENGEMODE || mode == HearthReader.PRACTICEMODE){
				rs = stat.executeQuery("select WIN from MATCHES "
						+" WHERE myheroid=" + heroId 
						+ " AND (mode=" + HearthReader.CHALLENGEMODE
						+ " OR mode=" + HearthReader.PRACTICEMODE + ")"
						+ " AND DELETED = 0"
					);
			}else{
				rs = stat.executeQuery("select WIN from MATCHES WHERE myheroid=" + heroId + " AND mode=" + mode + " AND DELETED = 0");
			}

			while(rs.next()){
				total += rs.getInt("WIN");
			}
		}

		return total;
	}
	
	public int getTotalLosses(int mode) throws SQLException{
		ResultSet rs;
		int total = 0;

		if(mode == HearthReader.ARENAMODE){
			rs = stat.executeQuery("select wins,losses from ARENARESULTS WHERE DELETED=0");
			while(rs.next()){
				total += rs.getInt("LOSSES");
			}
		} else {
			
			if(mode == HearthReader.CHALLENGEMODE || mode == HearthReader.PRACTICEMODE){
				rs = stat.executeQuery("select WIN from MATCHES "
						+" WHERE mode=" + HearthReader.CHALLENGEMODE
						+ " OR mode=" + HearthReader.PRACTICEMODE + ""
					);
			}else{
				rs = stat.executeQuery("select WIN from MATCHES WHERE mode=" + mode);
			}
			while(rs.next()){
				total += rs.getInt("WIN") == 0 ? 1 : 0;
			}
		}

		return total;
	}
	
	public int getTotalLossesByHero(int mode, int heroId) throws SQLException{
		ResultSet rs;
		int total = 0;

		if(mode == HearthReader.ARENAMODE){
			rs = stat.executeQuery("select wins,losses from ARENARESULTS WHERE heroid=" + heroId + " AND DELETED=0" );
			while(rs.next()){
				total += rs.getInt("LOSSES");
			}
		} else {
			
			if(mode == HearthReader.CHALLENGEMODE || mode == HearthReader.PRACTICEMODE){
				rs = stat.executeQuery("select WIN from MATCHES WHERE "
						+ "myheroid=" + heroId + " AND (mode=" + mode 
						+ " OR MODE=" + HearthReader.PRACTICEMODE
						+ ") AND DELETED=0"
				);
			} else {
				rs = stat.executeQuery("select WIN from MATCHES WHERE myheroid=" + heroId + " AND mode=" + mode + " AND DELETED=0");
			}
			
			while(rs.next()){
				total += rs.getInt("WIN") == 0 ? 1 : 0;
			}
		}

		return total;
	}
	
	public float getWinRateByGoesFirst(int mode) throws SQLException{
		return this.getWinRateGoesBy(mode, true, -1, -1);
	}
	
	public float getWinRateByGoesSecond(int mode) throws SQLException{
		return this.getWinRateGoesBy(mode, false, -1, -1);
	}
	
	public float getWinRateGoesBy(int mode, boolean goesFirstFlag, int heroId, int against) throws SQLException{
		ResultSet rs;
		float winrate = -1;
		int goesFirst = goesFirstFlag ? 1 : 0;
		String table = "MATCHES";
		String sqlWhere = mode == HearthReader.PRACTICEMODE || mode == HearthReader.CHALLENGEMODE ?
				" WHERE goesFirst = " + goesFirst + " AND (mode = " + HearthReader.CHALLENGEMODE + " OR MODE=" + HearthReader.PRACTICEMODE +") AND DELETED=0"
				:" WHERE goesFirst = " + goesFirst + " AND mode = " + mode + " AND DELETED=0";
					
		String sql = "select SUM(WIN), COUNT(*) from " + table + sqlWhere;
		
		if(heroId > -1){
			sql += " AND myHeroId = " + heroId;
		} 
		
		if(against > -1){
			sql += " AND oppHeroId = " + against;
		}
		
		rs = stat.executeQuery(sql);
		
		if(rs.next()){
			winrate = rs.getFloat("SUM(WIN)") / rs.getFloat("COUNT(*)") * 100;
		}
		
		return winrate;
	}
	
	public ResultSet getArenaResults() throws SQLException{
		ResultSet rs;
		rs = stat.executeQuery("select * from ARENARESULTS WHERE DELETED=0 ORDER BY ID DESC LIMIT 20");

		return rs;
	}
	
	public ResultSet getArenaResult(int id) throws SQLException{
		ResultSet rs;
		rs = stat.executeQuery("select * from ARENARESULTS WHERE id=" + id);

		return rs;
	}
	
	public ResultSet getMatches() throws SQLException{
		ResultSet rs;
		rs = stat.executeQuery("select * from MATCHES WHERE DELETED=0 ORDER BY ID DESC LIMIT 100");

		return rs;
	}
	
	public ResultSet getMatch(int id) throws SQLException{
		ResultSet rs;
		rs = stat.executeQuery("select * from MATCHES WHERE id = " + id);

		return rs;
	}
	
	public ResultSet getLastMatches(int n) throws SQLException{
		ResultSet rs;
		rs = stat.executeQuery("select * from MATCHES WHERE DELETED=0 ORDER BY ID DESC LIMIT " + n);
		return rs;
	}
	
	public ResultSet getLastArenaResults(int n) throws SQLException{
		ResultSet rs;
		rs = stat.executeQuery("select * from ARENARESULTS WHERE DELETED=0 ORDER BY ID DESC LIMIT " + n);
		return rs;
	}
	
	public void outputArenaStatus(int mode, String score, String hero){
		liveScore = score;
		liveHero = hero;
		this.saveStreamStats(mode);
	}
	
	public void ouputMatchStatus(int mode, String match){
		liveGamestats = match;
		this.saveStreamStats(mode);
	}
	
	private void saveStreamStats(int mode){
		float winrate = 0;
		String overall = "Overall win rate: ";
		String arenaScore = "Arena score: " + liveScore;
		String arenaClass = "Arena class: " + liveHero;
		String latestGame = "Arena game: " + liveGamestats;
		String[] lines = new String[4];
		
		try {
			winrate = this.getOverallWinRate(mode);
			
			if(winrate > -1){
				overall += winrate + " %";
			} else {
				overall += " N|A";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		lines[0] = overall;
		lines[1] = arenaScore;
		lines[2] = arenaClass;
		lines[3] = latestGame;
		
		this.writeLines(lines);
	}
	
	private void writeLines(String[] lines){
		
		//limit the write frequency to 1 second
		if(new Date().getTime() < lastWrite.getTime() + 1000){
			return;
		}
		
		HearthHelper.createFolder("./output");
		
		try {
			for(int i = 0; i < lines.length; i++){
				PrintWriter lineWriter = new PrintWriter("./output/line" + (i+1) + ".txt", "UTF-8");
				lineWriter.println(lines[i]);
				lineWriter.close();
			}
			
			lastWrite = new Date();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
