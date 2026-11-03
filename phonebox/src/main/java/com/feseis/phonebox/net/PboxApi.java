package com.feseis.phonebox.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.feseis.phonebox.model.SmsMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class PboxApi {

    private static final int TIMEOUT_MS = 5000;

    private final String baseUrl;

    public PboxApi(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void health() throws ApiException {
        request("GET", "/sms/health", null);
    }

    public List<String> listPhones() throws ApiException {
        String response = request("GET", "/sms/phones", null);
        try {
            JSONArray array = new JSONArray(response);
            List<String> phones = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                phones.add(array.getString(i));
            }
            return phones;
        } catch (JSONException exception) {
            throw new ApiException("服务器返回格式异常");
        }
    }

    public SmsMessage send(String senderPhone, String receiverPhone, String content) throws ApiException {
        JSONObject body = new JSONObject();
        try {
            body.put("senderPhone", senderPhone);
            body.put("receiverPhone", receiverPhone);
            body.put("content", content);
        } catch (JSONException exception) {
            throw new ApiException("请求数据异常");
        }
        return SmsMessage.fromJson(parseObject(request("POST", "/sms/send", body)));
    }

    public MessagePage messages(String receiverPhone, long afterId, int limit) throws ApiException {
        String path = "/sms/messages?receiverPhone=" + encode(receiverPhone)
                + "&afterId=" + afterId
                + "&limit=" + limit;
        JSONObject body = parseObject(request("GET", path, null));
        List<SmsMessage> messages = new ArrayList<>();
        JSONArray array = body.optJSONArray("messages");
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item != null) {
                    messages.add(SmsMessage.fromJson(item));
                }
            }
        }
        return new MessagePage(messages, body.optLong("lastId", afterId));
    }

    public int markRead(String receiverPhone, long lastId) throws ApiException {
        JSONObject body = new JSONObject();
        try {
            body.put("receiverPhone", receiverPhone);
            body.put("lastId", lastId);
        } catch (JSONException exception) {
            throw new ApiException("请求数据异常");
        }
        return parseObject(request("POST", "/sms/messages/read", body)).optInt("updated", 0);
    }

    public void delete(long id, String receiverPhone) throws ApiException {
        request("DELETE", "/sms/messages/" + id + "?receiverPhone=" + encode(receiverPhone), null);
    }

    private JSONObject parseObject(String response) throws ApiException {
        try {
            return new JSONObject(response);
        } catch (JSONException exception) {
            throw new ApiException("服务器返回格式异常");
        }
    }

    private String request(String method, String path, JSONObject body) throws ApiException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");

            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
            }

            int statusCode = connection.getResponseCode();
            String responseText = read(statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream());

            if (statusCode < 200 || statusCode >= 300) {
                throw new ApiException(parseError(responseText, statusCode));
            }
            return responseText;
        } catch (IOException exception) {
            throw new ApiException("无法连接服务器：" + exception.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String parseError(String responseText, int statusCode) {
        if (responseText != null && !responseText.isEmpty()) {
            try {
                String detail = new JSONObject(responseText).optString("detail", "");
                if (!detail.isEmpty()) {
                    return detail;
                }
            } catch (JSONException ignored) {
                // 非 JSON 错误体，使用状态码兜底
            }
        }
        return "请求失败（HTTP " + statusCode + "）";
    }

    private static String read(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException exception) {
            return value;
        }
    }

    public static final class MessagePage {

        public final List<SmsMessage> messages;
        public final long lastId;

        public MessagePage(List<SmsMessage> messages, long lastId) {
            this.messages = messages;
            this.lastId = lastId;
        }
    }
}
