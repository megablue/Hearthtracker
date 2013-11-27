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
		this.saveArenaResult(0, 9, 0);
		this.saveArenaResult(0, 9, 0);
		this.saveArenaResult(0, 9, 9);
		assert this.getOverallWinRate(HearthReader.ARENAMODE) == 75.0f;
		
		this.saveMatchResult(HearthReader.ARENAMODE, 0, 0, 1, 1, new Date(), 0);
		this.saveMatchResult(HearthReader.ARENAMODE, 1, 0, 1, 0, new Date(), 0);
		this.saveMatchResult(HearthReader.ARENAMODE, 2, 0, 0, 0, new Date(), 0);
		this.saveMatchResult(HearthReader.ARENAMODE, 3, 0, 0, 1, new Date(), 0);
		this.saveMatchResult(HearthReader.ARENAMODE, 4, 0, 0, 1, new Date(), 0);
		this.saveMatchResult(HearthReader.ARENAMODE, 5, 0, 0, 1, new Date(), 0);
		
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
		
		rs = stat.executeQuery("select count(*) from information_schema.tables where table_name = 'ARENARESULTS'");
		 
		if(rs.next()){
			if(rs.getInt("COUNT(*)") == 0){
				stat.execute("create table arenaResults( "
								+"id int primary key auto_increment, "
								+"heroId int, "
								+"wins int, "
								+"losses int, "
								+"timeCaptured TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
								+"modified int DEFAULT 0, "
								+"lastmodified TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
								+"deleted int DEFAULT 0, "
								+"submitted int DEFAULT 0 "
								+ ")"
						);
				stat.execute("CREATE INDEX heroId ON arenaResults(heroId, DELETED)");
				newdb = true;
			}
		}
		 
		rs = stat.executeQuery("select count(*) from information_schema.tables where table_name = 'MATCHES' OR table_name = 'ARENAMATCHES'");
		 
		if(rs.next()){
			if(rs.getInt("COUNT(*)") == 0){
				stat.execute("create table MATCHES("
								+"id int primary key auto_increment, "
								+"myHeroId int, "
								+"oppHeroId int, "
								+"goesFirst int, win int, "
								+"startTime TIMESTAMP, "
								+"totalTime int,"
								+"mode int,"
								+"modified int DEFAULT 0, "
								+"lastmodified TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
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
		}
		
		if(!newdb && dbSetting.version == 0){
			stat.execute("ALTER TABLE ARENARESULTS ADD MODIFIED INT");
			stat.execute("ALTER TABLE ARENARESULTS ADD DELETED INT");
			stat.execute("ALTER TABLE ARENARESULTS ADD LASTMODIFIED TIMESTAMP");
			stat.execute("ALTER TABLE ARENARESULTS ADD SUBMITTED INT");
			stat.execute("ALTER TABLE ARENARESULTS ALTER COLUMN DELETED SET DEFAULT 0");
			stat.execute("ALTER TABLE ARENARESULTS ALTER COLUMN MODIFIED SET DEFAULT 0");
			stat.execute("ALTER TABLE ARENARESULTS ALTER COLUMN SUBMITTED SET DEFAULT 0");
			stat.execute("ALTER TABLE ARENARESULTS ALTER COLUMN LASTMODIFIED SET DEFAULT CURRENT_TIMESTAMP");
						
			stat.execute("DROP INDEX IF EXISTS heroId");
			stat.execute("CREATE INDEX heroId ON arenaResults(heroId, DELETED)");
			
			stat.execute("ALTER TABLE ARENAMATCHES RENAME to MATCHES");
			stat.execute("ALTER TABLE MATCHES ADD MODE INT");
			stat.execute("ALTER TABLE MATCHES ADD DELETED INT");
			stat.execute("ALTER TABLE MATCHES ADD MODIFIED INT");
			stat.execute("ALTER TABLE MATCHES ADD LASTMODIFIED TIMESTAMP");
			stat.execute("ALTER TABLE MATCHES ADD SUBMITTED INT");
			stat.execute("ALTER TABLE MATCHES ALTER COLUMN MODE SET DEFAULT " + HearthReader.ARENAMODE);
			stat.execute("ALTER TABLE MATCHES ALTER COLUMN DELETED SET DEFAULT 0");
			stat.execute("ALTER TABLE MATCHES ALTER COLUMN MODIFIED SET DEFAULT 0");
			stat.execute("ALTER TABLE MATCHES ALTER COLUMN SUBMITTED SET DEFAULT 0");
			stat.execute("ALTER TABLE MATCHES ALTER COLUMN LASTMODIFIED SET DEFAULT CURRENT_TIMESTAMP");
			
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
			
			dbSetting.version = 1;
			config.save(dbSetting, "." + File.separator + "data" + File.separator + "database.xml");
		}
	}
	
	public void saveArenaResult(int heroId, int wins, int losses) throws SQLException{
		String sql = "INSERT INTO arenaResults(heroId, wins, losses) VALUES(" + heroId + "," + wins + "," + losses + ")";
		stat.execute(sql);
	}
	
	public void saveMatchResult(int mode, int myHeroId, int oppHeroId, int goesFirst, int win, Date startTime, int totalTime) throws SQLException{
		java.sql.Timestamp sqlDate = new java.sql.Timestamp(startTime.getTime());
		String table = "MATCHES";
		
		String sql = "INSERT INTO " + table +"(myHeroId, oppHeroId, goesFirst, win, startTime, mode, totalTime) " 
					+ "VALUES(" + myHeroId + "," 
					+ oppHeroId + "," 
					+ goesFirst + "," 
					+ win + ","
					+ "'" + sqlDate.toString() + "'" + ","
					+ mode + ","
					+ totalTime + ")";

		stat.execute(sql);
	}
	
	public void saveModifiedMatchResult(int id, int mode, int myHeroId, int oppHeroId, int goesFirst, int win, Date startTime, int totalTime) throws SQLException{
		java.sql.Timestamp sqlDate = new java.sql.Timestamp(startTime.getTime());
		java.sql.Timestamp sqlDateMod = new java.sql.Timestamp(new Date().getTime());
		String table = "MATCHES";
		ResultSet rs = this.getMatch(id);
		int modified = 0;
		
		if(rs.next()){
			if(rs.getInt("MYHEROID") != myHeroId){
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
					+ " startTime='" + sqlDate.toString() + "', "
					+ " totalTime=" + totalTime + ", "
					+ " lastmodified='" + sqlDateMod.toString() + "', "
					+ " modified=" + modified
					+ " WHERE id=" + id;
		stat.execute(sql);
	}
	
	public void deleteMatchResult(int id) throws SQLException{
		java.sql.Timestamp sqlDateMod = new java.sql.Timestamp(new Date().getTime());
		String table = "MATCHES";

		String sql = "UPDATE " + table
					+ " SET deleted=1, "
					+ " lastmodified='" + sqlDateMod.toString() + "' "
					+ " WHERE id=" + id;
		stat.execute(sql);
	}
	
	public int getTotalRunsByHero(int mode, int heroid) throws SQLException{
		ResultSet rs;
		int total = 0;

		if(mode == HearthReader.ARENAMODE){
			rs = stat.executeQuery("select count(*) as TOTAL from ARENARESULTS WHERE heroid = " + heroid);
			
			if(rs.next()){
				total += rs.getInt("TOTAL");
			}
		} else {
			rs = stat.executeQuery("select count(*) as TOTAL from MATCHES WHERE myheroid = " + heroid + " AND MODE=" + mode);
			
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
			rs = stat.executeQuery("select wins,losses from ARENARESULTS where heroId = " + heroId);
			
			while(rs.next()){
				found = true;
				wins += rs.getInt("WINS");
				losses += rs.getInt("LOSSES");
			}
		} else {
			rs = stat.executeQuery("select win FROM MATCHES where MYHEROID = " + heroId + " AND MODE=" + mode);
			
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
			rs = stat.executeQuery("select wins from ARENARESULTS where heroId = " + heroId);
			
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
			rs = stat.executeQuery("select WIN from MATCHES WHERE MODE=" + mode + " AND DELETED=0");
			while(rs.next()){
				found = true;
				
				if(rs.getInt("WINS") == 1){
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
			rs = stat.executeQuery("select wins,losses from ARENARESULTS");
			while(rs.next()){
				total += rs.getInt("WINS");
			}
		} else {
			rs = stat.executeQuery("select WIN from MATCHES WHERE mode=" + mode);
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
			rs = stat.executeQuery("select wins,losses from ARENARESULTS WHERE heroid=" + heroId);
			while(rs.next()){
				total += rs.getInt("WINS");
			}
		} else {
			rs = stat.executeQuery("select WIN from MATCHES WHERE myheroid=" + heroId + " AND mode=" + mode);
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
			rs = stat.executeQuery("select WIN from MATCHES WHERE mode=" + mode + " AND DELETED=0");
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
			rs = stat.executeQuery("select WIN from MATCHES WHERE myheroid=" + heroId + " AND mode=" + mode + " AND DELETED=0");
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
		String sqlWhere = " WHERE goesFirst = " + goesFirst + " AND mode = " + mode + " AND DELETED=0";
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
	
	public ResultSet getMatches() throws SQLException{
		ResultSet rs;
		rs = stat.executeQuery("select * from MATCHES WHERE DELETED=0 ORDER BY ID DESC LIMIT 50");

		return rs;
	}
	
	public ResultSet getMatch(int id) throws SQLException{
		ResultSet rs;
		rs = stat.executeQuery("select * from MATCHES WHERE id = " + id);

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
