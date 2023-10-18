package bakas.it.objectdetection.classifier;
/**
 *      ArtificialIntelligenceFrameworktoProtectChildrenfromHarmfulDigitalContent
 *      Copyright (C) 2021 BAKAS BİLİŞİM ELEKTRONİK YAZILIM DANIŞMANLIK SANAYİ VE TİCARET LİMİTED ŞİRKETİ
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.

 *     This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.

 *      You should have received a copy of the GNU General Public License
 *      along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import bakas.it.objectdetection.MyApplication;

public class Classifier {
    protected Interpreter tflite;
    private MappedByteBuffer tfliteModel;
    private TensorImage inputImageBuffer;
    private  int imageSizeX;
    private  int imageSizeY;
    public  TensorBuffer outputProbabilityBuffer;
    public  TensorProcessor probabilityProcessor;
    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD = 255.0f;
    private static final float PROBABILITY_MEAN = 0.0f;
    private static final float PROBABILITY_STD = 1.0f;
    private List<String> labels;


    public Classifier(){
    }

    public void initialize(){
        try{
            inittflite();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void inittflite() throws IOException {
        // Creating the model using tflite model
        AssetFileDescriptor fileDescriptor= MyApplication.getInstance().getAssets().openFd("modelv4.0.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startoffset = fileDescriptor.getStartOffset();
        long declaredLength=fileDescriptor.getDeclaredLength();
        tflite =new Interpreter(fileChannel.map(FileChannel.MapMode.READ_ONLY,startoffset,declaredLength));
    }

    public void classify(Bitmap bitmap){
        int imageTensorIndex = 0;
        //int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // Reading the shape from model input layer
        //imageSizeY = imageShape[1]; // Not neccessary for now since i have hard coded the picture size for now
        //imageSizeX = imageShape[2]; // Not neccessary for now since i have hard coded the picture size for now
        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();

        int probabilityTensorIndex = 0;
        int[] probabilityShape =
                tflite.getOutputTensor(probabilityTensorIndex).shape(); // Reading the shape of output from model output layer {1, NUM_CLASSES}
        DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

        inputImageBuffer = new TensorImage(imageDataType); // Initializing a TensorImage
        outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType); // Initializing a TensorBuffer
        probabilityProcessor = new TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build();


        inputImageBuffer = loadImage(bitmap); // Convert bitmap image to TensorImage


        tflite.run(inputImageBuffer.getBuffer(),outputProbabilityBuffer.getBuffer().rewind()); // Run the model to predict picture
    }

    private TensorImage loadImage(Bitmap bitmap) {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap); // loading picture "bitmap" into TensorImage "inputImageBuffer"

        // Creates processor for the TensorImage.
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight()); // Not necessary for now because i have disabled cropping.
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder() // Initializing an Image Process pipeline
                        //.add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(320, 320, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR)) // Resize picture
                        .add(getPreprocessNormalizeOp()) // Normalize pixel values
                        .build();
        return imageProcessor.process(inputImageBuffer); // Processing the image using the pipeline
    }

    // Some functions for normalizing image pixel values. Other methods can be added.
    private TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }
    private TensorOperator getPostprocessNormalizeOp(){
        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
    }

    public String predict(){
        String max_label = "No Results"; // Initializing a prediction label.

        try{
            labels = FileUtil.loadLabels( MyApplication.getInstance(),"newdict.txt"); // Read labels from newdict.txt inside assets folder.
        }catch (Exception e){
            e.printStackTrace();
        }

        // We take labels and probabilities from TensorLabel object and assign this to a Map<String, Float>
        Map<String, Float> labeledProbability =
                new TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer))
                        .getMapWithFloatValue();

        // Assign the max probability to maxValueInMap
        float maxValueInMap =(Collections.max(labeledProbability.values()));

        // Loop through the map of labels&probabilities, get max probability label and assign it to max_label
//        for (Map.Entry<String, Float> entry : labeledProbability.entrySet()) {
//            if (entry.getValue()==maxValueInMap) {
//                max_label = entry.getKey();
//            }
//        }

        if(labeledProbability.get("BAKAS BILISIM framework classification result: Violence Content") >= 0.8){
            return "BAKAS BILISIM framework classification result: Violence Content";
        }else if (labeledProbability.get("BAKAS BILISIM framework classification result: Normal Content") >= 0.7){
            return "BAKAS BILISIM framework classification result: Normal Content";
        }else {
            return "BAKAS BILISIM framework classification result: Suspicious Content";
        }

        //return max_label; // Return max_label
    }
}
