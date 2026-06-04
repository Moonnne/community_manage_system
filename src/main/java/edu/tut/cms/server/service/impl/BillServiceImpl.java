package edu.tut.cms.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.exception.BusinessException;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.*;
import edu.tut.cms.pojo.entity.Asset;
import edu.tut.cms.pojo.entity.Bill;
import edu.tut.cms.pojo.entity.BillDetail;
import edu.tut.cms.pojo.entity.BillType;
import edu.tut.cms.pojo.vo.BillDetailVO;
import edu.tut.cms.pojo.vo.BillSummaryVO;
import edu.tut.cms.pojo.vo.BillTypeVO;
import edu.tut.cms.pojo.vo.BillVO;
import edu.tut.cms.server.mapper.AssetMapper;
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
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] BILL_CODE_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final int BILL_CODE_LENGTH = 16;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final BillMapper billMapper;
    private final BillDetailMapper billDetailMapper;
    private final BillTypeMapper billTypeMapper;
    private final AssetMapper assetMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBill(BillAddDTO dto) {
        CurrentUser currentUser = requireCurrentUser();
        validateBillTime(dto.getBillStartTime(), dto.getBillEndTime());
        List<BillDetailAddDTO> details = requireDetails(dto.getDetails());
        List<Long> assetIds = resolveAssetIds(dto);

        for (Long assetId : assetIds) {
            Asset asset = requireAsset(assetId);
            Long ownerId = asset.getAssetOwnerId();
            if (dto.getOwnerId() != null && !dto.getOwnerId().equals(ownerId)) {
                throw new BusinessException("所选资产不属于当前用户");
            }

            Bill bill = Bill.builder()
                    .billCode(generateBillCode())
                    .ownerId(ownerId)
                    .createrId(currentUser.getId())
                    .assetId(assetId)
                    .billCreateTime(LocalDateTime.now())
                    .billStartTime(dto.getBillStartTime())
                    .billEndTime(dto.getBillEndTime())
                    .billStatus(1)
                    .billTotal(sumDetailTotal(details))
                    .billPaid(ZERO)
                    .billRemark(dto.getBillRemark())
                    .build();
            bill.setBillStatus(resolveBillStatus(bill));
            billMapper.insert(bill);
            insertDetails(bill.getId(), details, Collections.emptyMap());
            refreshBillPaymentFromDetails(bill.getId());
        }
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
        Bill oldBill = requireBill(dto.getId());
        validateBillTime(dto.getBillStartTime(), dto.getBillEndTime());
        List<BillDetailAddDTO> details = requireDetails(dto.getDetails());
        Long assetId = dto.getAssetId() != null ? dto.getAssetId() : oldBill.getAssetId();
        Asset asset = requireAsset(assetId);

        Map<Long, BillDetailVO> oldDetailMap = billDetailMapper.selectByBillId(oldBill.getId()).stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(BillDetailVO::getId, item -> item, (a, b) -> a));

        Bill bill = Bill.builder()
                .id(oldBill.getId())
                .ownerId(asset.getAssetOwnerId())
                .assetId(assetId)
                .billStartTime(dto.getBillStartTime())
                .billEndTime(dto.getBillEndTime())
                .billTotal(sumDetailTotal(details))
                .billPaid(min(nonNull(oldBill.getBillPaid()), sumDetailTotal(details)))
                .billRemark(dto.getBillRemark())
                .build();
        bill.setBillStatus(resolveBillStatus(mergeForStatus(oldBill, bill)));
        bill.setBillFinishTime(bill.getBillStatus() != null && bill.getBillStatus() == 0 ? LocalDateTime.now() : null);
        billMapper.updateById(bill);

        billDetailMapper.deleteByBillId(oldBill.getId());
        insertDetails(oldBill.getId(), details, oldDetailMap);
        refreshBillPaymentFromDetails(oldBill.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBillStatus(BillStatusDTO dto) {
        Bill bill = requireBill(dto.getId());
        if (dto.getBillStatus() != null && dto.getBillStatus() == 5) {
            billMapper.updateById(Bill.builder().id(bill.getId()).billStatus(5).build());
            return;
        }
        refreshBillPaymentFromDetails(bill.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payBill(BillPayDTO dto) {
        Bill bill = requireBill(dto.getId());
        ensureBillVisibleToCurrentUser(bill);
        ensureBillPayable(bill);
        for (BillDetailVO detail : billDetailMapper.selectByBillId(bill.getId())) {
            billDetailMapper.updateById(BillDetail.builder()
                    .id(detail.getId())
                    .billDetailStatus(0)
                    .build());
        }
        refreshBillPaymentFromDetails(bill.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payBillDetail(BillDetailPayDTO dto) {
        Bill bill = requireBill(dto.getBillId());
        ensureBillVisibleToCurrentUser(bill);
        ensureBillPayable(bill);
        BillDetail detail = billDetailMapper.selectById(dto.getDetailId());
        if (detail == null || !bill.getId().equals(detail.getBillId())) {
            throw new BusinessException("账单款项不存在");
        }
        billDetailMapper.updateById(BillDetail.builder()
                .id(detail.getId())
                .billDetailStatus(0)
                .build());
        refreshBillPaymentFromDetails(bill.getId());
    }

    @Override
    public BillVO getBill(Long id) {
        Bill bill = requireBill(id);
        ensureBillVisibleToCurrentUser(bill);
        List<BillVO> voList = billMapper.selectPage(bill.getOwnerId(), null, bill.getBillCode(), null, null, null, null);
        BillVO vo = voList.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(toVO(bill));
        normalizeVOStatus(vo);
        vo.setDetails(billDetailMapper.selectByBillId(id));
        return vo;
    }

    @Override
    public PageResult<BillVO> pageBills(Integer pageNum, Integer pageSize,
                                        Long ownerId, Integer billStatus, String billCode,
                                        String ownerUid, String createrUid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        CurrentUser currentUser = requireCurrentUser();
        if (currentUser.getUserRole() != null && currentUser.getUserRole() >= 4) {
            ownerId = currentUser.getId();
        }
        PageHelper.startPage(pageNum, pageSize);
        List<BillVO> list = billMapper.selectPage(ownerId, billStatus, billCode, ownerUid, createrUid, rangeStart, rangeEnd);
        list.forEach(this::normalizeVOStatus);
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
            return emptySummary();
        }
        if (summary.getUnpaidAmount() == null) {
            summary.setUnpaidAmount(ZERO);
        }
        if (summary.getUnpaidCount() == null) summary.setUnpaidCount(0L);
        if (summary.getPaidCount() == null) summary.setPaidCount(0L);
        if (summary.getCancelledCount() == null) summary.setCancelledCount(0L);
        if (summary.getOverdueCount() == null) summary.setOverdueCount(0L);
        if (summary.getPartiallyPaidCount() == null) summary.setPartiallyPaidCount(0L);
        return summary;
    }

    @Override
    public PageResult<BillVO> pageMyUnpaidBills(Integer pageNum, Integer pageSize) {
        CurrentUser currentUser = requireCurrentUser();
        PageHelper.startPage(pageNum, pageSize);
        List<BillVO> list = billMapper.selectUnpaidPage(currentUser.getId());
        list.forEach(this::normalizeVOStatus);
        return PageResult.of(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBillDetail(BillDetailAddDTO dto) {
        Bill bill = requireBill(dto.getBillId());
        BillDetail detail = buildDetail(bill.getId(), dto, null);
        billDetailMapper.insert(detail);
        refreshBillPaymentFromDetails(bill.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBillDetail(BillDetailUpdateDTO dto) {
        BillDetail old = billDetailMapper.selectById(dto.getId());
        if (old == null) {
            throw new BusinessException("账单款项不存在");
        }
        BigDecimal total = calculateDetailTotal(dto.getBillDetailQuantity(), dto.getBillDetailUnitPrice());
        Integer status = old.getBillDetailStatus() != null && old.getBillDetailStatus() == 0 ? 0 : 1;
        BillDetail detail = BillDetail.builder()
                .id(dto.getId())
                .billDetailName(dto.getBillDetailName())
                .billDetailType(dto.getBillDetailType())
                .billDetailQuantity(dto.getBillDetailQuantity())
                .billDetailUnitPrice(dto.getBillDetailUnitPrice())
                .billDetailTotal(total)
                .billDetailStatus(status)
                .billDetailRemark(dto.getBillDetailRemark())
                .build();
        billDetailMapper.updateById(detail);
        refreshBillPaymentFromDetails(old.getBillId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBillDetail(Long id) {
        BillDetail old = billDetailMapper.selectById(id);
        if (old == null) {
            return;
        }
        billDetailMapper.deleteById(id);
        refreshBillPaymentFromDetails(old.getBillId());
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
        if (currentUser.getUserRole() != null && currentUser.getUserRole() >= 4) {
            ownerId = currentUser.getId();
        }
        List<BillVO> bills = billMapper.selectPage(ownerId, billStatus, null, null, null, null, null);
        bills.forEach(this::normalizeVOStatus);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("账单数据");
            XSSFRow header = sheet.createRow(0);
            String[] columns = {"账单编号", "业主", "资产", "创建时间", "起始时间", "截止时间", "状态", "应收总额", "实收总额", "备注"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }
            for (int i = 0; i < bills.size(); i++) {
                BillVO bill = bills.get(i);
                XSSFRow row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(orEmpty(bill.getBillCode()));
                row.createCell(1).setCellValue(orEmpty(bill.getOwnerName()));
                row.createCell(2).setCellValue(orEmpty(bill.getAssetName()));
                row.createCell(3).setCellValue(formatTime(bill.getBillCreateTime()));
                row.createCell(4).setCellValue(formatTime(bill.getBillStartTime()));
                row.createCell(5).setCellValue(formatTime(bill.getBillEndTime()));
                row.createCell(6).setCellValue(getStatusName(bill.getBillStatus()));
                row.createCell(7).setCellValue(nonNull(bill.getBillTotal()).doubleValue());
                row.createCell(8).setCellValue(nonNull(bill.getBillPaid()).doubleValue());
                row.createCell(9).setCellValue(orEmpty(bill.getBillRemark()));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("导出账单 Excel 失败", e);
            throw new BusinessException("导出失败");
        }
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
            throw new BusinessException("该账单类型已被账单款项使用，无法删除");
        }
        billTypeMapper.deleteById(id);
    }

    private void insertDetails(Long billId, List<BillDetailAddDTO> details, Map<Long, BillDetailVO> oldDetailMap) {
        for (BillDetailAddDTO dto : details) {
            BillDetailVO oldDetail = dto.getId() != null ? oldDetailMap.get(dto.getId()) : null;
            BillDetail detail = buildDetail(billId, dto, oldDetail);
            billDetailMapper.insert(detail);
        }
    }

    private BillDetail buildDetail(Long billId, BillDetailAddDTO dto, BillDetailVO oldDetail) {
        BigDecimal total = calculateDetailTotal(dto.getBillDetailQuantity(), dto.getBillDetailUnitPrice());
        Integer status = oldDetail != null && oldDetail.getBillDetailStatus() != null && oldDetail.getBillDetailStatus() == 0 ? 0 : 1;
        return BillDetail.builder()
                .billId(billId)
                .billDetailName(dto.getBillDetailName())
                .billDetailType(dto.getBillDetailType())
                .billDetailQuantity(dto.getBillDetailQuantity())
                .billDetailUnitPrice(dto.getBillDetailUnitPrice())
                .billDetailTotal(total)
                .billDetailStatus(status)
                .billDetailRemark(dto.getBillDetailRemark())
                .build();
    }

    private void refreshBillPaymentFromDetails(Long billId) {
        Bill bill = requireBill(billId);
        List<BillDetailVO> details = billDetailMapper.selectByBillId(billId);
        BigDecimal total = details.stream().map(BillDetailVO::getBillDetailTotal).filter(Objects::nonNull)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal paid = details.stream()
                .filter(detail -> detail.getBillDetailStatus() != null && detail.getBillDetailStatus() == 0)
                .map(BillDetailVO::getBillDetailTotal)
                .filter(Objects::nonNull)
                .reduce(ZERO, BigDecimal::add);
        paid = min(paid, total);
        Bill merged = Bill.builder()
                .id(bill.getId())
                .billStatus(bill.getBillStatus())
                .billTotal(total)
                .billPaid(paid)
                .billEndTime(bill.getBillEndTime())
                .build();
        Integer status = resolveBillStatus(merged);
        billMapper.updateById(Bill.builder()
                .id(billId)
                .billTotal(total)
                .billPaid(paid)
                .billStatus(status)
                .billFinishTime(status == 0 ? LocalDateTime.now() : null)
                .build());
    }

    private Integer resolveBillStatus(Bill bill) {
        if (bill.getBillStatus() != null && bill.getBillStatus() == 5) {
            return 5;
        }
        BigDecimal total = nonNull(bill.getBillTotal());
        BigDecimal paid = nonNull(bill.getBillPaid());
        if (paid.compareTo(total) >= 0) {
            return 0;
        }
        if (bill.getBillEndTime() != null && bill.getBillEndTime().isBefore(LocalDateTime.now())) {
            return 3;
        }
        if (paid.compareTo(ZERO) > 0) {
            return 2;
        }
        return 1;
    }

    private String generateBillCode() {
        for (int i = 0; i < 20; i++) {
            StringBuilder builder = new StringBuilder("B-");
            for (int j = 0; j < BILL_CODE_LENGTH; j++) {
                builder.append(BILL_CODE_CHARS[RANDOM.nextInt(BILL_CODE_CHARS.length)]);
            }
            String code = builder.toString();
            if (billMapper.countByBillCode(code) == 0) {
                return code;
            }
        }
        throw new BusinessException("账单编号生成失败，请重试");
    }

    private void validateBillTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException("账单起止时间不能为空");
        }
        if (Duration.between(startTime, endTime).toMinutes() < 60) {
            throw new BusinessException("账单时长不能少于一小时");
        }
    }

    private List<BillDetailAddDTO> requireDetails(List<BillDetailAddDTO> details) {
        if (details == null || details.isEmpty()) {
            throw new BusinessException("请至少添加一个账单款项");
        }
        return details;
    }

    private List<Long> resolveAssetIds(BillAddDTO dto) {
        if (dto.getAssetIds() != null && !dto.getAssetIds().isEmpty()) {
            return dto.getAssetIds().stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        }
        if (dto.getAssetId() != null) {
            return List.of(dto.getAssetId());
        }
        throw new BusinessException("请选择资产");
    }

    private Asset requireAsset(Long assetId) {
        Asset asset = assetMapper.selectById(assetId);
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }
        if (asset.getAssetOwnerId() == null) {
            throw new BusinessException("资产未绑定用户");
        }
        return asset;
    }

    private Bill requireBill(Long id) {
        Bill bill = billMapper.selectById(id);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        return bill;
    }

    private BigDecimal sumDetailTotal(List<BillDetailAddDTO> details) {
        return details.stream()
                .map(item -> calculateDetailTotal(item.getBillDetailQuantity(), item.getBillDetailUnitPrice()))
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDetailTotal(Integer quantity, BigDecimal unitPrice) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("账单款项数量必须大于 0");
        }
        if (unitPrice == null || unitPrice.compareTo(ZERO) < 0) {
            throw new BusinessException("账单款项单价不能小于 0");
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private Bill mergeForStatus(Bill oldBill, Bill patch) {
        return Bill.builder()
                .billStatus(oldBill.getBillStatus())
                .billEndTime(patch.getBillEndTime() != null ? patch.getBillEndTime() : oldBill.getBillEndTime())
                .billTotal(patch.getBillTotal() != null ? patch.getBillTotal() : oldBill.getBillTotal())
                .billPaid(patch.getBillPaid() != null ? patch.getBillPaid() : oldBill.getBillPaid())
                .build();
    }

    private void normalizeVOStatus(BillVO vo) {
        Bill bill = Bill.builder()
                .billStatus(vo.getBillStatus())
                .billEndTime(vo.getBillEndTime())
                .billTotal(vo.getBillTotal())
                .billPaid(vo.getBillPaid())
                .build();
        vo.setBillStatus(resolveBillStatus(bill));
    }

    private void ensureBillPayable(Bill bill) {
        Integer status = resolveBillStatus(bill);
        if (status == 0) {
            throw new BusinessException("账单已结清");
        }
        if (status == 5) {
            throw new BusinessException("已取消的账单无法付款");
        }
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

    private BillSummaryVO emptySummary() {
        return BillSummaryVO.builder()
                .unpaidCount(0L)
                .paidCount(0L)
                .cancelledCount(0L)
                .unpaidAmount(ZERO)
                .overdueCount(0L)
                .partiallyPaidCount(0L)
                .build();
    }

    private BillVO toVO(Bill bill) {
        return BillVO.builder()
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
                .build();
    }

    private BigDecimal min(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    private BigDecimal nonNull(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : time.format(FORMATTER);
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
}
