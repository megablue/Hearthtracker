import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MyHearth {
	static boolean debugMode = true;
	static HearthReader hearth;
	static Tracker tracker;
	
	static String[] winsLabels = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
	static String[] lifeLabels = {"0", "1", "2", "3"};
		
	public static void main(String[] args) {
		int previousLosses = -1;
		int currentLosses = -1;
		int previousWins = -1;
		int currentWins = -1;
		int previousHero = -1;
		int currentHero = -1;
		int heroId = -1;
		int sleep = 1000;
		
		hearth = new HearthReader(debugMode);
		tracker = new Tracker();
		
		clearLines();
		
		while(true){
			if(hearth.isScoreScreen()){
				System.out.println("Found score screen..." );
				
				previousWins = currentWins;
				currentWins = hearth.getWins();
				
				previousLosses = currentLosses;
				currentLosses = hearth.getLosses();
								
				heroId = hearth.getHero();
				
				if(heroId > -1){
					previousHero = currentHero;
					currentHero = heroId;
				}
				
				if(currentHero != previousHero || currentWins != previousWins || currentLosses != previousLosses){
					updateLines(currentWins, currentLosses, currentHero);
				}
			}
			
			try {
				TimeUnit.MILLISECONDS.sleep(sleep);
			} catch (InterruptedException e) {
			}
		}

	}
	
	public static void clearLines(){
		try {
			PrintWriter line1Writer = new PrintWriter(".\\output\\line1.txt", "UTF-8");
			line1Writer.println("Arena Status: ??????");
			line1Writer.close();
			
			line1Writer = new PrintWriter(".\\output\\line2.txt", "UTF-8");
			line1Writer.println("");
			line1Writer.close();
			
			line1Writer = new PrintWriter(".\\output\\line3.txt", "UTF-8");
			line1Writer.println("");
			line1Writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	
	public static void updateLines(int currentWins, int currentLosses, int currentHero){
		
		try {
			PrintWriter line1Writer = new PrintWriter(".\\output\\line1.txt", "UTF-8");
			
			if(debugMode){
				System.out.println("currentWins: " + currentWins);
				System.out.println("currentLosses: " + currentLosses);
			}
			
			if( (currentWins > -1) && (currentLosses > -1) ){
				line1Writer.println("Arena Score: " + winsLabels[currentWins] + " - " + lifeLabels[currentLosses]);
			}
			
			line1Writer.close();
			
			line1Writer = new PrintWriter(".\\output\\line2.txt", "UTF-8");
			line1Writer.println("Class: " + hearth.getHeroLabel(currentHero));
			line1Writer.close();
			
		   	Calendar cal = Calendar.getInstance();
	    	cal.getTime();
	    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			
			line1Writer = new PrintWriter(".\\output\\line3.txt", "UTF-8");
			line1Writer.println("Last Update: " + sdf.format(cal.getTime()));
			line1Writer.close();
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

	}

}
