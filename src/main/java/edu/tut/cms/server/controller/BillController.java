package edu.tut.cms.server.controller;

import edu.tut.cms.common.aop.RequirePermission;
import edu.tut.cms.common.aop.RequireRole;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.common.result.Result;
import edu.tut.cms.pojo.dto.*;
import edu.tut.cms.pojo.vo.BillDetailVO;
import edu.tut.cms.pojo.vo.BillSummaryVO;
import edu.tut.cms.pojo.vo.BillTypeVO;
import edu.tut.cms.pojo.vo.BillVO;
import edu.tut.cms.server.service.BillService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/bill")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping("/add")
    @RequirePermission(minLevel = 1)
    @RequireRole({0, 1, 2})
    public Result<Void> add(@Valid @RequestBody BillAddDTO dto) {
        billService.addBill(dto);
        return Result.success();
    }

    @PostMapping("/delete")
    @RequirePermission(minLevel = 1)
    @RequireRole({0, 1, 2})
    public Result<Void> delete(@RequestParam Long id) {
        billService.deleteBill(id);
        return Result.success();
    }

    @PostMapping("/delete/batch")
    @RequirePermission(minLevel = 1)
    @RequireRole({0, 1, 2})
    public Result<Void> batchDelete(@Valid @RequestBody BillBatchDeleteDTO dto) {
        billService.batchDeleteBills(dto);
        return Result.success();
    }

    @PostMapping("/update")
    @RequirePermission(minLevel = 1)
    @RequireRole({0, 1, 2})
    public Result<Void> update(@Valid @RequestBody BillUpdateDTO dto) {
        billService.updateBill(dto);
        return Result.success();
    }

    @PostMapping("/status")
    @RequirePermission(minLevel = 1)
    @RequireRole({0, 1, 2})
    public Result<Void> updateStatus(@Valid @RequestBody BillStatusDTO dto) {
        billService.updateBillStatus(dto);
        return Result.success();
    }

    @PostMapping("/pay")
    @RequirePermission(minLevel = 0)
    public Result<Void> pay(@Valid @RequestBody BillPayDTO dto) {
        billService.payBill(dto);
        return Result.success();
    }

    @PostMapping("/detail/pay")
    @RequirePermission(minLevel = 0)
    public Result<Void> payDetail(@Valid @RequestBody BillDetailPayDTO dto) {
        billService.payBillDetail(dto);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<BillVO> get(@PathVariable Long id) {
        return Result.success(billService.getBill(id));
    }

    @GetMapping("/page")
    public Result<PageResult<BillVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam(required = false) Long ownerId,
                                           @RequestParam(required = false) Integer billStatus,
                                           @RequestParam(required = false) String billCode,
                                           @RequestParam(required = false) String ownerUid,
                                           @RequestParam(required = false) String createrUid,
                                           @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime rangeStart,
                                           @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime rangeEnd) {
        return Result.success(billService.pageBills(pageNum, pageSize, ownerId, billStatus, billCode, ownerUid, createrUid, rangeStart, rangeEnd));
    }

    @GetMapping("/my/summary")
    @RequirePermission(minLevel = 0)
    public Result<BillSummaryVO> mySummary() {
        return Result.success(billService.getMyBillSummary());
    }

    @GetMapping("/my/unpaid")
    @RequirePermission(minLevel = 0)
    public Result<PageResult<BillVO>> myUnpaid(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(billService.pageMyUnpaidBills(pageNum, pageSize));
    }

    @PostMapping("/detail/add")
    @RequirePermission(minLevel = 1)
    @RequireRole({0, 1, 2})
    public Result<Void> addDetail(@Valid @RequestBody BillDetailAddDTO dto) {
        billService.addBillDetail(dto);
        return Result.success();
    }

    @PostMapping("/detail/update")
    @RequirePermission(minLevel = 1)
    @RequireRole({0, 1, 2})
    public Result<Void> updateDetail(@Valid @RequestBody BillDetailUpdateDTO dto) {
        billService.updateBillDetail(dto);
        return Result.success();
    }

    @PostMapping("/detail/delete")
    @RequirePermission(minLevel = 1)
    @RequireRole({0, 1, 2})
    public Result<Void> deleteDetail(@RequestParam Long id) {
        billService.deleteBillDetail(id);
        return Result.success();
    }

    @GetMapping("/detail/{billId}")
    public Result<List<BillDetailVO>> getDetails(@PathVariable Long billId) {
        return Result.success(billService.getBillDetails(billId));
    }

    @GetMapping("/export")
    @RequirePermission(minLevel = 1)
    public void exportExcel(@RequestParam(required = false) Long ownerId,
                            @RequestParam(required = false) Integer billStatus,
                            HttpServletResponse response) throws IOException {
        byte[] bytes = billService.exportBillsToExcel(ownerId, billStatus);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("账单导出.xlsx", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }

    @GetMapping("/types")
    public Result<List<BillTypeVO>> types() {
        return Result.success(billService.listBillTypes());
    }

    @PostMapping("/type/add")
    @RequireRole({0, 1})
    public Result<Void> addType(@Valid @RequestBody BillTypeAddDTO dto) {
        billService.addBillType(dto);
        return Result.success();
    }

    @PostMapping("/type/delete")
    @RequireRole({0, 1})
    public Result<Void> deleteType(@RequestParam Integer id) {
        billService.deleteBillType(id);
        return Result.success();
    }
}
