package my.hearthtracking.app;

import java.awt.image.BufferedImage;
import java.io.File;

public class HearthTest{

	public static void main( String args[] ) {
		testArenaWins();
    }
	
	public static void testArenaWins(){
		HearthImageSurf surf = new HearthImageSurf();
		HearthImagePHash pHash = new HearthImagePHash(32, 16);
		BufferedImage[] arenaWins = new BufferedImage[13];
		int counter = 0;
		
		for(int i = 0; i < arenaWins.length; i++){
			arenaWins[i] = HearthHelper.loadImage(new File("./images/" + i + ".png"));
		}

		System.out.println("Generating SURF scores for arena wins...");
		
		long start = System.currentTimeMillis();
		long init = 0;
		
		for(int x = 0; x < arenaWins.length; x++){
    		for(int y = 0; y < arenaWins.length; y++){
				float score = surf.compare(arenaWins[x], arenaWins[y]);

				if(init == 0){
					init = System.currentTimeMillis() - start;
				}
				
				if(score >= 0.9){
					System.out.println(x + " vs " + y + ", score: " + score);
				}
				
				if(score >= 0.8 && score < 0.9){
					System.out.println(x + " vs " + y + ", score: " + score + " (close match)");
				}
				
				++counter;
    		}
		}

        System.out.println("Init: " + init + " ms");
        System.out.println("Total: " + (System.currentTimeMillis() - start) + " ms");
        System.out.println("Average: " + (System.currentTimeMillis() - start)/counter + " ms");

		System.out.println("Generating Pecpectual scores for arena wins...");
		
		start = System.currentTimeMillis();
		init = 0;
		counter = 0;
		
		for(int x = 0; x < arenaWins.length; x++){
    		for(int y = 0; y < arenaWins.length; y++){
    			String a = pHash.getHash(arenaWins[x]);
    			String b = pHash.getHash(arenaWins[y]);
    			int distance = pHash.distance(a, b);
    			float score = pHash.getPHashScore(a, distance);
    			
				if(init == 0){
					init = System.currentTimeMillis() - start;
				}
				
				if(score >= 0.9){
					System.out.println(x + " vs " + y + ", score: " + score);
				}
				
				if(score >= 0.8 && score < 0.9){
					System.out.println(x + " vs " + y + ", score: " + score + " (close match)");
				}
				
				++counter;
    		}
		}

        System.out.println("Init: " + init + " ms");
        System.out.println("Total: " + (System.currentTimeMillis() - start) + " ms");
        
        float avg = (System.currentTimeMillis() - start)/(float)counter;
        System.out.println("Average: " +  avg + " ms");
	}
}
