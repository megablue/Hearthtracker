package my.hearthtracking.app;

public class HearthMatch {
	public static final int GAME_RESULT_DRAW 	= -2;
	public static final int GAME_RESULT_UNKNOWN = -1;
	public static final int GAME_RESULT_DEFEAT 	= 0;
	public static final int GAME_RESULT_VICTORY = 1;
	
	
	public HearthMatch() {
		
	}
	
	public static String gameResultToString(int result){
		switch(result){
			case GAME_RESULT_DRAW:
			return "draw";
						
			case GAME_RESULT_DEFEAT:
			return "defeat";
			
			case GAME_RESULT_VICTORY:
			return "victory";
			
			case GAME_RESULT_UNKNOWN:
			default: 
			return "unknown";
		}
	}

}
