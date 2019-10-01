package org.javacv.face.recognition;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.javacv.common.ImageUtility;

import java.util.function.Function;

/**
 * Predict the gender of a face image.
 */
public class GenderPredictor implements Function<Mat, String> {

    private final ImageUtility utility;
    private final Recognitionable recognizer;

    public GenderPredictor(String trainingPath) {
        utility = ImageUtility.Instance;

        recognizer = new Recognizer(RecognizerType.Fisher);
        recognizer.train(new GenderTrainer(trainingPath));
    }

    @Override
    public String apply(Mat mat) {
        Mat face = utility.toGray(mat);
        switch (recognizer.predict(face)) {
            case 0: return "female";
            case 1: return "male";
            default: return "";
        }
    }
}