package bakas.it.objectdetection.FaceDetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;


import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import bakas.it.objectdetection.MainActivity;
import bakas.it.objectdetection.ObjectDetection.Result;

public class FaceDetection {

    private Module mModule = null;
    public FaceDetection(Context context){
        try {
            if (mModule == null) {
                mModule = LiteModuleLoader.load(MainActivity.assetFilePath(context, "face3.ptl"));
            }
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
        }
    }

    @WorkerThread
    @Nullable
    public ArrayList<Result> analyzeImage(Bitmap bitmap, Context context, boolean returnxyxyx) {
        Log.e("Module:", mModule.toString());

        try {

            Matrix matrix = new Matrix();
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            resizedBitmap = Bitmap.createScaledBitmap(resizedBitmap, FaceImageProcessor.mInputWidth, FaceImageProcessor.mInputHeight, true);

            final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
            IValue ivalue = IValue.from(inputTensor);
            if(ivalue != null){
                IValue ivalue_2 = mModule.forward(ivalue);
                if (ivalue_2 != null){
                    IValue[] outputTuple = ivalue_2.toTuple();
                    Log.e("tuple", Arrays.toString(outputTuple));
                    Tensor outputTensor = outputTuple[0].toTensor();
                    Tensor outputTensor1 = outputTuple[1].toTensor();
                    Tensor outputTensor2 = outputTuple[2].toTensor();
                    final float[] scores = outputTensor.getDataAsFloatArray();
                    Log.e("sc", Arrays.toString(scores));
                    Log.e("sc", Arrays.toString(outputTensor1.getDataAsFloatArray()));
                    Log.e("sc", Arrays.toString(outputTensor2.getDataAsFloatArray()));

                    float imgScaleX = (float)bitmap.getWidth() / FaceImageProcessor.mInputWidth;
                    float imgScaleY = (float)bitmap.getHeight() / FaceImageProcessor.mInputHeight;
                    if(returnxyxyx){
                        final ArrayList<Result> results = FaceImageProcessor.outputsToNMSPredictions2(scores, imgScaleX, imgScaleY, 0, 0);
                        return results;
                    }
                    else{
                        final ArrayList<Result> results = FaceImageProcessor.outputsToNMSPredictions(scores, imgScaleX, imgScaleY, 0, 0);
                        return results;
                    }

                }

                else{
                    return  new ArrayList<>();
                }

            }
            else{
                return  new ArrayList<>();
            }

        }

        catch (Exception e) {
            return  new ArrayList<>();
        }



    }
}
