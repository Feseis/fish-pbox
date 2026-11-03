package com.feseis.phonebox;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.feseis.phonebox.net.ApiException;
import com.feseis.phonebox.net.PboxApi;
import com.feseis.phonebox.prefs.AppPrefs;
import com.google.android.material.button.MaterialButton;

public class SetupActivity extends AppCompatActivity {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private AppPrefs prefs;
    private EditText etServer;
    private EditText etPhone;
    private TextView tvStatus;
    private MaterialButton btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.setup_root), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = new AppPrefs(this);
        etServer = findViewById(R.id.et_server);
        etPhone = findViewById(R.id.et_phone);
        tvStatus = findViewById(R.id.tv_status);
        btnConnect = findViewById(R.id.btn_connect);

        String savedBaseUrl = prefs.getBaseUrl();
        String savedPhone = prefs.getPhone();
        if (!savedBaseUrl.isEmpty()) {
            etServer.setText(savedBaseUrl);
        }
        if (!savedPhone.isEmpty()) {
            etPhone.setText(savedPhone);
        }

        btnConnect.setOnClickListener(view -> connect());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private void connect() {
        String baseUrl = normalizeServer(etServer.getText().toString());
        String phone = etPhone.getText().toString().trim();

        tvStatus.setText("");
        etPhone.setError(null);
        if (baseUrl == null) {
            tvStatus.setText(R.string.error_server);
            return;
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            etPhone.setError(getString(R.string.error_phone));
            return;
        }

        setBusy(true);
        executor.execute(() -> {
            try {
                PboxApi api = new PboxApi(baseUrl);
                api.health();
                List<String> phones = api.listPhones();
                if (!phones.contains(phone)) {
                    throw new ApiException(getString(R.string.error_unregistered));
                }
                prefs.save(baseUrl, phone);
                runOnUi(() -> {
                    startActivity(new Intent(SetupActivity.this, MainActivity.class));
                    finish();
                });
            } catch (ApiException exception) {
                runOnUi(() -> {
                    tvStatus.setText(exception.getMessage());
                    setBusy(false);
                });
            }
        });
    }

    private void setBusy(boolean busy) {
        btnConnect.setEnabled(!busy);
        btnConnect.setText(busy ? R.string.action_connecting : R.string.action_connect);
    }

    private void runOnUi(Runnable action) {
        runOnUiThread(() -> {
            if (!isFinishing() && !isDestroyed()) {
                action.run();
            }
        });
    }

    private static String normalizeServer(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            value = "http://" + value;
        }
        Uri uri = Uri.parse(value);
        String host = uri.getHost();
        if (host == null || host.isEmpty()) {
            return null;
        }
        int port = uri.getPort();
        if (port == -1) {
            port = 8080;
        }
        String scheme = "https".equalsIgnoreCase(uri.getScheme()) ? "https" : "http";
        return scheme + "://" + host + ":" + port;
    }
}
