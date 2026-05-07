package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.AssetType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssetTypeMapper {

    @Select("SELECT * FROM asset_type")
    List<AssetType> selectAll();

    @Select("SELECT * FROM asset_type WHERE asset_type_id = #{assetTypeId}")
    AssetType selectById(Integer assetTypeId);
}
