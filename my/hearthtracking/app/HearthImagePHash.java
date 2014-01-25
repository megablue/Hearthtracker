package my.hearthtracking.app;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import edu.emory.mathcs.jtransforms.dct.DoubleDCT_2D;

public class HearthImagePHash {

	private final int size;
	private final int smallerSize;
	private DoubleDCT_2D dct = null;

	public HearthImagePHash(int size, int smallerSize) {
		this.size = size;
		this.smallerSize = smallerSize;
		dct = new DoubleDCT_2D(size, size);
	}
	
	public int distance(String s1, String s2) {
		s1 = extractHash(s1);
		s2 = extractHash(s2);
		
		int counter = 0;
		for (int k = 0; k < s1.length();k++) {
			if(s1.charAt(k) != s2.charAt(k)) {
				counter++;
			}
		}
		return counter;
	}
	
	public static float getRGBScore(String t1, String t2){
		return getRGBScore(getRGB(t1), getRGB(t2));
	}
	
	public static float getRGBScore(int[] rgb1, int[] rgb2){
		
		int diff = Math.abs(rgb1[0] - rgb2[0]) + Math.abs(rgb1[1] - rgb2[1]) + Math.abs(rgb1[2] - rgb2[2]);
		
		return  (255f - diff)/255f;
	}
	
	public static int[] getRGB(String hash){
		String[] parts = hash.split("-");
		int[] rgb = {0, 0, 0};
		rgb[0] = Integer.parseInt(parts[0]);
		rgb[1] = Integer.parseInt(parts[1]);
		rgb[2] = Integer.parseInt(parts[2]);
		return rgb;
	}
	
	public static String extractHash(String hash){
		String[] parts = hash.split("-");
		return parts.length > 3 ? parts[3] : "";
	}
	
	public float getPHashScore(String hash, int distance){
		int max = hash.length();
		float score = 1 - ((float)distance/max);
		return score;
	}
	
	// Returns a 'binary string' (like. 001010111011100010) which is easy to do a hamming distance on. 
	public String getHash(BufferedImage img){

		//well usually it will always end up resizing
		if(img.getWidth() != size || img.getHeight() != size){
			//resize the image
			img = resize(img, size, size);
		}
		
		//reducing the colors doesn't seems to help at all
		//it is even slower with and result seems to be the identical
		//img = grayscale(img);
		
		double[][] vals = new double[size][size];
		int rgb = 0;
		int red = 0;
		int green = 0;
		int blue = 0;
		
		//convert pixels to double double array
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				rgb = (img.getRGB(x, y));
				red 	+= (rgb >> 16) & 0xFF;
				green	+= (rgb >> 8) & 0xFF; 
				blue	+= rgb & 0xFF; 
				vals[x][y] = rgb;
			}
		}
		
		//compute DCT
		dct.forward(vals, true);
		
		/* 4. Reduce the DCT. 
		 * This is the magic step. While the DCT is 32x32, just keep the 
		 * top-left 8x8. Those represent the lowest frequencies in the 
		 * picture.
		 */
		/* 5. Compute the average value. 
		 * Like the Average Hash, compute the mean DCT value (using only 
		 * the 8x8 DCT low-frequency values and excluding the first term 
		 * since the DC coefficient can be significantly different from 
		 * the other values and will throw off the average).
		 */
		double total = 0;
		
		for (int x = 0; x < smallerSize; x++) {
			for (int y = 0; y < smallerSize; y++) {
				total += vals[x][y];
			}
		}
		total -= vals[0][0];
		
		double avg = total / (double) ((smallerSize * smallerSize) - 1);
	
		/* 6. Further reduce the DCT. 
		 * This is the magic step. Set the 64 hash bits to 0 or 1 
		 * depending on whether each of the 64 DCT values is above or 
		 * below the average value. The result doesn't tell us the 
		 * actual low frequencies; it just tells us the very-rough 
		 * relative scale of the frequencies to the mean. The result 
		 * will not vary as long as the overall structure of the image 
		 * remains the same; this can survive gamma and color histogram 
		 * adjustments without a problem.
		 */
		String hash = "";
		
		for (int x = 0; x < smallerSize; x++) {
			for (int y = 0; y < smallerSize; y++) {
				if (x != 0 && y != 0) {
					hash += (vals[x][y] > avg ? "1":"0");
				}
			}
		}
		
		red 	= red	/(size * size);
		green 	= green	/(size * size);
		blue 	= blue	/(size * size);
		
		return red + "-" + green + "-" + blue + "-" + hash;
	}
	
	private BufferedImage resize(BufferedImage image, int width, int height) {
		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}
}