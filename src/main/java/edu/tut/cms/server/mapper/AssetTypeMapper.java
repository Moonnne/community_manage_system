package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.AssetType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AssetTypeMapper {

    @Select("SELECT * FROM asset_type ORDER BY asset_type_id ASC")
    List<AssetType> selectAll();

    @Select("SELECT * FROM asset_type WHERE asset_type_id = #{assetTypeId}")
    AssetType selectById(Integer assetTypeId);

    @Select("SELECT COALESCE(MAX(asset_type_id), 0) FROM asset_type")
    Integer selectMaxTypeId();

    @Insert("INSERT INTO asset_type(asset_type_id, asset_type_name, asset_type_short) VALUES(#{assetTypeId}, #{assetTypeName}, #{assetTypeShort})")
    int insert(AssetType type);

    @Update("UPDATE asset_type SET asset_type_name = #{assetTypeName}, asset_type_short = #{assetTypeShort} WHERE asset_type_id = #{assetTypeId}")
    int updateById(AssetType type);

    @Delete("DELETE FROM asset_type WHERE asset_type_id = #{assetTypeId}")
    int deleteById(Integer assetTypeId);
}
