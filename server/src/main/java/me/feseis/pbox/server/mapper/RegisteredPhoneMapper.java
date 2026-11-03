package me.feseis.pbox.server.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RegisteredPhoneMapper {

    @Select("SELECT phone FROM registered_phone ORDER BY phone")
    List<String> findAll();

    @Select("SELECT phone FROM registered_phone WHERE phone = #{phone}")
    String findByPhone(@Param("phone") String phone);

    @Insert("INSERT INTO registered_phone (phone) VALUES (#{phone})")
    int insert(@Param("phone") String phone);

    @Delete("DELETE FROM registered_phone WHERE phone = #{phone}")
    int deleteByPhone(@Param("phone") String phone);
}
