package ca.yorku.eecs4443_finalproject_golf;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // Event listener for "Get Started" button
        findViewById(R.id.getStartedButton).setOnClickListener(this::getStarted);

        // Show results activity
        Button btnShowResults = (Button) findViewById(R.id.btn_show_results);
        btnShowResults.setOnClickListener(v -> startActivity(new Intent(SetupActivity.this, ResultActivity.class)));
    }

    private void getStarted(View v) {
        EditText nameField = findViewById(R.id.nameField);
        String name = nameField.getText().toString();

        // Ensure name is valid
        if (name.length() < 3) {
            nameField.setError(getString(R.string.error_invalid_name));
            return;
        }

        Bundle b = new Bundle();
        b.putString(BundleKeys.NAME, name);

        // Start the test
        Intent i = new Intent(this, TestingActivity.class);
        i.putExtras(b);
        startActivity(i);
    }
}