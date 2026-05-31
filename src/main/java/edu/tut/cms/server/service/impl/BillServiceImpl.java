package edu.tut.cms.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.exception.BusinessException;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.*;
import edu.tut.cms.pojo.entity.Bill;
import edu.tut.cms.pojo.entity.BillDetail;
import edu.tut.cms.pojo.entity.BillType;
import edu.tut.cms.pojo.vo.BillDetailVO;
import edu.tut.cms.pojo.vo.BillSummaryVO;
import edu.tut.cms.pojo.vo.BillTypeVO;
import edu.tut.cms.pojo.vo.BillVO;
import edu.tut.cms.server.mapper.BillDetailMapper;
import edu.tut.cms.server.mapper.BillMapper;
import edu.tut.cms.server.mapper.BillTypeMapper;
import edu.tut.cms.server.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillMapper billMapper;
    private final BillDetailMapper billDetailMapper;
    private final BillTypeMapper billTypeMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBill(BillAddDTO dto) {
        CurrentUser currentUser = requireCurrentUser();
        Bill bill = Bill.builder()
                .billCode(dto.getBillCode())
                .ownerId(dto.getOwnerId())
                .createrId(currentUser.getId())
                .assetId(dto.getAssetId())
                .billCreateTime(LocalDateTime.now())
                .billStartTime(dto.getBillStartTime())
                .billEndTime(dto.getBillEndTime())
                .billStatus(1)
                .billTotal(dto.getBillTotal())
                .billPaid(dto.getBillPaid() != null ? dto.getBillPaid() : BigDecimal.ZERO)
                .billRemark(dto.getBillRemark())
                .build();
        billMapper.insert(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBill(Long id) {
        billDetailMapper.deleteByBillId(id);
        billMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteBills(BillBatchDeleteDTO dto) {
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            throw new BusinessException("请选择要删除的账单");
        }
        for (Long id : dto.getIds()) {
            deleteBill(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBill(BillUpdateDTO dto) {
        Bill bill = Bill.builder()
                .id(dto.getId())
                .billStartTime(dto.getBillStartTime())
                .billEndTime(dto.getBillEndTime())
                .billStatus(dto.getBillStatus())
                .billTotal(dto.getBillTotal())
                .billPaid(dto.getBillPaid())
                .billRemark(dto.getBillRemark())
                .build();
        billMapper.updateById(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBillStatus(BillStatusDTO dto) {
        if (dto.getBillStatus() != null && dto.getBillStatus() == 0) {
            Bill bill = Bill.builder()
                    .id(dto.getId())
                    .billStatus(0)
                    .billFinishTime(LocalDateTime.now())
                    .build();
            billMapper.updateById(bill);
        } else {
            billMapper.updateStatus(dto.getId(), dto.getBillStatus());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payBill(BillPayDTO dto) {
        Bill bill = billMapper.selectById(dto.getId());
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        ensureBillVisibleToCurrentUser(bill);
        if (bill.getBillStatus() != null && bill.getBillStatus() == 0) {
            throw new BusinessException("账单已结清");
        }
        if (bill.getBillStatus() != null && bill.getBillStatus() == 5) {
            throw new BusinessException("已取消的账单无法付款");
        }
        Bill update = Bill.builder()
                .id(bill.getId())
                .billStatus(0)
                .billPaid(bill.getBillTotal())
                .billFinishTime(LocalDateTime.now())
                .build();
        billMapper.updateById(update);
    }

    @Override
    public BillVO getBill(Long id) {
        Bill bill = billMapper.selectById(id);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        ensureBillVisibleToCurrentUser(bill);
        List<BillDetailVO> details = billDetailMapper.selectByBillId(id);

        List<BillVO> voList = billMapper.selectPage(bill.getOwnerId(), null, bill.getBillCode());
        BillVO vo = voList.stream()
                .filter(v -> v.getId().equals(id))
                .findFirst()
                .orElse(BillVO.builder()
                        .id(bill.getId())
                        .billCode(bill.getBillCode())
                        .ownerId(bill.getOwnerId())
                        .createrId(bill.getCreaterId())
                        .assetId(bill.getAssetId())
                        .billCreateTime(bill.getBillCreateTime())
                        .billStartTime(bill.getBillStartTime())
                        .billEndTime(bill.getBillEndTime())
                        .billFinishTime(bill.getBillFinishTime())
                        .billStatus(bill.getBillStatus())
                        .billTotal(bill.getBillTotal())
                        .billPaid(bill.getBillPaid())
                        .billRemark(bill.getBillRemark())
                        .build());
        vo.setDetails(details);
        return vo;
    }

    @Override
    public PageResult<BillVO> pageBills(Integer pageNum, Integer pageSize,
                                        Long ownerId, Integer billStatus, String billCode) {
        CurrentUser currentUser = requireCurrentUser();
        if (currentUser.getUserRole() >= 4) {
            ownerId = currentUser.getId();
        }

        PageHelper.startPage(pageNum, pageSize);
        List<BillVO> list = billMapper.selectPage(ownerId, billStatus, billCode);
        PageInfo<BillVO> pageInfo = new PageInfo<>(list);

        PageResult<BillVO> result = new PageResult<>();
        result.setTotal(pageInfo.getTotal());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        result.setRecords(pageInfo.getList());
        return result;
    }

    @Override
    public BillSummaryVO getMyBillSummary() {
        CurrentUser currentUser = requireCurrentUser();
        BillSummaryVO summary = billMapper.selectSummary(currentUser.getId());
        if (summary == null) {
            return BillSummaryVO.builder()
                    .unpaidCount(0L)
                    .unpaidAmount(BigDecimal.ZERO)
                    .overdueCount(0L)
                    .partiallyPaidCount(0L)
                    .build();
        }
        if (summary.getUnpaidAmount() == null) {
            summary.setUnpaidAmount(BigDecimal.ZERO);
        }
        return summary;
    }

    @Override
    public PageResult<BillVO> pageMyUnpaidBills(Integer pageNum, Integer pageSize) {
        CurrentUser currentUser = requireCurrentUser();
        PageHelper.startPage(pageNum, pageSize);
        List<BillVO> list = billMapper.selectUnpaidPage(currentUser.getId());
        return PageResult.of(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBillDetail(BillDetailAddDTO dto) {
        BillDetail detail = BillDetail.builder()
                .billId(dto.getBillId())
                .billDetailName(dto.getBillDetailName())
                .billDetailType(dto.getBillDetailType())
                .billDetailQuantity(dto.getBillDetailQuantity())
                .billDetailUnitPrice(dto.getBillDetailUnitPrice())
                .billDetailTotal(dto.getBillDetailTotal())
                .billDetailRemark(dto.getBillDetailRemark())
                .build();
        billDetailMapper.insert(detail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBillDetail(BillDetailUpdateDTO dto) {
        BillDetail detail = BillDetail.builder()
                .id(dto.getId())
                .billDetailName(dto.getBillDetailName())
                .billDetailType(dto.getBillDetailType())
                .billDetailQuantity(dto.getBillDetailQuantity())
                .billDetailUnitPrice(dto.getBillDetailUnitPrice())
                .billDetailTotal(dto.getBillDetailTotal())
                .billDetailRemark(dto.getBillDetailRemark())
                .build();
        billDetailMapper.updateById(detail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBillDetail(Long id) {
        billDetailMapper.deleteById(id);
    }

    @Override
    public List<BillDetailVO> getBillDetails(Long billId) {
        Bill bill = billMapper.selectById(billId);
        if (bill == null) {
            return Collections.emptyList();
        }
        ensureBillVisibleToCurrentUser(bill);
        return billDetailMapper.selectByBillId(billId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportBillsToExcel(Long ownerId, Integer billStatus) {
        CurrentUser currentUser = requireCurrentUser();
        if (currentUser.getUserRole() >= 4) {
            ownerId = currentUser.getId();
        }

        List<BillVO> bills = billMapper.selectPage(ownerId, billStatus, null);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("账单数据");

            XSSFRow header = sheet.createRow(0);
            String[] columns = {"账单编码", "业主", "资产", "创建时间", "起始时间", "截止时间", "状态", "应收总额", "实收总额", "备注"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            for (int i = 0; i < bills.size(); i++) {
                BillVO bill = bills.get(i);
                XSSFRow row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(bill.getBillCode() != null ? bill.getBillCode() : "");
                row.createCell(1).setCellValue(bill.getOwnerName() != null ? bill.getOwnerName() : "");
                row.createCell(2).setCellValue(bill.getAssetName() != null ? bill.getAssetName() : "");
                row.createCell(3).setCellValue(bill.getBillCreateTime() != null ? bill.getBillCreateTime().format(FORMATTER) : "");
                row.createCell(4).setCellValue(bill.getBillStartTime() != null ? bill.getBillStartTime().format(FORMATTER) : "");
                row.createCell(5).setCellValue(bill.getBillEndTime() != null ? bill.getBillEndTime().format(FORMATTER) : "");
                row.createCell(6).setCellValue(getStatusName(bill.getBillStatus()));
                row.createCell(7).setCellValue(bill.getBillTotal() != null ? bill.getBillTotal().doubleValue() : 0);
                row.createCell(8).setCellValue(bill.getBillPaid() != null ? bill.getBillPaid().doubleValue() : 0);
                row.createCell(9).setCellValue(bill.getBillRemark() != null ? bill.getBillRemark() : "");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("导出账单Excel失败", e);
            throw new BusinessException("导出失败");
        }
    }

    private String getStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "已完成";
            case 1 -> "未完成";
            case 2 -> "部分完成";
            case 3 -> "已逾期";
            case 4 -> "已补缴";
            case 5 -> "已取消";
            default -> String.valueOf(status);
        };
    }

    @Override
    public List<BillTypeVO> listBillTypes() {
        List<BillType> types = billTypeMapper.selectAll();
        return types.stream().map(t -> BillTypeVO.builder()
                .billTypeId(t.getBillTypeId())
                .billTypeName(t.getBillTypeName())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBillType(BillTypeAddDTO dto) {
        Integer nextTypeId = billTypeMapper.selectMaxTypeId() + 1;
        BillType type = BillType.builder()
                .billTypeId(nextTypeId)
                .billTypeName(dto.getBillTypeName())
                .build();
        billTypeMapper.insert(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBillType(Integer id) {
        if (billDetailMapper.countByBillTypeId(id) > 0) {
            throw new BusinessException("该账单类型已被账单明细使用，无法删除");
        }
        billTypeMapper.deleteById(id);
    }

    private CurrentUser requireCurrentUser() {
        CurrentUser currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        return currentUser;
    }

    private void ensureBillVisibleToCurrentUser(Bill bill) {
        CurrentUser currentUser = requireCurrentUser();
        if (currentUser.getUserRole() != null
                && currentUser.getUserRole() >= 4
                && !currentUser.getId().equals(bill.getOwnerId())) {
            throw new BusinessException("无权访问该账单");
        }
    }
}
