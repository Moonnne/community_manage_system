package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.VisitorType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VisitorTypeMapper {

    @Select("SELECT * FROM visitor_type ORDER BY id ASC")
    List<VisitorType> selectAll();

    @Select("SELECT * FROM visitor_type WHERE visitor_type_id = #{visitorTypeId}")
    VisitorType selectById(Integer visitorTypeId);

    @Insert("INSERT INTO visitor_type(visitor_type_id, visitor_type_name, visitor_type_remark) VALUES(#{visitorTypeId}, #{visitorTypeName}, #{visitorTypeRemark})")
    int insert(VisitorType type);

    @Delete("DELETE FROM visitor_type WHERE visitor_type_id = #{visitorTypeId}")
    int deleteById(Integer visitorTypeId);
}
