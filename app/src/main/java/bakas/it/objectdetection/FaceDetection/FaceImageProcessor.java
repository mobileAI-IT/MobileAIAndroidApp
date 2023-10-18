package bakas.it.objectdetection.FaceDetection;

import android.graphics.Rect;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import android.graphics.Rect;

import bakas.it.objectdetection.ObjectDetection.Result;

public class FaceImageProcessor {

    static int mInputWidth = 640;
    static int mInputHeight = 640;
    private static float mThreshold = 0.35f;
    private static int mNmsLimit = 250;

    static ArrayList<Result> nonMaxSuppression(ArrayList<Result> boxes, int limit, float threshold) {

        Collections.sort(boxes,
                new Comparator<Result>() {
                    @Override
                    public int compare(Result o1, Result o2) {
                        return o1.score.compareTo(o2.score);
                    }
                });

        ArrayList<Result> selected = new ArrayList<>();
        boolean[] active = new boolean[boxes.size()];
        Arrays.fill(active, true);
        int numActive = active.length;

        boolean done = false;
        for (int i=0; i<boxes.size() && !done; i++) {
            if (active[i]) {
                Result boxA = boxes.get(i);
                selected.add(boxA);
                if (selected.size() >= limit) break;

                for (int j=i+1; j<boxes.size(); j++) {
                    if (active[j]) {
                        Result boxB = boxes.get(j);
                        if (IOU(boxA.rect, boxB.rect) > threshold) {
                            active[j] = false;
                            numActive -= 1;
                            if (numActive <= 0) {
                                done = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        Log.e("Selected,", selected.toString());
        return selected;
    }

    static float IOU(Rect a, Rect b) {
        float areaA = (a.right - a.left) * (a.bottom - a.top);
        if (areaA <= 0.0) return 0.0f;

        float areaB = (b.right - b.left) * (b.bottom - b.top);
        if (areaB <= 0.0) return 0.0f;

        float intersectionMinX = Math.max(a.left, b.left);
        float intersectionMinY = Math.max(a.top, b.top);
        float intersectionMaxX = Math.min(a.right, b.right);
        float intersectionMaxY = Math.min(a.bottom, b.bottom);
        float intersectionArea = Math.max(intersectionMaxY - intersectionMinY, 0) *
                Math.max(intersectionMaxX - intersectionMinX, 0);
        return intersectionArea / (areaA + areaB - intersectionArea);
    }

    static ArrayList<Result> outputsToNMSPredictions(float[] outputs, float imgScaleX, float imgScaleY, float startX, float startY) {
        ArrayList<Result> results = new ArrayList<>();
        int a = 0;
        for (int i = 0; i< outputs.length; i+=6) {
            if (outputs[i+4] > mThreshold) {
                /*float x = outputs[i];
                float y = outputs[i+1];
                float w = outputs[i+2];
                float h = outputs[i+3];
                float left = imgScaleX * (x - w/2);
                float top = imgScaleY * (y - h/2);
                float right = imgScaleX * (x + w/2);
                float bottom = imgScaleY * (y + h/2);*/

                float x = outputs[i];
                float y = outputs[i+1];
                float w = outputs[i+2];
                float h = outputs[i+3];
                float left = imgScaleX * x;
                float top = imgScaleY * y;
                float right = imgScaleX * w;
                float bottom = imgScaleY * h;

                Rect rect = new Rect((int)(startX+left), (int)(startY+top), (int)(startX+right), (int)(startY+bottom));
                Result result = new Result(0, outputs[i+4], rect);
                results.add(result);
                //Log.e("ObT", String.valueOf(outputs[i+4]));
            }
        }
        return nonMaxSuppression(results, mNmsLimit, mThreshold);
    }

    static ArrayList<Result> outputsToNMSPredictions2(float[] outputs, float imgScaleX, float imgScaleY, float startX, float startY) {
        ArrayList<Result> results = new ArrayList<>();
        int a = 0;
        for (int i = 0; i< outputs.length; i+=6) {
            if (outputs[i+4] > mThreshold) {
                /*float x = outputs[i];
                float y = outputs[i+1];
                float w = outputs[i+2];
                float h = outputs[i+3];
                float left = imgScaleX * (x - w/2);
                float top = imgScaleY * (y - h/2);
                float right = imgScaleX * (x + w/2);
                float bottom = imgScaleY * (y + h/2);*/

                float x = outputs[i];
                float y = outputs[i+1];
                float w = outputs[i+2];
                float h = outputs[i+3];
                float left = imgScaleX * x;
                float top = imgScaleY * y;
                float right = imgScaleX * w;
                float bottom = imgScaleY * h;

                Rect rect = new Rect((int)x, (int)y,(int) w,(int) h);
                Result result = new Result(0, outputs[i+4], rect);
                results.add(result);
                //Log.e("ObT", String.valueOf(outputs[i+4]));
            }
        }
        return nonMaxSuppression(results, mNmsLimit, mThreshold);
    }
}
