package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.Bill;
import edu.tut.cms.pojo.vo.BillSummaryVO;
import edu.tut.cms.pojo.vo.BillVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BillMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO bill(bill_code, owner_id, creater_id, asset_id, bill_create_time, bill_start_time, bill_end_time, bill_status, bill_total, bill_paid, bill_remark) VALUES(#{billCode}, #{ownerId}, #{createrId}, #{assetId}, #{billCreateTime}, #{billStartTime}, #{billEndTime}, #{billStatus}, #{billTotal}, #{billPaid}, #{billRemark})")
    int insert(Bill bill);

    int updateById(Bill bill);

    @Delete("DELETE FROM bill WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM bill WHERE id = #{id}")
    Bill selectById(Long id);

    @Select("SELECT COUNT(1) FROM bill WHERE bill_code = #{billCode}")
    int countByBillCode(String billCode);

    @Update("UPDATE bill SET bill_status = #{billStatus} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("billStatus") Integer billStatus);

    List<BillVO> selectPage(@Param("ownerId") Long ownerId,
                            @Param("billStatus") Integer billStatus,
                            @Param("billCode") String billCode,
                            @Param("ownerUid") String ownerUid,
                            @Param("createrUid") String createrUid,
                            @Param("rangeStart") java.time.LocalDateTime rangeStart,
                            @Param("rangeEnd") java.time.LocalDateTime rangeEnd);

    List<BillVO> selectUnpaidPage(@Param("ownerId") Long ownerId);

    BillSummaryVO selectSummary(@Param("ownerId") Long ownerId);
}
