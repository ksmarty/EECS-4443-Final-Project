package digitalink;

import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.mlkit.common.MlKitException;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.vision.digitalink.DigitalInkRecognition;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions;
import com.google.mlkit.vision.digitalink.Ink;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;

import static android.content.ContentValues.TAG;

/**
 * Code from Ibrahim Canerdogan
 * https://github.com/icanerdogan/Google-MLKit-Android-Apps/tree/master/Archive%20-%20Java/DigitalInkRecognition
 * Modified to fit project requirements
 */
public class StrokeManager {
    private static DigitalInkRecognitionModel model;
    private static Ink.Builder inkBuilder = Ink.builder();
    private static Ink.Stroke.Builder strokeBuilder;

    public static void addNewTouchEvent(@NonNull MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        long t = System.currentTimeMillis();

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN -> {
                strokeBuilder = Ink.Stroke.builder();
                strokeBuilder.addPoint(Ink.Point.create(x, y, t));
            }
            case MotionEvent.ACTION_MOVE -> strokeBuilder.addPoint(Ink.Point.create(x, y, t));
            case MotionEvent.ACTION_UP -> {
                strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                inkBuilder.addStroke(strokeBuilder.build());
                strokeBuilder = null;
            }
        }
    }

    public static void setModel(LANG lang) {
        DigitalInkRecognitionModelIdentifier modelIdentifier = null;

        String languageTag = getLanguage(lang);

        try {
            modelIdentifier =
                    DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag);
        } catch (MlKitException e) {
            Log.i(TAG, "Exception" + e);
        }

        assert modelIdentifier != null;
        model = DigitalInkRecognitionModel.builder(modelIdentifier).build();
    }

    @Contract(pure = true)
    public static String getLanguage(@NonNull LANG lang) {
        return switch (lang) {
            case ENGLISH -> "en";
            case GEORGIAN -> "ka";
            case GREEK -> "el";
            case ARMENIAN -> "hy";
            case UKRAINIAN -> "uk";
        };
    }

    public static void init() {
        Arrays.stream(LANG.values()).forEach(StrokeManager::download);
    }

    public static void download(LANG lang) {
        setModel(lang);

        RemoteModelManager remoteModelManager = RemoteModelManager.getInstance();

        remoteModelManager
                .download(model, new DownloadConditions.Builder().build())
                .addOnSuccessListener(aVoid -> Log.i(TAG, "Model downloaded"))
                .addOnFailureListener(
                        e -> Log.e(TAG, "Error while downloading a model: " + e));
    }

    public static void recognize(TextView textView) {
        DigitalInkRecognizer recognizer = DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(model).build());

        Ink ink = inkBuilder.build();

        recognizer.recognize(ink)
                .addOnSuccessListener(result -> textView.setText(result.getCandidates().get(0).getText()))
                .addOnFailureListener(e -> Log.e(TAG, "Error during recognition: " + e));
    }

    public static boolean hasStrokes() {
        return inkBuilder.build().getStrokes().size() > 0;
    }

    public static void clear() {
        inkBuilder = Ink.builder();
    }

    public enum LANG {
        ENGLISH,
        GEORGIAN,
        GREEK,
        ARMENIAN,
        UKRAINIAN

    }
}
