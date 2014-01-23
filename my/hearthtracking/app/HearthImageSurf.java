package my.hearthtracking.app;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.alg.feature.UtilFeature;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.struct.feature.AssociatedIndex;
import boofcv.struct.feature.SurfFeature;
import boofcv.struct.image.ImageFloat32;
import my.hearthtracking.app.HearthScannerSettings.Scanbox;

import org.ddogleg.struct.FastQueue;

public class HearthImageSurf {
	ScoreAssociation<SurfFeature> scorer = FactoryAssociation.defaultScore(SurfFeature.class);
    AssociateDescription<SurfFeature> associate = FactoryAssociation.greedy(scorer, Double.MAX_VALUE, true);
    
    public float compare(BufferedImage a, BufferedImage b){
    	return compare(getDesc(a), getDesc(b));
    }
    
	public float compare(DetectDescribePoint<ImageFloat32,SurfFeature> template, DetectDescribePoint<ImageFloat32,SurfFeature> region){
		FastQueue<SurfFeature> descTemplate = UtilFeature.createQueue(template,100);
        FastQueue<SurfFeature> descRegion = UtilFeature.createQueue(template,100);
        
        describeImage(template, descTemplate);
        describeImage(region, descRegion);
        
        associate.setSource(descTemplate);
        associate.setDestination(descRegion);
        associate.setThreshold(0.9);
        associate.associate();

        FastQueue<AssociatedIndex> matches = associate.getMatches();
        float sumScore = 0;
        float average = 0;
                
        for(int i = 0; i < matches.size(); i++){
        	sumScore += matches.get(i).fitScore;
        }
        
        average = 1 - (sumScore/matches.size());
          
        return average;
	}

	public DetectDescribePoint<ImageFloat32,SurfFeature> getDesc( BufferedImage src ) {
		ImageFloat32 image = convertImage(src);
		
        // create the detector and descriptors
        DetectDescribePoint<ImageFloat32,SurfFeature> surf = FactoryDetectDescribe.
                        surfFast(new ConfigFastHessian(0, 2, 200, 2, 9, 4, 4), null, null, ImageFloat32.class);

         // specify the image to process
        surf.detect(image);
        
        return surf;
	}
			
    private void describeImage(DetectDescribePoint<ImageFloat32,SurfFeature> detDesc, FastQueue<SurfFeature> descs )
    {
    	for( int i = 0; i < detDesc.getNumberOfFeatures(); i++ ) {
    		descs.grow().setTo(detDesc.getDescription(i));
    	}
    }

	private ImageFloat32 convertImage(BufferedImage src){
		return ConvertBufferedImage.convertFromSingle(src, null, ImageFloat32.class);
	}
}
