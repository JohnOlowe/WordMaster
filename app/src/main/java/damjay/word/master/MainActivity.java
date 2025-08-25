package damjay.word.master;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.flexbox.FlexboxLayout;
import damjay.word.master.databinding.ActivityMainBinding;

import java.util.HashSet;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private int buttonWidth;
    private int moreResultsSize;
    
    private HashSet<String> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate and get instance of binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // set content view to binding's root
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle(R.string.app_name);
        try {
            initializeElements();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void initializeElements() {
        binding.wordInput.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        checkCurrentText(s, count);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
        binding.moreResults.setOnClickListener(this::loadMoreOccurrences);
        binding.lessResults.setOnClickListener(this::loadLessOccurrences);
    }

    public void checkCurrentText(CharSequence s, int count) {
        String newText = stripExtra(s.toString());
        if (!s.toString().equals(newText)) {
            binding.wordInput.setText(newText);
            binding.wordInput.setSelection(newText.length());
            return;
        }
        int childCount = binding.charNum.getChildCount();
        if (buttonWidth <= 0 && childCount > 0) {
            buttonWidth = binding.charNum.getChildAt(0).getMeasuredWidth();
        }
        count = s.length();
        if (childCount < count - 2) {
            for (; childCount < count - 2; childCount++) {
                Button button = new Button(MainActivity.this);
                button.setLayoutParams(new LinearLayout.LayoutParams(buttonWidth > 0 ? buttonWidth : FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT));
                // button.setPadding(20, 20, 20, 20);
                button.setText("" + (3 + childCount));
                button.setOnClickListener(MainActivity.this::loadOccurrences);
                binding.charNum.addView(button);
            }
        } else {
            for (; childCount > 0 && childCount > count - 2; childCount--) {
                binding.charNum.removeViewAt(childCount - 1);
            }
        }
    }

    private String stripExtra(String line) {
        // Start with the beginning of the line
        int index = 0;
        while (index < line.length()) {
            if ((line.charAt(index) >= 'A' && line.charAt(index) <= 'Z')
                    || (line.charAt(index) >= 'a' && line.charAt(index) <= 'z')) break;
            else index++;
        }
        line = line.substring(index);

        // Now finish by strippig the end of the line
        index = line.length();

        while (index > 0) {
            if ((line.charAt(index - 1) >= 'A' && line.charAt(index - 1) <= 'Z')
                    || (line.charAt(index - 1) >= 'a' && line.charAt(index - 1) <= 'z')) break;
            else index--;
        }
        line = line.substring(0, index);

        // Now check the distinct character length
        HashSet<Character> distinctLetters = new HashSet<>();
        for (int i = 0; i < line.length(); i++) {
            distinctLetters.add(line.charAt(i));
            if (distinctLetters.size() > 10) {
                Toast.makeText(MainActivity.this, R.string.character_limit, Toast.LENGTH_SHORT)
                        .show();
                return line.substring(0, i).toLowerCase();
            }
        }

        return line.toLowerCase();
    }

    public void loadOccurrences(View view) {
        if (binding.wordResults.getChildCount() > 0) binding.wordResults.removeAllViews();
        int num = 0;
        try {
            Button button = (Button) view;
            num = Integer.parseInt(button.getText().toString());
        } catch (Exception e) {
            System.out.println(num);
            e.printStackTrace();
        }
        System.out.println("The number is " + num);
        if (num == 0) return;

        // Start the unscrambling operation
        Toast.makeText(this, "Starting", Toast.LENGTH_SHORT);
        System.out.println("Starting Operation");
        if (!WordUnscrambler.unzipData(getFilesDir(), getAssets(), "WordDB.zip")) {
            Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT);
            return;
        }
        Toast.makeText(this, "Done", Toast.LENGTH_SHORT);
        results = WordUnscrambler.getResults(
        binding.wordInput.getText().toString(), num, WordUnscrambler.SMALL_DB);

        if (results == null) {
            Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT);
            return;
        }
        Toast.makeText(this, "Results: " + results.size(), Toast.LENGTH_SHORT);
        for (String word : results) {
            Button button = new Button(this);
            button.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            FlexboxLayout.LayoutParams.WRAP_CONTENT,
                            FlexboxLayout.LayoutParams.WRAP_CONTENT));
            // button.setPadding(20, 20, 20, 20);
            button.setText(word);
            button.setOnClickListener(MainActivity.this::removeButton);
            binding.wordResults.addView(button);
        }
        moreResultsSize = 0;
        binding.moreResults.setVisibility(View.VISIBLE);
        binding.lessResults.setVisibility(View.GONE);
    }
    
    public void loadMoreOccurrences(View view) {
        HashSet<String> moreResults = WordUnscrambler.getResults(WordUnscrambler.text, WordUnscrambler.limit, WordUnscrambler.LARGE_DB);
        moreResultsSize = 0;
        
        for (String newResult : moreResults) {
            if (!results.contains(newResult)) {
                moreResultsSize++;
                Button button = new Button(this);
                button.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            FlexboxLayout.LayoutParams.WRAP_CONTENT,
                            FlexboxLayout.LayoutParams.WRAP_CONTENT));
                // button.setPadding(20, 20, 20, 20);
                button.setText(newResult);
                button.setOnClickListener(MainActivity.this::removeButton);
                binding.wordResults.addView(button);
            }
        }
        
        binding.moreResults.setVisibility(View.GONE);
        binding.lessResults.setVisibility(View.VISIBLE);
    }
    
    public void loadLessOccurrences(View view) {
        if (moreResultsSize > 0)
            binding.wordResults.removeViews(binding.wordResults.getChildCount() - moreResultsSize, moreResultsSize);
        binding.moreResults.setVisibility(View.VISIBLE);
        binding.lessResults.setVisibility(View.GONE);
    }

    public void removeButton(View view) {
        int viewIndex = binding.wordResults.indexOfChild(view);
        if (moreResultsSize > 0 && viewIndex >= binding.wordResults.getChildCount() - moreResultsSize)
            moreResultsSize--;
        binding.wordResults.removeViewAt(viewIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}
