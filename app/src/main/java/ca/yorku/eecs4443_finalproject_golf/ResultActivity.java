package ca.yorku.eecs4443_finalproject_golf;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.Arrays;
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class ResultActivity extends AppCompatActivity {

    Button btnClear;
    TextView txtTitle;
    ListView lsResults;
    ImageView imgQRCode;
    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        txtTitle = (TextView) findViewById(R.id.lbl_results_title);
        lsResults = (ListView) findViewById(R.id.list_results);
        imgQRCode = (ImageView) findViewById(R.id.qr_code);
        btnClear = (Button) findViewById(R.id.btn_clear);

        displayResults(this, lsResults);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ResultActivity.this);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPref.clearCSVData(ResultActivity.this);
                        displayResults(ResultActivity.this, lsResults);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {} //Ignore
                });
                AlertDialog dialog = builder.create();
                dialog.setTitle("Clear CSV data");
                dialog.setMessage("Are you sure?");
                dialog.show();
            }
        });
    }


    public void displayResults(Context context, ListView lsResults) {
        String results = SharedPref.getCSVData(this);
        String[] list_result = Arrays.stream(results.split("\n"))
                .map(line -> line.replace(",", "\t\t"))
                .toArray(String[]::new);
        lsResults.setAdapter(
                new ArrayAdapter<String>(
                        this,
                        R.layout.listview_item,
                        list_result
                ));

        /**
         * Library Used: https://github.com/androidmads/QRGenerator
         */
        // Initializing the QR Encoder with your value to be encoded, type your required and Dimension
        QRGEncoder qrgEncoder = new QRGEncoder(results, null, QRGContents.Type.TEXT, 500);
        qrgEncoder.setColorBlack(Color.WHITE);
        qrgEncoder.setColorWhite(Color.BLACK);
        try {
            // Getting QR-Code as Bitmap
            bitmap = qrgEncoder.getBitmap();
            // Setting Bitmap to ImageView
            imgQRCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}