package bakas.it.objectdetection.ObjectDetection;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import bakas.it.objectdetection.MainActivity;

public class ObjectDetection {
    private Module mModule = null;

    @WorkerThread
    @Nullable
    public  ArrayList<Result> analyzeImage(Bitmap bitmap, Context context) {
        try {
            if (mModule == null) {
                mModule = LiteModuleLoader.load(MainActivity.assetFilePath(context, "gunmain.ptl"));
            }
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
        }
        Log.e("Module:", mModule.toString());

        Matrix matrix = new Matrix();
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        resizedBitmap = Bitmap.createScaledBitmap(resizedBitmap, ImageProcessor.mInputWidth, ImageProcessor.mInputHeight, true);

        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);

        IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();
        final Tensor outputTensor = outputTuple[0].toTensor();
        final float[] outputs = outputTensor.getDataAsFloatArray();
        float imgScaleX = (float)bitmap.getWidth() / ImageProcessor.mInputWidth;
        float imgScaleY = (float)bitmap.getHeight() / ImageProcessor.mInputHeight;

        final ArrayList<Result> results = ImageProcessor.outputsToNMSPredictions(outputs, imgScaleX, imgScaleY, 0, 0);
        return results;
    }

}
