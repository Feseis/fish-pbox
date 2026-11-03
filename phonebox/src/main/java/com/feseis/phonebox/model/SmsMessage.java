package com.feseis.phonebox.model;

import org.json.JSONObject;

public class SmsMessage {

    public long id;
    public String senderPhone = "";
    public String senderName = "";
    public String receiverPhone = "";
    public String content = "";
    public String messageType = "USER";
    public boolean receiverRead;
    public String createdAt = "";

    public static SmsMessage fromJson(JSONObject json) {
        SmsMessage message = new SmsMessage();
        message.id = json.optLong("id");
        message.senderPhone = json.optString("senderPhone", "");
        message.senderName = json.isNull("senderName") ? "" : json.optString("senderName", "");
        message.receiverPhone = json.optString("receiverPhone", "");
        message.content = json.optString("content", "");
        message.messageType = json.optString("messageType", "USER");
        message.receiverRead = json.optBoolean("receiverRead", false);
        message.createdAt = json.isNull("createdAt") ? "" : json.optString("createdAt", "");
        return message;
    }

    public String displayTime() {
        if (createdAt.length() >= 19) {
            return createdAt.substring(5, 19).replace('T', ' ');
        }
        return createdAt;
    }
}
