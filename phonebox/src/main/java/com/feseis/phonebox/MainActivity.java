package com.feseis.phonebox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.feseis.phonebox.adapter.MessageAdapter;
import com.feseis.phonebox.model.SmsMessage;
import com.feseis.phonebox.net.ApiException;
import com.feseis.phonebox.net.PboxApi;
import com.feseis.phonebox.prefs.AppPrefs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class MainActivity extends AppCompatActivity implements MessageAdapter.Listener {

    private static final int POLL_INTERVAL_MS = 1000;
    private static final int PAGE_SIZE = 50;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Runnable pollTask = this::pollTick;

    private PboxApi api;
    private String phone;
    private MessageAdapter adapter;
    private TextView tvRefreshStatus;
    private TextView tvUnread;
    private TextView tvEmpty;
    private EditText etReceiver;
    private EditText etContent;
    private MaterialButton btnSend;
    private View layoutRecord;
    private View layoutSend;
    private MaterialButtonToggleGroup toggleTabs;

    private boolean started;
    private boolean recordTabSelected = true;
    private boolean pollInFlight;
    private volatile boolean loadedOnce;
    private volatile long lastId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppPrefs prefs = new AppPrefs(this);
        phone = prefs.getPhone();
        String baseUrl = prefs.getBaseUrl();
        if (phone.isEmpty() || baseUrl.isEmpty()) {
            startActivity(new Intent(this, SetupActivity.class));
            finish();
            return;
        }
        api = new PboxApi(baseUrl);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvPhone = findViewById(R.id.tv_phone);
        TextView tvServer = findViewById(R.id.tv_server);
        tvRefreshStatus = findViewById(R.id.tv_refresh_status);
        tvUnread = findViewById(R.id.tv_unread);
        tvEmpty = findViewById(R.id.tv_empty);
        etReceiver = findViewById(R.id.et_receiver);
        etContent = findViewById(R.id.et_content);
        btnSend = findViewById(R.id.btn_send);
        layoutRecord = findViewById(R.id.layout_record);
        layoutSend = findViewById(R.id.layout_send);
        toggleTabs = findViewById(R.id.toggle_tabs);

        tvPhone.setText(getString(R.string.local_phone_prefix) + phone);
        tvServer.setText(baseUrl);

        RecyclerView recyclerView = findViewById(R.id.rv_messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.tv_switch).setOnClickListener(view -> {
            startActivity(new Intent(this, SetupActivity.class));
            finish();
        });
        findViewById(R.id.tv_mark_all_read).setOnClickListener(view -> markAllRead());
        btnSend.setOnClickListener(view -> sendMessage());

        toggleTabs.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            recordTabSelected = checkedId == R.id.btn_tab_record;
            layoutRecord.setVisibility(recordTabSelected ? View.VISIBLE : View.GONE);
            layoutSend.setVisibility(recordTabSelected ? View.GONE : View.VISIBLE);
            updatePolling();
        });
        toggleTabs.check(R.id.btn_tab_record);

        renderState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (api == null) {
            return;
        }
        started = true;
        updatePolling();
    }

    @Override
    protected void onStop() {
        super.onStop();
        started = false;
        handler.removeCallbacks(pollTask);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pollTask);
        executor.shutdownNow();
    }

    @Override
    public void onMarkRead(SmsMessage message) {
        if (message.receiverRead) {
            return;
        }
        executor.execute(() -> {
            try {
                api.markRead(phone, message.id);
                runOnUi(() -> {
                    adapter.markReadThrough(message.id);
                    renderState();
                });
            } catch (ApiException exception) {
                runOnUi(() -> showToast(exception.getMessage()));
            }
        });
    }

    @Override
    public void onDelete(SmsMessage message) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.delete_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> deleteMessage(message.id))
                .show();
    }

    private void deleteMessage(long id) {
        executor.execute(() -> {
            try {
                api.delete(id, phone);
                runOnUi(() -> {
                    adapter.removeById(id);
                    renderState();
                });
            } catch (ApiException exception) {
                runOnUi(() -> showToast(exception.getMessage()));
            }
        });
    }

    private void markAllRead() {
        long maxId = adapter.maxId();
        if (maxId <= 0) {
            return;
        }
        executor.execute(() -> {
            try {
                api.markRead(phone, maxId);
                runOnUi(() -> {
                    adapter.markReadThrough(maxId);
                    renderState();
                });
            } catch (ApiException exception) {
                runOnUi(() -> showToast(exception.getMessage()));
            }
        });
    }

    private void sendMessage() {
        String receiver = etReceiver.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        etReceiver.setError(null);
        etContent.setError(null);

        if (!PHONE_PATTERN.matcher(receiver).matches()) {
            etReceiver.setError(getString(R.string.error_phone));
            return;
        }
        if (content.isEmpty()) {
            etContent.setError(getString(R.string.error_content));
            return;
        }

        btnSend.setEnabled(false);
        executor.execute(() -> {
            try {
                api.send(phone, receiver, content);
                runOnUi(() -> {
                    etContent.setText("");
                    btnSend.setEnabled(true);
                    showToast(getString(R.string.sent));
                    updatePolling();
                });
            } catch (ApiException exception) {
                runOnUi(() -> {
                    btnSend.setEnabled(true);
                    showToast(exception.getMessage());
                });
            }
        });
    }

    private void updatePolling() {
        handler.removeCallbacks(pollTask);
        if (started && recordTabSelected) {
            handler.post(pollTask);
        }
    }

    private void scheduleNextPoll() {
        handler.removeCallbacks(pollTask);
        if (started && recordTabSelected) {
            handler.postDelayed(pollTask, POLL_INTERVAL_MS);
        }
    }

    private void pollTick() {
        if (!started || !recordTabSelected) {
            return;
        }
        if (pollInFlight) {
            handler.postDelayed(pollTask, POLL_INTERVAL_MS);
            return;
        }

        pollInFlight = true;
        final boolean initialLoad = !loadedOnce;
        executor.execute(() -> {
            try {
                long cursor = initialLoad ? 0 : lastId;
                List<SmsMessage> fetched = new ArrayList<>();
                while (true) {
                    PboxApi.MessagePage page = api.messages(phone, cursor, PAGE_SIZE);
                    fetched.addAll(page.messages);
                    cursor = page.lastId;
                    if (page.messages.size() < PAGE_SIZE) {
                        break;
                    }
                }
                final long newLastId = cursor;

                runOnUi(() -> {
                    pollInFlight = false;
                    if (initialLoad) {
                        adapter.replaceAll(fetched);
                        loadedOnce = true;
                    } else if (!fetched.isEmpty()) {
                        adapter.addNew(fetched);
                    }
                    lastId = newLastId;
                    tvRefreshStatus.setTextColor(ContextCompat.getColor(this, R.color.pbox_muted));
                    tvRefreshStatus.setText(R.string.refresh_ok);
                    renderState();
                    scheduleNextPoll();
                });
            } catch (ApiException exception) {
                runOnUi(() -> {
                    pollInFlight = false;
                    tvRefreshStatus.setTextColor(ContextCompat.getColor(this, R.color.pbox_error));
                    tvRefreshStatus.setText(getString(R.string.refresh_failed_prefix) + exception.getMessage());
                    scheduleNextPoll();
                });
            }
        });
    }

    private void renderState() {
        tvUnread.setText(getString(R.string.unread_prefix) + adapter.unreadCount());
        tvEmpty.setVisibility(adapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void runOnUi(Runnable action) {
        runOnUiThread(() -> {
            if (!isFinishing() && !isDestroyed()) {
                action.run();
            }
        });
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
