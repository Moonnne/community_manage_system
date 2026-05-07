package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.Visitor;
import edu.tut.cms.pojo.vo.VisitorVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VisitorMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO visitor(visitor_code, asset_id, owner_id, visitor_creater, visitor_name, visitor_phone, visitor_type, visitor_status, visitor_create_time, visitor_remark) VALUES(#{visitorCode}, #{assetId}, #{ownerId}, #{visitorCreater}, #{visitorName}, #{visitorPhone}, #{visitorType}, #{visitorStatus}, #{visitorCreateTime}, #{visitorRemark})")
    int insert(Visitor visitor);

    int updateById(Visitor visitor);

    @Delete("DELETE FROM visitor WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM visitor WHERE id = #{id}")
    Visitor selectById(Long id);

    List<VisitorVO> selectPage(@Param("ownerId") Long ownerId,
                               @Param("visitorType") Integer visitorType,
                               @Param("visitorStatus") String visitorStatus,
                               @Param("visitorName") String visitorName);
}
