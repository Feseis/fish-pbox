package me.feseis.pbox.server.mapper;

import java.util.List;

import me.feseis.pbox.server.entity.SmsMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SmsMessageMapper {

    @Insert("""
            INSERT INTO sms_message (sender_phone, receiver_phone, content, message_type)
            VALUES (#{senderPhone}, #{receiverPhone}, #{content}, #{messageType})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SmsMessage message);

    @Select("""
            SELECT m.id, m.sender_phone, m.receiver_phone, m.content, m.message_type,
                   m.read_at, m.created_at, s.sender_name
            FROM sms_message m
            LEFT JOIN special_sender s ON m.message_type = 'API' AND s.phone = m.sender_phone
            WHERE m.id = #{id}
            """)
    SmsMessage findById(@Param("id") long id);

    @Select("""
            SELECT m.id, m.sender_phone, m.receiver_phone, m.content, m.message_type,
                   m.read_at, m.created_at, s.sender_name
            FROM sms_message m
            LEFT JOIN special_sender s ON m.message_type = 'API' AND s.phone = m.sender_phone
            WHERE m.receiver_phone = #{receiverPhone}
              AND m.deleted_at IS NULL
              AND m.id > #{afterId}
            ORDER BY m.id
            LIMIT #{limit}
            """)
    List<SmsMessage> findAfter(@Param("receiverPhone") String receiverPhone,
                               @Param("afterId") long afterId,
                               @Param("limit") int limit);

    @Update("""
            UPDATE sms_message
            SET read_at = NOW(3)
            WHERE receiver_phone = #{receiverPhone}
              AND id <= #{lastId}
              AND read_at IS NULL
              AND deleted_at IS NULL
            """)
    int markRead(@Param("receiverPhone") String receiverPhone, @Param("lastId") long lastId);

    @Update("""
            UPDATE sms_message
            SET deleted_at = NOW(3)
            WHERE id = #{id}
              AND receiver_phone = #{receiverPhone}
              AND deleted_at IS NULL
            """)
    int softDelete(@Param("id") long id, @Param("receiverPhone") String receiverPhone);

    @Select("SELECT 1")
    int ping();
}
