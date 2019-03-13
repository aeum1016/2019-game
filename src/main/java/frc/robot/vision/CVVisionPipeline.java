package frc.robot.vision;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.explodingbacon.bcnlib.vision.Contour;
import com.explodingbacon.bcnlib.vision.Rectangle;
import org.opencv.core.*;
import org.opencv.core.Core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;
import org.opencv.objdetect.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

/**
 * ExamplePipeline class.
 *
 * <p>An OpenCV pipeline generated by GRIP.
 *
 * @author GRIP
 */
public class CVVisionPipeline {

    //Outputs
    private Mat hsvThresholdOutput = new Mat();
    private ArrayList<MatOfPoint> findContoursOutput = new ArrayList<MatOfPoint>();
    private ArrayList<MatOfPoint> filterContoursOutput = new ArrayList<MatOfPoint>();
    private ArrayList<Contour> finalContoursOutput = new ArrayList<Contour>();
    private double pixelOffset;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * This is the primary method that runs the entire pipeline and updates the outputs.
     */
    public void process(Mat source0) {
        // Step HSV_Threshold0:
        Mat hsvThresholdInput = source0;
        double[] hsvThresholdHue = {60, 110.0};
        double[] hsvThresholdSaturation = {150.0, 255.0};
        double[] hsvThresholdValue = {50.0, 255.0};
        hsvThreshold(hsvThresholdInput, hsvThresholdHue, hsvThresholdSaturation, hsvThresholdValue, hsvThresholdOutput);

        // Step Find_Contours0:
        Mat findContoursInput = hsvThresholdOutput;
        boolean findContoursExternalOnly = false;
        findContours(findContoursInput, findContoursExternalOnly, findContoursOutput);

        // Step Filter_Contours0:
        ArrayList<MatOfPoint> filterContoursContours = findContoursOutput;
        double filterContoursMinArea = 100;
        double filterContoursMinPerimeter = 0.0;
        double filterContoursMinWidth = 0.0;
        double filterContoursMaxWidth = 1000.0;
        double filterContoursMinHeight = 0.0;
        double filterContoursMaxHeight = 1000.0;
        double[] filterContoursSolidity = {0, 100};
        double filterContoursMaxVertices = 1000000.0;
        double filterContoursMinVertices = 0.0;
        double filterContoursMinRatio = 0.0;
        double filterContoursMaxRatio = 1000.0;
        filterContours(filterContoursContours, filterContoursMinArea, filterContoursMinPerimeter, filterContoursMinWidth, filterContoursMaxWidth, filterContoursMinHeight, filterContoursMaxHeight, filterContoursSolidity, filterContoursMaxVertices, filterContoursMinVertices, filterContoursMinRatio, filterContoursMaxRatio, filterContoursOutput);
        finalContoursOutput = identifyContours(filterContoursOutput);
        pixelOffset = getCenterOffset(finalContoursOutput, 213); //426
    }

    /**
     * This method is a generated getter for the output of a HSV_Threshold.
     * @return Mat output from HSV_Threshold.
     */
    public Mat hsvThresholdOutput() {
        return hsvThresholdOutput;
    }

    /**
     * This method is a generated getter for the output of a Find_Contours.
     * @return ArrayList<MatOfPoint> output from Find_Contours.
     */
    public ArrayList<MatOfPoint> findContoursOutput() {
        return findContoursOutput;
    }

    /**
     * This method is a generated getter for the output of a Filter_Contours.
     * @return ArrayList<MatOfPoint> output from Filter_Contours.
     */
    public ArrayList<MatOfPoint> filterContoursOutput() {
        return filterContoursOutput;
    }


    /**
     * Segment an image based on hue, saturation, and value ranges.
     *
     * @param input The image on which to perform the HSL threshold.
     * @param hue The min and max hue
     * @param sat The min and max saturation
     * @param val The min and max value
     * @param out The image in which to store the output.
     */
    private void hsvThreshold(Mat input, double[] hue, double[] sat, double[] val,
                              Mat out) {
        Imgproc.cvtColor(input, out, Imgproc.COLOR_BGR2HSV);
        Core.inRange(out, new Scalar(hue[0], sat[0], val[0]),
                new Scalar(hue[1], sat[1], val[1]), out);
    }

    /**
     * Sets the values of pixels in a binary image to their distance to the nearest black pixel.
     * @param input The image on which to perform the Distance Transform.
     * @param externalOnly The Transform.
     */
    private void findContours(Mat input, boolean externalOnly,
                              List<MatOfPoint> contours) {
        Mat hierarchy = new Mat();
        contours.clear();
        int mode;
        if (externalOnly) {
            mode = Imgproc.RETR_EXTERNAL;
        }
        else {
            mode = Imgproc.RETR_LIST;
        }
        int method = Imgproc.CHAIN_APPROX_SIMPLE;
        Imgproc.findContours(input, contours, hierarchy, mode, method);
    }


