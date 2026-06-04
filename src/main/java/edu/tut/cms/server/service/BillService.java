package edu.tut.cms.server.service;

import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.*;
import edu.tut.cms.pojo.vo.BillDetailVO;
import edu.tut.cms.pojo.vo.BillSummaryVO;
import edu.tut.cms.pojo.vo.BillTypeVO;
import edu.tut.cms.pojo.vo.BillVO;

import java.util.List;
import java.time.LocalDateTime;

public interface BillService {

    /**
     * 新增账单
     *
     * @param dto 账单信息
     */
    void addBill(BillAddDTO dto);

    /**
     * 删除账单（同时删除账单明细）
     *
     * @param id 账单ID
     */
    void deleteBill(Long id);

    void batchDeleteBills(BillBatchDeleteDTO dto);

    /**
     * 更新账单信息
     *
     * @param dto 账单更新信息
     */
    void updateBill(BillUpdateDTO dto);

    /**
     * 更新账单状态，若为已完成则设置完成时间
     *
     * @param dto 账单ID和目标状态
     */
    void updateBillStatus(BillStatusDTO dto);

    void payBill(BillPayDTO dto);

    void payBillDetail(BillDetailPayDTO dto);

    /**
     * 获取账单详情（含明细列表）
     *
     * @param id 账单ID
     * @return 账单VO
     */
    BillVO getBill(Long id);

    /**
     * 分页查询账单列表
     *
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @param ownerId    业主ID
     * @param billStatus 账单状态
     * @param billCode   账单编码（模糊）
     * @return 分页结果
     */
    PageResult<BillVO> pageBills(Integer pageNum, Integer pageSize,
                                 Long ownerId, Integer billStatus, String billCode,
                                 String ownerUid, String createrUid,
                                 LocalDateTime rangeStart, LocalDateTime rangeEnd);

    BillSummaryVO getMyBillSummary();

    PageResult<BillVO> pageMyUnpaidBills(Integer pageNum, Integer pageSize);

    /**
     * 新增账单明细
     *
     * @param dto 明细信息
     */
    void addBillDetail(BillDetailAddDTO dto);

    /**
     * 更新账单明细
     *
     * @param dto 明细更新信息
     */
    void updateBillDetail(BillDetailUpdateDTO dto);

    /**
     * 删除账单明细
     *
     * @param id 明细ID
     */
    void deleteBillDetail(Long id);

    /**
     * 获取指定账单的所有明细
     *
     * @param billId 账单ID
     * @return 明细列表
     */
    List<BillDetailVO> getBillDetails(Long billId);

    /**
     * 导出账单数据为Excel
     *
     * @param ownerId    业主ID（可选）
     * @param billStatus 账单状态（可选）
     * @return Excel字节数组
     */
    byte[] exportBillsToExcel(Long ownerId, Integer billStatus);

    /**
     * 获取所有账单类型
     *
     * @return 账单类型列表
     */
    List<BillTypeVO> listBillTypes();

    /**
     * 新增账单类型
     *
     * @param dto 账单类型信息
     */
    void addBillType(BillTypeAddDTO dto);

    /**
     * 删除账单类型
     *
     * @param id 账单类型ID
     */
    void deleteBillType(Integer id);
}
