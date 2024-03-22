package ca.yorku.eecs4443_finalproject_golf;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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

        // Hide "Show Results" if there are none
        if (!SharedPref.hasSavedData(this))
            btnShowResults.setVisibility(View.GONE);

        setupAgeField();
    }

    private void setupAgeField() {
        EditText ageField = findViewById(R.id.ageField);
        ageField.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    private void getStarted(View v) {
        EditText nameField = findViewById(R.id.nameField);
        String name = nameField.getText().toString();

        EditText ageField = findViewById(R.id.ageField);
        String ageString = ageField.getText().toString();
        int age = Integer.parseInt(ageString.length() == 0 ? "0" : ageString);

        Spinner experienceSelect = findViewById(R.id.experienceSelect);
        String experience = experienceSelect.getSelectedItem().toString();

        Spinner genderSelect = findViewById(R.id.genderSelect);
        String gender = genderSelect.getSelectedItem().toString();

        boolean isValid = true;

        // Ensure name is valid
        if (name.length() < 3) {
            nameField.setError(getString(R.string.error_invalid_name));
            isValid = false;
        }

        // Ensure age is valid
        if (age < 18 || age > 120) {
            ageField.setError("Age must be in the range 18~120");
            isValid = false;
        }

        if (!isValid) return;

        Bundle b = new Bundle();
        b.putString(BundleKeys.NAME, name);
        b.putInt(BundleKeys.AGE, age);
        b.putString(BundleKeys.GENDER, gender);
        b.putString(BundleKeys.EXPERIENCE, experience);

        // Start the test
        Intent i = new Intent(this, TestingActivity.class);
        i.putExtras(b);
        startActivity(i);
    }
}