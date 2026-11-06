package com.example.baggagefee;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final double ECONOMY_LIMIT_KG = 20.0;
    private static final double BUSINESS_LIMIT_KG = 40.0;
    private static final double SERVICE_TAX = 0.08; // 8% service tax

    private ScrollView scrollMain;
    private TextInputLayout layoutWeight, layoutRate;
    private TextInputEditText etWeight, etRate;
    private MaterialButtonToggleGroup toggleClass;
    private MaterialCardView resultCard;
    private TextView tvNote, tvAllowance, tvExcessWeight, tvCharge, tvTax, tvTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollMain = findViewById(R.id.scrollMain);
        layoutWeight = findViewById(R.id.layoutWeight);
        layoutRate = findViewById(R.id.layoutRate);
        etWeight = findViewById(R.id.etWeight);
        etRate = findViewById(R.id.etRate);
        toggleClass = findViewById(R.id.toggleClass);
        resultCard = findViewById(R.id.resultCard);
        tvNote = findViewById(R.id.tvNote);
        tvAllowance = findViewById(R.id.tvAllowance);
        tvExcessWeight = findViewById(R.id.tvExcessWeight);
        tvCharge = findViewById(R.id.tvCharge);
        tvTax = findViewById(R.id.tvTax);
        tvTotal = findViewById(R.id.tvTotal);

        Button btnCalculate = findViewById(R.id.btnCalculate);
        Button btnReset = findViewById(R.id.btnReset);

        btnCalculate.setOnClickListener(v -> calculate());
        btnReset.setOnClickListener(v -> reset());
    }

    private void calculate() {
        layoutWeight.setError(null);
        layoutRate.setError(null);

        String weightText = getText(etWeight);
        String rateText = getText(etRate);

        double weight = 0, rate = 0;
        boolean valid = true;

        // ---- Validate weight ----
        if (weightText.isEmpty()) {
            layoutWeight.setError(getString(R.string.error_weight_empty));
            valid = false;
        } else {
            try {
                weight = Double.parseDouble(weightText);
                if (weight < 0) {
                    layoutWeight.setError(getString(R.string.error_negative));
                    valid = false;
                }
            } catch (NumberFormatException e) {
                layoutWeight.setError(getString(R.string.error_invalid_number));
                valid = false;
            }
        }

        // ---- Validate rate ----
        if (rateText.isEmpty()) {
            layoutRate.setError(getString(R.string.error_rate_empty));
            valid = false;
        } else {
            try {
                rate = Double.parseDouble(rateText);
                if (rate < 0) {
                    layoutRate.setError(getString(R.string.error_negative));
                    valid = false;
                }
            } catch (NumberFormatException e) {
                layoutRate.setError(getString(R.string.error_invalid_number));
                valid = false;
            }
        }

        if (!valid) {
            resultCard.setVisibility(View.GONE);
            return;
        }

        // ---- Calculate ----
        boolean isEconomy = toggleClass.getCheckedButtonId() == R.id.btnEconomy;
        double allowance = isEconomy ? ECONOMY_LIMIT_KG : BUSINESS_LIMIT_KG;

        double excessWeight = Math.max(0, weight - allowance); // never negative
        double charge = excessWeight * rate;
        double tax = SERVICE_TAX * charge;
        double total = charge + tax;

        // ---- Output ----
        tvAllowance.setText(String.format(Locale.US, getString(R.string.val_kg), allowance));
        tvExcessWeight.setText(String.format(Locale.US, getString(R.string.val_kg), excessWeight));
        tvCharge.setText(String.format(Locale.US, getString(R.string.val_rm), charge));
        tvTax.setText(String.format(Locale.US, getString(R.string.val_rm), tax));
        tvTotal.setText(String.format(Locale.US, getString(R.string.val_rm), total));

        if (excessWeight == 0) {
            tvNote.setText(R.string.note_within);
            tvNote.setBackgroundResource(R.drawable.note_ok_bg);
            tvNote.setTextColor(getColor(R.color.success_text));
        } else {
            tvNote.setText(String.format(Locale.US, getString(R.string.note_over), excessWeight));
            tvNote.setBackgroundResource(R.drawable.note_warn_bg);
            tvNote.setTextColor(getColor(R.color.warn_text));
        }

        resultCard.setAlpha(0f);
        resultCard.setVisibility(View.VISIBLE);
        resultCard.animate().alpha(1f).setDuration(300).start();
        scrollMain.post(() -> scrollMain.fullScroll(View.FOCUS_DOWN));
        hideKeyboard();
    }

    private void reset() {
        etWeight.setText("");
        etRate.setText("");
        layoutWeight.setError(null);
        layoutRate.setError(null);
        toggleClass.check(R.id.btnEconomy);
        resultCard.setVisibility(View.GONE);
        scrollMain.post(() -> scrollMain.fullScroll(View.FOCUS_UP));
        etWeight.requestFocus();
    }

    private String getText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // ---------------- Toolbar menu (Share + About) ----------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            send.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text,
                    getString(R.string.app_name), getString(R.string.app_url)));
            startActivity(Intent.createChooser(send, getString(R.string.share_via)));
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
