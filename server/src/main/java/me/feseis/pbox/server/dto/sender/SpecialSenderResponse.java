package me.feseis.pbox.server.dto.sender;

/**
 * 特殊发送方出参。apiCode 是投递凭据，仅管理端展示。
 */
public record SpecialSenderResponse(
        String phone,
        String senderName,
        String apiCode
) {

    @Override
    public String toString() {
        return "SpecialSenderResponse[phone=" + phone + ", senderName=" + senderName + ", apiCode=***]";
    }
}
