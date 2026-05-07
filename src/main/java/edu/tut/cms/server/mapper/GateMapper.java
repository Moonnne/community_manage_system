package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.Gate;
import edu.tut.cms.pojo.vo.GateVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GateMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO gate(gate_code, gate_name, gate_chief_id, gate_remark) VALUES(#{gateCode}, #{gateName}, #{gateChiefId}, #{gateRemark})")
    int insert(Gate gate);

    int updateById(Gate gate);

    @Delete("DELETE FROM gate WHERE id = #{id}")
    int deleteById(Integer id);

    @Select("SELECT * FROM gate WHERE id = #{id}")
    Gate selectById(Integer id);

    List<GateVO> selectAll();
}