    /**
     * Filters out contours that do not meet certain criteria.
     * @param inputContours is the input list of contours
     * @param output is the the output list of contours
     * @param minArea is the minimum area of a contour that will be kept
     * @param minPerimeter is the minimum perimeter of a contour that will be kept
     * @param minWidth minimum width of a contour
     * @param maxWidth maximum width
     * @param minHeight minimum height
     * @param maxHeight maximimum height
     * @param minVertexCount minimum vertex Count of the contours
     * @param maxVertexCount maximum vertex Count
     * @param minRatio minimum ratio of width to height
     * @param maxRatio maximum ratio of width to height
     */
    private void filterContours(List<MatOfPoint> inputContours, double minArea,
                                double minPerimeter, double minWidth, double maxWidth, double minHeight, double
                                        maxHeight, double[] solidity, double maxVertexCount, double minVertexCount, double
                                        minRatio, double maxRatio, List<MatOfPoint> output) {
        final MatOfInt hull = new MatOfInt();
        output.clear();
        //operation
        for (int i = 0; i < inputContours.size(); i++) {
            final MatOfPoint contour = inputContours.get(i);
            final Rect bb = Imgproc.boundingRect(contour);
            if (bb.width < minWidth || bb.width > maxWidth) continue;
            if (bb.height < minHeight || bb.height > maxHeight) continue;
            final double area = Imgproc.contourArea(contour);
            if (area < minArea) continue;
            if (Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true) < minPerimeter) continue;
            Imgproc.convexHull(contour, hull);
            MatOfPoint mopHull = new MatOfPoint();
            mopHull.create((int) hull.size().height, 1, CvType.CV_32SC2);
            for (int j = 0; j < hull.size().height; j++) {
                int index = (int)hull.get(j, 0)[0];
                double[] point = new double[] { contour.get(index, 0)[0], contour.get(index, 0)[1]};
                mopHull.put(j, 0, point);
            }
            final double solid = 100 * area / Imgproc.contourArea(mopHull);
            if (solid < solidity[0] || solid > solidity[1]) continue;
            if (contour.rows() < minVertexCount || contour.rows() > maxVertexCount)	continue;
            final double ratio = bb.width / (double)bb.height;
            if (ratio < minRatio || ratio > maxRatio) continue;
            output.add(contour);
        }
    }

    private ArrayList<Contour> identifyContours(List<MatOfPoint> inputContours){
        RotatedRect temp;
        ArrayList<Contour> goodContours = new ArrayList<>();
        for (MatOfPoint m : inputContours) {
            Contour c = new Contour(m);
            if (c.getArea() > 250) {
                boolean tall;
                temp = c.rotatedRect;
                double ratio = temp.size.height / temp.size.width;
                if(ratio < 1) {
                    ratio = 1 / ratio;
                    tall = false;
                } else tall = true;
                if(ratio > 1.5){
                    if(tall && Math.abs(Math.abs(temp.angle) - 14.5) < 22.5){
                        goodContours.add(c);
                    } else if(!tall && Math.abs(Math.abs(temp.angle) - 75.5) < 22.5){
                        goodContours.add(c);
                    }
                }
            }
        }

        goodContours.sort((o1, o2) -> (int)(o2.getArea() - o1.getArea()));
        ArrayList<Contour> finalContours = new ArrayList<>();

        if(goodContours.size() > 2){
            // Filter for a pair of targets with angles of rotation roughly matching the goal
            RotatedRect rr1 = goodContours.get(0).rotatedRect;
            double targetAngle = rr1.size.height > rr1.size.width ? 75.5 : 14.5;
            RotatedRect current;
            int out = 0;
            for(int i = 1; i < goodContours.size(); i++){
                current = goodContours.get(i).rotatedRect;
                if(Math.abs(Math.abs(current.angle) - targetAngle) < 22.5){
                    if(rr1.size.height > rr1.size.width && current.center.x < rr1.center.x){
                        out = i;
                        //System.out.println("Found tall");
                        break;
                    } else if(rr1.size.height < rr1.size.width && current.center.x > rr1.center.x){
                        out = i;
                        //System.out.println("Found short");
                        break;
                    }
                }
            }
            finalContours.add(goodContours.get(0));
            finalContours.add(goodContours.get(out));
        } else if(goodContours.size() == 2){
            // If there is only two targets, assume they're the correct two targets. jk dont
            RotatedRect rr1 = goodContours.get(0).rotatedRect;
            RotatedRect current = goodContours.get(1).rotatedRect;

            if(rr1.size.height > rr1.size.width && current.center.x < rr1.center.x){
                finalContours = goodContours;
            } else if(rr1.size.height < rr1.size.width && current.center.x > rr1.center.x){
                finalContours = goodContours;
            }
        } else {
            //Log.e("Less than 2 potential vision targets seen");
        }

        if(finalContours.size() == 2){
            return finalContours;
        }
        return null;
    }

    private double getCenterOffset(ArrayList<Contour> contours, int center){
        if(contours.size() == 2){
            Contour c1 = contours.get(0);
            Contour c2 = contours.get(1);

            if (c1.coords.x > c2.coords.x) {
                c1 = contours.get(1);
                c2 = contours.get(0);
            }

            Rectangle r1 = c1.getBoundingBox();
            Rectangle r2 = c2.getBoundingBox();
            RotatedRectPoints rr1 = new RotatedRectPoints(c1.rotatedRect);
            RotatedRectPoints rr2 = new RotatedRectPoints(c2.rotatedRect);

            double avg = (rr1.inst.center.x + rr2.inst.center.x)/2;
            return center - avg;
        }
        return 0;
    }
}