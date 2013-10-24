import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.h2.jdbcx.JdbcDataSource;

public class Tracker {
	Connection conn;
	
	public Tracker(){
		this.initDB();
		try {
			this.selfTest();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initDB(){
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:.//data/database");
		ds.setUser("tracker");
		ds.setPassword("tracker");
		
		try {
			conn = ds.getConnection();
			this.createTables();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void selfTest() throws SQLException{
		this.truncateDB();
		this.saveArenaResult(0, 9, 0);
		this.saveArenaResult(0, 9, 0);
		this.saveArenaResult(0, 9, 9);
		assert this.getOverallWinRate() == 75.0f;
		
		this.saveMatchResult(0, 0, 1, 1, new Date(), 0);
		this.saveMatchResult(1, 0, 1, 0, new Date(), 0);
		this.saveMatchResult(2, 0, 0, 0, new Date(), 0);
		this.saveMatchResult(3, 0, 0, 1, new Date(), 0);
		this.saveMatchResult(4, 0, 0, 1, new Date(), 0);
		this.saveMatchResult(5, 0, 0, 1, new Date(), 0);
		
		assert this.getWinRateByGoesFirst() == 50.0f;
		assert this.getWinRateByGoesSecond() == 75.0f;
	}
	
	private void truncateDB() throws SQLException{
		Statement stat = conn.createStatement();
		stat.execute("TRUNCATE TABLE ARENARESULTS");
		stat.execute("TRUNCATE TABLE ARENAMATCHES");
		stat.close();
	}
	
	private void closeDB(){
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createTables() throws SQLException{
		 Statement stat = conn.createStatement();
		 ResultSet rs;
	     
		 rs = stat.executeQuery("select count(*) from information_schema.tables where table_name = 'ARENARESULTS'");
		 
		 if(rs.next()){
			 if(rs.getInt("COUNT(*)") == 0){
				 stat.execute("create table arenaResults(id int primary key auto_increment, heroId int, wins int, losses int, timeCaptured TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
				 stat.execute("CREATE INDEX heroId ON arenaResults(heroId)");
			 }
		 }
		 
		 rs = stat.executeQuery("select count(*) from information_schema.tables where table_name = 'ARENAMATCHES'");
		 
		 if(rs.next()){
			 if(rs.getInt("COUNT(*)") == 0){
				 stat.execute("create table arenaMatches(id int primary key auto_increment, myHeroId int, oppHeroId int, goesFirst int, win int, startTime TIMESTAMP, totalTime int)");
				 stat.execute("CREATE INDEX myHeroId ON arenaMatches(myHeroId)");
			 }
		 }

		 stat.close();
	}
	
	public void saveArenaResult(int heroId, int wins, int losses) throws SQLException{
		String sql = "INSERT INTO arenaResults(heroId, wins, losses) VALUES(" + heroId + "," + wins + "," + losses + ")";
		Statement stat = conn.createStatement();
		stat.execute(sql);
		stat.close();
	}
	
	public void saveMatchResult(int myHeroId, int oppHeroId, int goesFirst, int win, Date startTime, int totalTime) throws SQLException{
		java.sql.Timestamp sqlDate = new java.sql.Timestamp(startTime.getTime());
		
		String sql = "INSERT INTO arenaMatches(myHeroId, oppHeroId, goesFirst, win, startTime, totalTime) " 
					+ "VALUES(" + myHeroId + "," 
					+ oppHeroId + "," 
					+ goesFirst + "," 
					+ win + ","
					+ "'" + sqlDate.toString() + "'" + ","
					+ totalTime + ")";

		Statement stat = conn.createStatement();
		stat.execute(sql);
		stat.close();
	}
	
	public float getWinRateByHero(int heroId) throws SQLException{
		Statement stat = conn.createStatement();
		ResultSet rs;
		int wins = 0;
		int losses = 0;
		float winrate = -1;
		boolean found = false;
		
		rs = stat.executeQuery("select wins,losses from ARENARESULTS where heroId = " + heroId);
		
		while(rs.next()){
			found = true;
			wins += rs.getInt("WINS");
			losses += rs.getInt("LOSSES");
		}
		
		if(found){
			winrate = (float) wins/(wins+losses);
			System.out.println("Winrate (" + heroId + "): " + winrate);
			return winrate;
		}
		
		return -1;
	}
	
	public float getOverallWinRate() throws SQLException{
		Statement stat = conn.createStatement();
		ResultSet rs;
		int wins = 0;
		int losses = 0;
		float winrate = -1;
		boolean found = false;
		
		rs = stat.executeQuery("select wins,losses from ARENARESULTS");
		
		while(rs.next()){
			found = true;
			wins += rs.getInt("WINS");
			losses += rs.getInt("LOSSES");
		}
		
		if(found){
			winrate = (float) wins/(wins+losses) * 100;
			System.out.println("Winrate (overall): " + winrate);
			return winrate;
		}
		
		return -1;
	}
	
	public float getWinRateByGoesFirst() throws SQLException{
		return this.getWinRateGoesBy(true, -1, -1);
	}
	
	public float getWinRateByGoesSecond() throws SQLException{
		return this.getWinRateGoesBy(false, -1, -1);
	}
	
	public float getWinRateGoesBy(boolean goesFirstFlag, int heroId, int against) throws SQLException{
		Statement stat = conn.createStatement();
		ResultSet rs;
		float winrate = -1;
		int goesFirst = goesFirstFlag ? 1 : 0;
		String sql = "select SUM(WIN), COUNT(*) from ARENAMATCHES where goesFirst = " + goesFirst;
		
		if(heroId > -1){
			sql += " AND myHeroId = " + heroId;
		} 
		
		if(against > -1){
			sql += " AND oppHeroId = " + against;
		}
		
		rs = stat.executeQuery(sql);
		
		if(rs.next()){
			winrate = rs.getFloat("SUM(WIN)") / rs.getFloat("COUNT(*)") * 100;
			
			if(goesFirstFlag){
				System.out.println("Winrate (goes first): " + winrate);
			} else {
				System.out.println("Winrate (goes second): " + winrate);
			}
			
			return winrate;
		}
		
		return -1;
	}
}
