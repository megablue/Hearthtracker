package my.hearthtracking.app;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import my.hearthtracking.app.HearthScannerSettings.Scanbox;

public class HearthTest{
	public static void main( String args[] ) {
		testCaputreDWM();
    }
	
	public static void testCaputreDWM(){
		System.out.println("HearthHelper.getHearthstoneHandle(): " + HearthHelper.getHearthstoneHandle());

		long start = System.currentTimeMillis();
		
		for(int i = 0; i < 1000; i++){
			HearthRobot.capture(HearthHelper.getHearthstoneHandle(), new Rectangle(0,0,1024,768));
		}
		
        System.out.println("Total: " + (System.currentTimeMillis() - start) + " ms");
        float average = (System.currentTimeMillis() - start)/1000f;
        System.out.println("Total scan: " + 1000);
        System.out.println("Average: " +  average + " ms");
		
		//HearthHelper.bufferedImageToFile(cap, "./cache/capture.png");
	}
	
	public static void testDeckSelectionRGB(){
		HearthScannerSettings setting = new HearthScannerSettings();
		HearthImagePHash pHash = new HearthImagePHash(16, 8);
		
		for(Scanbox sb : setting.list){
			if(sb.scene.equals("deckSelection")){
				sb.target = HearthHelper.loadImage(new File("./images/" + sb.imgfile));
			}
		}
		
		long start = System.currentTimeMillis();
		long init = 0;
		int counter = 0;
		float average = 0;
		int rgbAll[] = {0, 0, 0};
		
		for(Scanbox sbA : setting.list){
			if(sbA.scene.equals("deckSelection")){
    			String a = pHash.getHash(sbA.target);
    			int[] rgbA = HearthImagePHash.getRGB(a);
    			
    			rgbAll[0] += rgbA[0];
    			rgbAll[1] += rgbA[1];
    			rgbAll[2] += rgbA[2];

    			++counter;    			
			}
		}

		rgbAll[0] = rgbAll[0]/counter;
		rgbAll[1] = rgbAll[1]/counter;
		rgbAll[2] = rgbAll[2]/counter;

		System.out.println("Average RGB: " + rgbAll[0] + "," + rgbAll[1] + "," +  rgbAll[2]);
		
        System.out.println("Init: " + init + " ms");
        System.out.println("Total: " + (System.currentTimeMillis() - start) + " ms");
        
        average = (System.currentTimeMillis() - start)/(float)counter;
        System.out.println("Total scan: " + counter);
        System.out.println("Average: " +  average + " ms");
	}
	
	public static void testGameModes(){
		HearthScannerSettings setting = new HearthScannerSettings();
		HearthImagePHash pHash = new HearthImagePHash(16, 8);
		
		for(Scanbox sb : setting.list){
			if(sb.scene.equals("gameMode")){
				sb.target = HearthHelper.loadImage(new File("./images/" + sb.imgfile));
			}
		}
		
		long start = System.currentTimeMillis();
		long init = 0;
		int counter = 0;
		float average = 0;
		
		for(Scanbox sbA : setting.list){
			if(sbA.scene.equals("gameMode")){
    			String a = pHash.getHash(sbA.target);
    			int[] rgbA = HearthImagePHash.getRGB(a);
    			
    			for(Scanbox sbB : setting.list){
    				if(!sbB.scene.equals("gameMode")){
    					continue;
    				}
    				
    				String b = pHash.getHash(sbB.target);
    				int distance = pHash.distance(a, b);
    				int[] rgbB = HearthImagePHash.getRGB(b);
        			float score = pHash.getPHashScore(a, distance);
        			
    				if(init == 0){
    					init = System.currentTimeMillis() - start;
    				}
    				
    				if(score >= 0.8){
    					
    					if(score < 0.9){
    						System.out.println(sbA.identifier + " vs " + sbB.identifier + ", score: " + score + " (close match)");
    					} else {
    						System.out.println(sbA.identifier + " vs " + sbB.identifier + ", score: " + score);
    					}
    					
    					System.out.println(sbA.identifier + "'s rgb: " + rgbA[0] + "," + rgbA[1] + "," + rgbA[2]);
    					System.out.println(sbB.identifier + "'s rgb: " + rgbB[0] + "," + rgbB[1] + "," + rgbB[2]);
    					System.out.println("rgb score: " + HearthImagePHash.getRGBScore(rgbA, rgbB));
    				}
    				  				
    				++counter;
    			}
    			
    			++counter;
			}
		}
		
        System.out.println("Init: " + init + " ms");
        System.out.println("Total: " + (System.currentTimeMillis() - start) + " ms");
        
        average = (System.currentTimeMillis() - start)/(float)counter;
        System.out.println("Total scan: " + counter);
        System.out.println("Average: " +  average + " ms");
	}
	
