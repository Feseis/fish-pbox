package me.feseis.pbox.server.mapper;

import java.util.List;

import me.feseis.pbox.server.entity.SpecialSender;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SpecialSenderMapper {

    @Select("SELECT phone, sender_name, api_code FROM special_sender ORDER BY phone")
    List<SpecialSender> findAll();

    @Select("SELECT phone, sender_name, api_code FROM special_sender WHERE phone = #{phone}")
    SpecialSender findByPhone(@Param("phone") String phone);

    @Select("SELECT phone, sender_name, api_code FROM special_sender WHERE api_code = #{apiCode}")
    SpecialSender findByApiCode(@Param("apiCode") String apiCode);

    @Select("SELECT api_code FROM special_sender WHERE phone = #{phone}")
    String findApiCodeByPhone(@Param("phone") String phone);

    @Insert("""
            INSERT INTO special_sender (phone, sender_name, api_code)
            VALUES (#{phone}, #{senderName}, #{apiCode})
            """)
    int insert(SpecialSender sender);

    @Update("UPDATE special_sender SET sender_name = #{senderName} WHERE phone = #{phone}")
    int updateSenderName(@Param("phone") String phone, @Param("senderName") String senderName);

    @Delete("DELETE FROM special_sender WHERE phone = #{phone}")
    int deleteByPhone(@Param("phone") String phone);
}
