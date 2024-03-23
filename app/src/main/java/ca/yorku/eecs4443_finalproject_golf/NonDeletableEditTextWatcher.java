package ca.yorku.eecs4443_finalproject_golf;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class  NonDeletableEditTextWatcher {
    private final EditText editText;
    private String previousText;
    private int previousSelection;

    boolean deletable;

    public NonDeletableEditTextWatcher(EditText editText) {
        this.editText = editText;
        this.previousText = editText.getText().toString();
        this.previousSelection = editText.getSelectionEnd();
        this.deletable = false;
        init();
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }
    public void clear(){
        setDeletable(true);
        this.editText.setText("");
        setDeletable(false);
    }
    private void init() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!deletable && s.length() < previousText.length()) {
                    editText.setText(previousText);
                    editText.setSelection(previousSelection);
                } else {
                    previousText = s.toString();
                    previousSelection = editText.getSelectionEnd();
                }
            }
        });
    }
}
