package my.hearthtracking.app;

public class HearthMatch {
	public static final int GAME_RESULT_DRAW 	= -2;
	public static final int GAME_RESULT_UNKNOWN = -1;
	public static final int GAME_RESULT_DEFEAT 	= 0;
	public static final int GAME_RESULT_VICTORY = 1;
	
	public static final int GAME_BOTH_COIN = -1;
	public static final int GAME_WITH_COIN = 0;
	public static final int GAME_NO_COIN = 1;
	
	public static final int GAME_GO_UNKNOWN = GAME_BOTH_COIN;
	public static final int GAME_GO_FIRST 	= GAME_NO_COIN;
	public static final int GAME_GO_SECOND 	= GAME_WITH_COIN;
	
	
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
	
	public static String goesFirstToString(int goesFirst){
		switch(goesFirst){
			case GAME_NO_COIN:
				return "First";
	
			case GAME_WITH_COIN:
				return "Second";
		}
	
		return "Unknown";
	}
	
	public static String coinToString(int coin){
		switch(coin){
			case GAME_WITH_COIN:
				return "Coin";
	
			case GAME_NO_COIN:
				return "No Coin";
			
			case GAME_BOTH_COIN:
				return "Both";
		}
		
		return "Unknown";
	}

}
