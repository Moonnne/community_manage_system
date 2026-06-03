package edu.tut.cms.server.service;

import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.BuildingAddDTO;
import edu.tut.cms.pojo.dto.BuildingUpdateDTO;
import edu.tut.cms.pojo.vo.BuildingVO;

import java.util.List;

public interface BuildingService {
    void addBuilding(BuildingAddDTO dto);

    void updateBuilding(BuildingUpdateDTO dto);

    void deleteBuilding(Long id);

    PageResult<BuildingVO> pageBuildings(Integer pageNum,
                                         Integer pageSize,
                                         String buildingCode,
                                         String buildingName,
                                         String buildingLocation,
                                         String buildingRemark);

    List<BuildingVO> listBuildings();
}
