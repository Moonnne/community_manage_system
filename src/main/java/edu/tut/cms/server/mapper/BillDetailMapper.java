package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.BillDetail;
import edu.tut.cms.pojo.vo.BillDetailVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BillDetailMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO bill_detail(bill_id, bill_detail_name, bill_detail_type, bill_detail_quantity, bill_detail_unit_price, bill_detail_total, bill_detail_status, bill_detail_remark) VALUES(#{billId}, #{billDetailName}, #{billDetailType}, #{billDetailQuantity}, #{billDetailUnitPrice}, #{billDetailTotal}, #{billDetailStatus}, #{billDetailRemark})")
    int insert(BillDetail detail);

    int updateById(BillDetail detail);

    @Delete("DELETE FROM bill_detail WHERE id = #{id}")
    int deleteById(Long id);

    @Delete("DELETE FROM bill_detail WHERE bill_id = #{billId}")
    int deleteByBillId(Long billId);

    @Select("SELECT * FROM bill_detail WHERE id = #{id}")
    BillDetail selectById(Long id);

    @Select("SELECT COUNT(1) FROM bill_detail WHERE bill_detail_type = #{billTypeId}")
    int countByBillTypeId(Integer billTypeId);

    List<BillDetailVO> selectByBillId(Long billId);
}
