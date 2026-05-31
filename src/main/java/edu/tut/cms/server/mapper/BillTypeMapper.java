package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.BillType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BillTypeMapper {

    @Select("SELECT * FROM bill_type ORDER BY bill_type_id ASC")
    List<BillType> selectAll();

    @Select("SELECT * FROM bill_type WHERE bill_type_id = #{billTypeId}")
    BillType selectById(Integer billTypeId);

    @Insert("INSERT INTO bill_type(bill_type_id, bill_type_name) VALUES(#{billTypeId}, #{billTypeName})")
    int insert(BillType type);

    @Delete("DELETE FROM bill_type WHERE bill_type_id = #{billTypeId}")
    int deleteById(Integer billTypeId);

    @Select("SELECT COALESCE(MAX(bill_type_id), 0) FROM bill_type")
    Integer selectMaxTypeId();
}
