package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.Asset;
import edu.tut.cms.pojo.vo.AssetVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AssetMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO asset(asset_code, asset_owner_id, asset_name, asset_type, asset_reg_time, asset_due_time, asset_status, asset_area, asset_building, asset_floor, asset_position, asset_remark) VALUES(#{assetCode}, #{assetOwnerId}, #{assetName}, #{assetType}, #{assetRegTime}, #{assetDueTime}, #{assetStatus}, #{assetArea}, #{assetBuilding}, #{assetFloor}, #{assetPosition}, #{assetRemark})")
    int insert(Asset asset);

    int updateById(Asset asset);

    @Delete("DELETE FROM asset WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM asset WHERE id = #{id}")
    Asset selectById(Long id);

    @Select("SELECT COUNT(1) FROM asset WHERE asset_code = #{assetCode}")
    int countByCode(String assetCode);

    @Select("SELECT COUNT(1) FROM asset WHERE asset_type = #{assetTypeId}")
    int countByAssetTypeId(Integer assetTypeId);

    List<AssetVO> selectPage(@Param("assetOwnerId") Long assetOwnerId,
                             @Param("ownerUid") String ownerUid,
                             @Param("assetType") Integer assetType,
                             @Param("assetStatus") Integer assetStatus,
                             @Param("assetBuilding") Long assetBuilding);

    List<AssetVO> selectAll(@Param("assetOwnerId") Long assetOwnerId);
}
