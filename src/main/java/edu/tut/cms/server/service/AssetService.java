package edu.tut.cms.server.service;

import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.AssetAddDTO;
import edu.tut.cms.pojo.dto.AssetTypeAddDTO;
import edu.tut.cms.pojo.dto.AssetUpdateDTO;
import edu.tut.cms.pojo.vo.AssetTypeVO;
import edu.tut.cms.pojo.vo.AssetVO;

import java.util.List;

public interface AssetService {

    /**
     * 新增资产
     *
     * @param dto 资产信息
     */
    void addAsset(AssetAddDTO dto);

    /**
     * 更新资产信息
     *
     * @param dto 资产更新信息
     */
    void updateAsset(AssetUpdateDTO dto);

    /**
     * 删除资产
     *
     * @param id 资产ID
     */
    void deleteAsset(Long id);

    /**
     * 分页查询资产列表
     *
     * @param pageNum      页码
     * @param pageSize     每页大小
     * @param assetOwnerId 业主ID
     * @param assetType    资产类型
     * @param assetStatus  资产状态
     * @param assetBuilding 楼栋（模糊）
     * @return 分页结果
     */
    PageResult<AssetVO> pageAssets(Integer pageNum, Integer pageSize,
                                   Long assetOwnerId, Integer assetType,
                                   Integer assetStatus, Long assetBuilding);

    /**
     * 导出资产数据为Excel
     *
     * @param assetOwnerId 业主ID（可选）
     * @return Excel字节数组
     */
    byte[] exportAssetsToExcel(Long assetOwnerId);

    /**
     * 获取所有资产类型
     *
     * @return 资产类型列表
     */
    List<AssetTypeVO> listAssetTypes();

    void addAssetType(AssetTypeAddDTO dto);

    void updateAssetType(AssetTypeAddDTO dto);

    void deleteAssetType(Integer id);
}
