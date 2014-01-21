package my.hearthtracking.app;

import java.awt.image.BufferedImage;

import boofcv.struct.image.ImageFloat32;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.alg.feature.detect.template.TemplateMatching;
import boofcv.factory.feature.detect.template.FactoryTemplateMatching;
import boofcv.factory.feature.detect.template.TemplateScoreType;
import boofcv.struct.feature.Match;

import java.util.List;

public class HearthImageAnalyzer {
	
	public static Match templateMatch(BufferedImage image, BufferedImage template) {
		ImageFloat32 src = null; 
		src = ConvertBufferedImage.convertFrom(image, src);
		
		ImageFloat32 target = null; 
//		template = HearthHelper.cropImage(template, 0, 0, template.getWidth() - 10, template.getHeight() - 10);
		target = ConvertBufferedImage.convertFrom(template, target);
		
		List<Match> matches = findMatches(src, target, 1);
		
		return (matches.size() > 0) ? matches.get(0) : null;
	}
	
    /**
     * Demonstrates how to search for matches of a template inside an image
     *
     * @param image           Image being searched
     * @param template        Template being looked for
     * @param expectedMatches Number of expected matches it hopes to find
     * @return List of match location and scores
     */
    public static List<Match> findMatches(ImageFloat32 image, ImageFloat32 template, int expectedMatches) {
            // create template matcher.
            TemplateMatching<ImageFloat32> matcher =
                            FactoryTemplateMatching.createMatcher(TemplateScoreType.SUM_DIFF_SQ, ImageFloat32.class);
            
            matcher.setMinimumSeparation(0);
            // Find the points which match the template the best
            matcher.setTemplate(template, expectedMatches);
            matcher.process(image);

            return matcher.getResults().toList();

    }
    
	public static float compare(BufferedImage a, BufferedImage b){
		
		BufferedImage a1 = HearthHelper.resizeImage(a, 32, 32);
		BufferedImage b1 = HearthHelper.resizeImage(b, 32, 32);
		int aPixel = 0, bPixel = 0;
		int matchCount = 0;
		
		for(int x = 0; x < 32; x++){
			for(int y = 0; y < 32; y++){
				aPixel = a1.getRGB(x, y);
				bPixel = b1.getRGB(x,y );

				if(Math.abs((aPixel - bPixel)) < 10){
					matchCount++;
				}
			}
		}
		
		return (float) matchCount/(32*32);
	}
}