	public static void testArenaWins(){
		HearthImageSurf surf = new HearthImageSurf();
		HearthImagePHash pHash = new HearthImagePHash(16, 8);
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
				
				if(score <= 0.1f){
					System.out.println(x + " vs " + y + ", score: " + score);
				}
				
				if(score >= 0.1f && score < 0.2f){
					System.out.println(x + " vs " + y + ", score: " + score + " (close match)");
				}
				
				++counter;
    		}
		}

		float average = (System.currentTimeMillis() - start)/counter;
		
        System.out.println("Init: " + init + " ms");
        System.out.println("Total: " + (System.currentTimeMillis() - start) + " ms");
        System.out.println("Average: " + average + " ms");

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
        
        average = (System.currentTimeMillis() - start)/(float)counter;
        System.out.println("Total scan: " + counter);
        System.out.println("Average: " +  average + " ms");
	}
	
	public static void testHeroes(){
		HearthScannerSettings setting = new HearthScannerSettings();
		HearthImagePHash pHash = new HearthImagePHash(16, 8);
		
		for(Scanbox sb : setting.list){
			if(sb.scene.equals("arenaHero")){
				sb.target = HearthHelper.loadImage(new File("./images/" + sb.imgfile));
			}
		}
		
		long start = System.currentTimeMillis();
		long init = 0;
		int counter = 0;
		float average = 0;
		
		for(Scanbox sbA : setting.list){
			if(sbA.scene.equals("arenaHero")){
    			String a = pHash.getHash(sbA.target);
    			
    			for(Scanbox sbB : setting.list){
    				if(!sbB.scene.equals("arenaHero")){
    					continue;
    				}
    				
    				String b = pHash.getHash(sbB.target);
    				int distance = pHash.distance(a, b);
        			float score = pHash.getPHashScore(a, distance);
        			
    				if(init == 0){
    					init = System.currentTimeMillis() - start;
    				}
    				
    				if(score >= 0.9){
    					System.out.println(sbA.identifier + " vs " + sbB.identifier + ", score: " + score);
    				}
    				
    				if(score >= 0.8 && score < 0.9){
    					System.out.println(sbA.identifier + " vs " + sbB.identifier + ", score: " + score + " (close match)");
    				}
    				
    				++counter;
    			}
    			
    			++counter;
			}
		}
		
        System.out.println("Init: " + init + " ms");
        System.out.println("Total: " + (System.currentTimeMillis() - start) + " ms");
        
        average = (System.currentTimeMillis() - start)/(float)counter;
        System.out.println("Total scan: " + counter);
        System.out.println("Average: " +  average + " ms");

		start = System.currentTimeMillis();
		init = 0;
		counter = 0;
		average = 0;
        
		for(Scanbox sbA : setting.list){
			if(!sbA.scene.equals("oppHero")){
				continue;
			}
			
			String a = pHash.getHash(sbA.target);
			
			for(Scanbox sbB : setting.list){
				if(!sbB.scene.equals("oppHero")){
					continue;
				}
				
				String b = pHash.getHash(sbB.target);
				int distance = pHash.distance(a, b);
    			float score = pHash.getPHashScore(a, distance);
    			
				if(init == 0){
					init = System.currentTimeMillis() - start;
				}
				
				if(score >= 0.9){
					System.out.println(sbA.identifier + " vs " + sbB.identifier + ", score: " + score);
				}
				
				if(score >= 0.8 && score < 0.9){
					System.out.println(sbA.identifier + " vs " + sbB.identifier + ", score: " + score + " (close match)");
				}
				
				++counter;
			}
			
			++counter;
		}
		
        System.out.println("Init: " + init + " ms");
        System.out.println("Total: " + (System.currentTimeMillis() - start) + " ms");
        
        average = (System.currentTimeMillis() - start)/(float)counter;
        System.out.println("Total scan: " + counter);
        System.out.println("Average: " +  average + " ms");
        
		start = System.currentTimeMillis();
		init = 0;
		counter = 0;
		average = 0;
        
		for(Scanbox sbA : setting.list){
			if(!sbA.scene.equals("myHero")){
				continue;
			}
			
			String a = pHash.getHash(sbA.target);
			
			for(Scanbox sbB : setting.list){
				if(!sbB.scene.equals("myHero")){
					continue;
				}
				
				String b = pHash.getHash(sbB.target);
				int distance = pHash.distance(a, b);
    			float score = pHash.getPHashScore(a, distance);
    			
				if(init == 0){
					init = System.currentTimeMillis() - start;
				}
				
				if(score >= 0.9){
					System.out.println(sbA.identifier + " vs " + sbB.identifier + ", score: " + score);
				}
				
				if(score >= 0.8 && score < 0.9){
					System.out.println(sbA.identifier + " vs " + sbB.identifier + ", score: " + score + " (close match)");
				}
				
				++counter;
			}
			
			++counter;
		}
		
        System.out.println("Init: " + init + " ms");
        System.out.println("Total: " + (System.currentTimeMillis() - start) + " ms");
        
        average = (System.currentTimeMillis() - start)/(float)counter;
        System.out.println("Total scan: " + counter);
        System.out.println("Average: " +  average + " ms");
	}
}
