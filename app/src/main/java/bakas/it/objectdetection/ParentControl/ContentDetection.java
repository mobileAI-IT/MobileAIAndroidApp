package bakas.it.objectdetection.ParentControl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import bakas.it.objectdetection.MainActivity;
import bakas.it.objectdetection.ObjectDetection.Result;

public class ContentDetection {

    private Module mModule = null;

    public ContentDetection(Context context, String modelName){
        try {
            if (mModule == null) {
                mModule = LiteModuleLoader.load(MainActivity.assetFilePath(context, modelName));
            }
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
        }
    }

    @WorkerThread
    @Nullable
    public ArrayList<Result> analyzeImage(Bitmap bitmap) {

        Log.e("Module:", mModule.toString());
        try {

            Matrix matrix = new Matrix();
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            resizedBitmap = Bitmap.createScaledBitmap(resizedBitmap, bitmap.getWidth(), bitmap.getHeight(), true);

            final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
            IValue ivalue = IValue.from(inputTensor);
            if(ivalue != null){
                IValue ivalue_2 = mModule.forward(ivalue);
                if (ivalue_2 != null){
                    IValue[] outputTuple = ivalue_2.toTuple();
                    Log.e("tuple", Arrays.toString(outputTuple));
                    Tensor outputTensor = outputTuple[0].toTensor();
                    final float[] scores = outputTensor.getDataAsFloatArray();
                    Log.e("sc", Arrays.toString(scores));

                    float imgScaleX = (float)bitmap.getWidth() / bitmap.getWidth();
                    float imgScaleY = (float)bitmap.getHeight() / bitmap.getHeight();
                    final ArrayList<Result> results = ContentImageProcessor.outputsToNMSPredictions(scores, imgScaleX, imgScaleY, 0, 0);
                    return results;


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
            e.printStackTrace();
            Log.e("Content Detection", "Error predicting", e);
            return  new ArrayList<>();
        }



    }
}
