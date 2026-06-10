package edu.tut.cms.server.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import edu.tut.cms.common.config.DeepSeekAgentProperties;
import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.exception.BusinessException;
import edu.tut.cms.common.exception.ForbiddenException;
import edu.tut.cms.common.exception.UnauthorizedException;
import edu.tut.cms.pojo.dto.*;
import edu.tut.cms.pojo.vo.AgentActionVO;
import edu.tut.cms.server.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentServiceImpl implements AgentService {

    private static final long SSE_TIMEOUT = 120_000L;
    private static final Set<String> SAFE_ROUTES = Set.of(
            "/welcome",
            "/user/list",
            "/finance/dashboard",
            "/finance/bill",
            "/security/visitor",
            "/security/car",
            "/security/car-history",
            "/security/gate",
            "/facility/list",
            "/asset/property",
            "/asset/parking",
            "/asset/building",
            "/task/list"
    );
    private static final Map<Integer, Integer> PERMISSION_MAP = Map.of(
            0, 3,
            1, 2,
            2, 1,
            3, 1,
            4, 0,
            5, 0
    );

    private final ApplicationContext applicationContext;
    private final DeepSeekAgentProperties agentProperties;
    private final UserService userService;
    private final AssetService assetService;
    private final BuildingService buildingService;
    private final BillService billService;
    private final VisitorService visitorService;
    private final CarService carService;
    private final CarHistoryService carHistoryService;
    private final FacilityService facilityService;
    private final GateService gateService;
    private final TaskService taskService;

    private Map<String, AgentActionDefinition> actionRegistry;

    @Override
    public SseEmitter chat(AgentChatDTO dto) {
        CurrentUser currentUser = requireCurrentUser();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        CompletableFuture.runAsync(() -> {
            UserContext.setCurrentUser(currentUser);
            try {
                handleChat(dto, emitter);
                send(emitter, "done", Map.of("ok", true));
                emitter.complete();
            } catch (Exception e) {
                log.warn("Agent chat failed", e);
                try {
                    send(emitter, "error", Map.of("message", e.getMessage() == null ? "AI服务暂时不可用" : e.getMessage()));
                } catch (IOException ignored) {
                }
                emitter.complete();
            } finally {
                UserContext.clear();
            }
        });
        return emitter;
    }

    @Override
    public AgentActionVO execute(AgentExecuteDTO dto) {
        requireCurrentUser();
        AgentActionDefinition definition = getDefinition(dto.getActionName());
        if (definition.readOnly || !definition.requiresConfirmation) {
            throw new BusinessException("该动作不需要确认执行");
        }
        assertAllowed(definition);
        Object data = definition.executor.apply(defaultPayload(dto.getPayload()));
        return AgentActionVO.builder()
                .type("action")
                .actionName(definition.actionName)
                .title(definition.title)
                .summary("操作已完成")
                .routePath(definition.routePath)
                .readOnly(false)
                .requiresConfirmation(false)
                .payload(defaultPayload(dto.getPayload()))
                .data(data)
                .build();
    }

    private void handleChat(AgentChatDTO dto, SseEmitter emitter) throws IOException {
        String message = dto == null ? null : dto.getMessage();
        if (!StringUtils.hasText(message)) {
            sendMessage(emitter, "请告诉我你想查询、跳转或操作哪个模块。");
            return;
        }
        ActionPlan plan = buildActionPlan(dto);
        if (plan == null || !StringUtils.hasText(plan.response)) {
            sendMessage(emitter, "我还没有理解你的具体意图，可以换一种说法吗？");
            return;
        }
        streamText(emitter, plan.response);
        if (!StringUtils.hasText(plan.actionName)) {
            return;
        }

        AgentActionDefinition definition = getDefinition(plan.actionName);
        assertAllowed(definition);
        Map<String, Object> payload = defaultPayload(plan.payload);
        if (definition.requiresConfirmation) {
            send(emitter, "pending_action", buildPendingAction(definition, payload, plan.response));
            return;
        }
        Object data = definition.executor.apply(payload);
        send(emitter, "action", AgentActionVO.builder()
                .type(definition.actionName.equals("navigate") ? "navigate" : "show_results")
                .actionName(definition.actionName)
                .title(definition.title)
                .summary(definition.readOnly ? "已完成查询" : "已完成操作")
                .routePath(definition.routePath)
                .readOnly(definition.readOnly)
                .requiresConfirmation(false)
                .payload(payload)
                .data(data)
                .build());
    }

    private AgentActionVO buildPendingAction(AgentActionDefinition definition, Map<String, Object> payload, String summary) {
        return AgentActionVO.builder()
                .type("pending_action")
                .actionName(definition.actionName)
                .title(definition.title)
                .summary(summary)
                .routePath(definition.routePath)
                .readOnly(false)
                .requiresConfirmation(true)
                .payload(payload)
                .build();
    }

    private ActionPlan buildActionPlan(AgentChatDTO dto) {
        ActionPlan plan = tryBuildPlanWithSpringAi(dto);
        if (plan != null) {
            return plan;
        }
        return buildHeuristicPlan(dto.getMessage());
    }

    private ActionPlan tryBuildPlanWithSpringAi(AgentChatDTO dto) {
        try {
            Object builder = findChatClientBuilder();
            Object chatClient = invoke(builder, "build");
            Object prompt = invoke(chatClient, "prompt");
            prompt = invoke(prompt, "system", buildSystemPrompt());
            prompt = invoke(prompt, "user", buildUserPrompt(dto));
            Object call = invoke(prompt, "call");
            Object content = invoke(call, "content");
            if (content instanceof String text && StringUtils.hasText(text)) {
                return parsePlan(text);
            }
        } catch (Exception e) {
            log.debug("Spring AI ChatClient is not available or did not return a valid plan: {}", e.getMessage());
        }
        return null;
    }

    private Object findChatClientBuilder() {
        if (applicationContext.containsBean("chatClientBuilder")) {
            return applicationContext.getBean("chatClientBuilder");
        }
        return applicationContext.getBeansOfType(Object.class).values().stream()
                .filter(bean -> bean.getClass().getName().contains("ChatClient")
                        && bean.getClass().getName().contains("Builder"))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Spring AI ChatClient未配置"));
    }

    private Object invoke(Object target, String methodName, Object... args) throws Exception {
        Method matched = null;
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(methodName)
                    && method.getParameterCount() == args.length
                    && parametersMatch(method.getParameterTypes(), args)) {
                matched = method;
                break;
            }
        }
        if (matched == null) {
            throw new NoSuchMethodException(methodName);
        }
        return matched.invoke(target, args);
    }

    private boolean parametersMatch(Class<?>[] parameterTypes, Object[] args) {
        for (int i = 0; i < parameterTypes.length; i++) {
            if (args[i] != null && !parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    private String buildSystemPrompt() {
        return """
                你是社区物业管理系统的安全 Agent。你只能输出 JSON，不要输出 Markdown。
                JSON 格式为 {"response":"给用户看的中文回复","actionName":"动作名或空","payload":{...}}。
                所有写操作只生成计划，不能说已经完成，前端会让用户确认。
                如果用户要求删除、批量删除、修改密码、退出登录、删除角色、删除类型、上传、导出或绕过权限，actionName 必须为空，并明确拒绝。
                如果参数不足，actionName 必须为空，并追问缺少的字段。
                允许动作名：
                navigate,
                search_users, search_assets, search_buildings, search_bills, search_my_unpaid_bills,
                search_visitors, search_cars, search_car_history, search_facilities, search_tasks,
                add_asset, update_asset, add_building, update_building, add_bill, update_bill, update_bill_status,
                pay_bill, pay_bill_detail, add_bill_detail, update_bill_detail, add_visitor, update_visitor,
                add_car, update_car, add_car_history, update_car_history, add_facility, update_facility,
                add_gate, update_gate, add_task, update_task, process_task, update_user_status。
                跳转 routePath 必须放在 payload.routePath，且只能使用系统允许的路由。
                """;
    }

    private String buildUserPrompt(AgentChatDTO dto) {
        Map<String, Object> prompt = new LinkedHashMap<>();
        prompt.put("message", dto.getMessage());
        prompt.put("currentPath", dto.getCurrentPath());
        prompt.put("history", dto.getHistory() == null ? List.of() : dto.getHistory());
        prompt.put("defaultPageSize", agentProperties.getPageSize());
        return JSON.toJSONString(prompt);
    }

    private ActionPlan parsePlan(String text) {
        String json = extractJson(text);
        JSONObject object = JSON.parseObject(json);
        Map<String, Object> payload = object.getObject("payload", Map.class);
        return new ActionPlan(
                firstText(object, "response", "message", "content"),
                object.getString("actionName"),
                payload == null ? Map.of() : payload
        );
    }

    private String extractJson(String text) {
        String normalized = text == null ? "" : text.trim();
        if (normalized.startsWith("```")) {
            normalized = normalized
                    .replaceFirst("^```[a-zA-Z]*\\s*", "")
                    .replaceFirst("\\s*```$", "")
                    .trim();
        }
        int start = normalized.indexOf('{');
        int end = normalized.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new BusinessException("AI返回格式错误");
        }
        return normalized.substring(start, end + 1);
    }

    private String firstText(JSONObject object, String... keys) {
        for (String key : keys) {
            String value = object.getString(key);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "我已理解你的请求。";
    }

    private ActionPlan buildHeuristicPlan(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        if (containsAny(message, "删除", "删掉", "批量删除", "密码", "退出", "注销", "导出", "上传")) {
            return new ActionPlan("抱歉，这类操作风险较高，我不能执行删除、修改密码、退出登录、上传或导出类操作。", null, Map.of());
        }
        if (containsAny(message, "跳转", "打开", "进入")) {
            String route = inferRoute(message);
            if (route != null) {
                return new ActionPlan("好的，我为你打开对应页面。", "navigate", Map.of("routePath", route));
            }
        }
        if (containsAny(message, "未支付", "未缴费", "待支付")) {
            return new ActionPlan("我帮你查询未支付账单。", "search_my_unpaid_bills", Map.of());
        }
        if (containsAny(message, "账单", "缴费", "费用")) {
            return new ActionPlan("我帮你查询账单信息。", "search_bills", keywordPayload("billCode", message));
        }
        if (containsAny(message, "车辆", "车牌")) {
            return new ActionPlan("我帮你查询车辆信息。", "search_cars", keywordPayload("carNumber", message));
        }
        if (containsAny(message, "访客")) {
            return new ActionPlan("我帮你查询访客信息。", "search_visitors", keywordPayload("visitorName", message));
        }
        if (containsAny(message, "设施", "设备")) {
            return new ActionPlan("我帮你查询设施信息。", "search_facilities", keywordPayload("facilityName", message));
        }
        if (containsAny(message, "资产", "房产", "停车位")) {
            return new ActionPlan("我帮你查询资产信息。", "search_assets", Map.of());
        }
        if (containsAny(message, "楼栋", "建筑")) {
            return new ActionPlan("我帮你查询楼栋信息。", "search_buildings", Map.of());
        }
        if (containsAny(message, "工单", "报修", "投诉")) {
            return new ActionPlan("我帮你查询工单信息。", "search_tasks", Map.of());
        }
        return new ActionPlan("我可以帮你跳转页面、查询数据，或在你确认后执行添加、编辑、状态更新等安全操作。", null, Map.of());
    }

    private String inferRoute(String message) {
        if (containsAny(message, "用户")) return "/user/list";
        if (containsAny(message, "财务", "账单")) return "/finance/bill";
        if (containsAny(message, "访客")) return "/security/visitor";
        if (containsAny(message, "车辆", "车牌")) return "/security/car";
        if (containsAny(message, "通行")) return "/security/car-history";
        if (containsAny(message, "门岗")) return "/security/gate";
        if (containsAny(message, "设施", "设备")) return "/facility/list";
        if (containsAny(message, "房产", "资产")) return "/asset/property";
        if (containsAny(message, "停车位")) return "/asset/parking";
        if (containsAny(message, "楼栋", "建筑")) return "/asset/building";
        if (containsAny(message, "工单", "报修", "投诉")) return "/task/list";
        if (containsAny(message, "首页", "主页")) return "/welcome";
        return null;
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text != null && text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> keywordPayload(String key, String message) {
        String value = message == null ? "" : message
                .replace("查询", "")
                .replace("搜索", "")
                .replace("帮我", "")
                .trim();
        if (!StringUtils.hasText(value) || value.length() > 30) {
            return Map.of();
        }
        return Map.of(key, value);
    }

    private Map<String, AgentActionDefinition> registry() {
        if (actionRegistry != null) {
            return actionRegistry;
        }
        Map<String, AgentActionDefinition> actions = new LinkedHashMap<>();
        register(actions, read("navigate", "页面跳转", null, payload -> {
            String routePath = asString(payload.get("routePath"));
            if (!SAFE_ROUTES.contains(routePath)) {
                throw new ForbiddenException("AI不能跳转到该页面");
            }
            return Map.of("routePath", routePath);
        }));
        register(actions, read("search_users", "查询用户", "/user/list", 2,
                payload -> userService.pageUsers(pageNum(payload), pageSize(payload),
                        asString(payload.get("uid")), asString(payload.get("userName")),
                        asString(payload.get("userPhone")), asString(payload.get("userEmail")),
                        asString(payload.get("contact")), asInteger(payload.get("userRole")),
                        asInteger(payload.get("userStatus")))));
        register(actions, read("search_assets", "查询资产", "/asset/property",
                payload -> assetService.pageAssets(pageNum(payload), pageSize(payload),
                        asLong(payload.get("assetOwnerId")), asString(payload.get("ownerUid")),
                        asString(payload.get("assetCode")), asInteger(payload.get("assetType")),
                        asInteger(payload.get("assetStatus")), asLong(payload.get("assetBuilding")),
                        null, null)));
        register(actions, read("search_buildings", "查询楼栋", "/asset/building",
                payload -> buildingService.pageBuildings(pageNum(payload), pageSize(payload),
                        asString(payload.get("buildingCode")), asString(payload.get("buildingName")),
                        asString(payload.get("buildingLocation")), asString(payload.get("buildingRemark")))));
        register(actions, read("search_bills", "查询账单", "/finance/bill",
                payload -> billService.pageBills(pageNum(payload), pageSize(payload),
                        asLong(payload.get("ownerId")), asInteger(payload.get("billStatus")),
                        asString(payload.get("billCode")), asString(payload.get("ownerUid")),
                        asString(payload.get("createrUid")), null, null)));
        register(actions, read("search_my_unpaid_bills", "查询未支付账单", "/finance/bill",
                payload -> billService.pageMyUnpaidBills(pageNum(payload), pageSize(payload))));
        register(actions, read("search_visitors", "查询访客", "/security/visitor",
                payload -> visitorService.pageVisitors(pageNum(payload), pageSize(payload),
                        asLong(payload.get("ownerId")), asString(payload.get("ownerUid")),
                        asLong(payload.get("assetId")), asInteger(payload.get("visitorType")),
                        asString(payload.get("visitorStatus")), asString(payload.get("visitorName")),
                        null, null)));
        register(actions, read("search_cars", "查询车辆", "/security/car",
                payload -> carService.pageCars(pageNum(payload), pageSize(payload),
                        asLong(payload.get("ownerId")), asString(payload.get("ownerUid")),
                        asString(payload.get("carNumber")), asInteger(payload.get("carType")),
                        asInteger(payload.get("carStatus")))));
        register(actions, read("search_car_history", "查询通行记录", "/security/car-history", new int[]{0, 1, 3}, null,
                payload -> carHistoryService.pageCarHistory(pageNum(payload), pageSize(payload),
                        asString(payload.get("carNumber")), asInteger(payload.get("gateId")),
                        asInteger(payload.get("carHistoryType")), null, null)));
        register(actions, read("search_facilities", "查询设施", "/facility/list", new int[]{0, 1, 2}, null,
                payload -> facilityService.pageFacilities(pageNum(payload), pageSize(payload),
                        asInteger(payload.get("facilityType")), asInteger(payload.get("facilityStatus")),
                        asString(payload.get("facilityCode")), asString(payload.get("facilityName")),
                        asLong(payload.get("facilityBuilding")))));
        register(actions, read("search_tasks", "查询工单", "/task/list",
                payload -> taskService.pageTasks(pageNum(payload), pageSize(payload),
                        asLong(payload.get("ownerId")), asLong(payload.get("taskOperatorId")),
                        asString(payload.get("taskCode")), asInteger(payload.get("taskStatus")),
                        asInteger(payload.get("taskType")), null, null)));

        register(actions, write("add_asset", "新增资产", "/asset/property", new int[]{0, 1, 2}, 1,
                payload -> run(() -> assetService.addAsset(toDto(payload, AssetAddDTO.class)))));
        register(actions, write("update_asset", "编辑资产", "/asset/property", new int[]{0, 1, 2}, 1,
                payload -> run(() -> assetService.updateAsset(toDto(payload, AssetUpdateDTO.class)))));
        register(actions, write("add_building", "新增楼栋", "/asset/building", new int[]{0, 1}, null,
                payload -> run(() -> buildingService.addBuilding(toDto(payload, BuildingAddDTO.class)))));
        register(actions, write("update_building", "编辑楼栋", "/asset/building", new int[]{0, 1}, null,
                payload -> run(() -> buildingService.updateBuilding(toDto(payload, BuildingUpdateDTO.class)))));
        register(actions, write("add_bill", "新增账单", "/finance/bill", new int[]{0, 1, 2}, 1,
                payload -> run(() -> billService.addBill(toDto(payload, BillAddDTO.class)))));
        register(actions, write("update_bill", "编辑账单", "/finance/bill", new int[]{0, 1, 2}, 1,
                payload -> run(() -> billService.updateBill(toDto(payload, BillUpdateDTO.class)))));
        register(actions, write("update_bill_status", "更新账单状态", "/finance/bill", new int[]{0, 1, 2}, 1,
                payload -> run(() -> billService.updateBillStatus(toDto(payload, BillStatusDTO.class)))));
        register(actions, write("pay_bill", "支付账单", "/finance/bill", null, 0,
                payload -> run(() -> billService.payBill(toDto(payload, BillPayDTO.class)))));
        register(actions, write("pay_bill_detail", "支付账单明细", "/finance/bill", null, 0,
                payload -> run(() -> billService.payBillDetail(toDto(payload, BillDetailPayDTO.class)))));
        register(actions, write("add_bill_detail", "新增账单明细", "/finance/bill", new int[]{0, 1, 2}, 1,
                payload -> run(() -> billService.addBillDetail(toDto(payload, BillDetailAddDTO.class)))));
        register(actions, write("update_bill_detail", "编辑账单明细", "/finance/bill", new int[]{0, 1, 2}, 1,
                payload -> run(() -> billService.updateBillDetail(toDto(payload, BillDetailUpdateDTO.class)))));
        register(actions, write("add_visitor", "新增访客", "/security/visitor", new int[]{0, 1, 3}, null,
                payload -> run(() -> visitorService.addVisitor(toDto(payload, VisitorAddDTO.class)))));
        register(actions, write("update_visitor", "编辑访客", "/security/visitor", new int[]{0, 1, 3}, null,
                payload -> run(() -> visitorService.updateVisitor(toDto(payload, VisitorUpdateDTO.class)))));
        register(actions, write("add_car", "新增车辆", "/security/car", new int[]{0, 1, 2}, 1,
                payload -> run(() -> carService.addCar(toDto(payload, CarAddDTO.class)))));
        register(actions, write("update_car", "编辑车辆", "/security/car", new int[]{0, 1, 2}, 1,
                payload -> run(() -> carService.updateCar(toDto(payload, CarUpdateDTO.class)))));
        register(actions, write("add_car_history", "新增通行记录", "/security/car-history", new int[]{0, 1, 3}, null,
                payload -> run(() -> carHistoryService.addCarHistory(toDto(payload, CarHistoryAddDTO.class)))));
        register(actions, write("update_car_history", "编辑通行记录", "/security/car-history", new int[]{0, 1, 3}, null,
                payload -> run(() -> carHistoryService.updateCarHistory(toDto(payload, CarHistoryUpdateDTO.class)))));
        register(actions, write("add_facility", "新增设施", "/facility/list", new int[]{0, 1, 2}, 1,
                payload -> run(() -> facilityService.addFacility(toDto(payload, FacilityAddDTO.class)))));
        register(actions, write("update_facility", "编辑设施", "/facility/list", new int[]{0, 1, 2}, 1,
                payload -> run(() -> facilityService.updateFacility(toDto(payload, FacilityUpdateDTO.class)))));
        register(actions, write("add_gate", "新增门岗", "/security/gate", new int[]{0, 1}, null,
                payload -> run(() -> gateService.addGate(toDto(payload, GateAddDTO.class)))));
        register(actions, write("update_gate", "编辑门岗", "/security/gate", new int[]{0, 1}, null,
                payload -> run(() -> gateService.updateGate(toDto(payload, GateUpdateDTO.class)))));
        register(actions, write("add_task", "新增工单", "/task/list", null, null,
                payload -> run(() -> taskService.addTask(toDto(payload, TaskAddDTO.class)))));
        register(actions, write("update_task", "编辑工单", "/task/list", null, 1,
                payload -> run(() -> taskService.updateTask(toDto(payload, TaskUpdateDTO.class)))));
        register(actions, write("process_task", "处理工单", "/task/list", null, 1,
                payload -> run(() -> taskService.processTask(asLongRequired(payload.get("id")), asInteger(payload.get("status")),
                        asLong(payload.get("operatorId")), asString(payload.get("remark"))))));
        register(actions, write("update_user_status", "更新用户状态", "/user/list", new int[]{0, 1}, null,
                payload -> run(() -> userService.updateUserStatus(toDto(payload, UserStatusDTO.class)))));

        actionRegistry = Collections.unmodifiableMap(actions);
        return actionRegistry;
    }

    private AgentActionDefinition read(String actionName, String title, String routePath, Function<Map<String, Object>, Object> executor) {
        return new AgentActionDefinition(actionName, title, routePath, true, false, null, null, executor);
    }

    private AgentActionDefinition read(String actionName, String title, String routePath, Integer requiredPermissionLevel,
                                       Function<Map<String, Object>, Object> executor) {
        return new AgentActionDefinition(actionName, title, routePath, true, false, null, requiredPermissionLevel, executor);
    }

    private AgentActionDefinition read(String actionName, String title, String routePath, int[] requiredRoles,
                                       Integer requiredPermissionLevel, Function<Map<String, Object>, Object> executor) {
        return new AgentActionDefinition(actionName, title, routePath, true, false, requiredRoles, requiredPermissionLevel, executor);
    }

    private AgentActionDefinition write(String actionName, String title, String routePath, int[] requiredRoles,
                                        Integer requiredPermissionLevel, Function<Map<String, Object>, Object> executor) {
        return new AgentActionDefinition(actionName, title, routePath, false, true, requiredRoles, requiredPermissionLevel, executor);
    }

    private void register(Map<String, AgentActionDefinition> actions, AgentActionDefinition definition) {
        actions.put(definition.actionName, definition);
    }

    private AgentActionDefinition getDefinition(String actionName) {
        AgentActionDefinition definition = registry().get(actionName);
        if (definition == null) {
            throw new ForbiddenException("AI不能执行该操作");
        }
        return definition;
    }

    private void assertAllowed(AgentActionDefinition definition) {
        CurrentUser user = requireCurrentUser();
        if (definition.requiredRoles != null) {
            boolean matched = Arrays.stream(definition.requiredRoles).anyMatch(role -> role == user.getUserRole());
            if (!matched) {
                throw new ForbiddenException("当前用户无权执行该AI操作");
            }
        }
        if (definition.requiredPermissionLevel != null) {
            int level = PERMISSION_MAP.getOrDefault(user.getUserRole(), 0);
            if (level < definition.requiredPermissionLevel) {
                throw new ForbiddenException("当前用户权限不足");
            }
        }
    }

    private CurrentUser requireCurrentUser() {
        CurrentUser user = UserContext.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("未登录");
        }
        return user;
    }

    private Map<String, Object> defaultPayload(Map<String, Object> payload) {
        return payload == null ? new LinkedHashMap<>() : new LinkedHashMap<>(payload);
    }

    private int pageNum(Map<String, Object> payload) {
        Integer pageNum = asInteger(payload.get("pageNum"));
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int pageSize(Map<String, Object> payload) {
        Integer pageSize = asInteger(payload.get("pageSize"));
        if (pageSize == null || pageSize < 1) {
            return agentProperties.getPageSize();
        }
        return Math.min(pageSize, 20);
    }

    private <T> T toDto(Map<String, Object> payload, Class<T> dtoClass) {
        return JSON.parseObject(JSON.toJSONString(payload), dtoClass);
    }

    private Object run(Runnable runnable) {
        runnable.run();
        return Map.of("success", true);
    }

    private Long asLong(Object value) {
        if (value == null || "".equals(value)) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.valueOf(String.valueOf(value));
    }

    private Long asLongRequired(Object value) {
        Long result = asLong(value);
        if (result == null) {
            throw new BusinessException("缺少必要参数 id");
        }
        return result;
    }

    private Integer asInteger(Object value) {
        if (value == null || "".equals(value)) return null;
        if (value instanceof Number number) return number.intValue();
        return Integer.valueOf(String.valueOf(value));
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private void streamText(SseEmitter emitter, String text) throws IOException {
        int chunkSize = 8;
        for (int i = 0; i < text.length(); i += chunkSize) {
            sendMessage(emitter, text.substring(i, Math.min(i + chunkSize, text.length())));
        }
    }

    private void sendMessage(SseEmitter emitter, String text) throws IOException {
        send(emitter, "message", Map.of("content", text));
    }

    private void send(SseEmitter emitter, String event, Object data) throws IOException {
        emitter.send(SseEmitter.event().name(event).data(data));
    }

    private record ActionPlan(String response, String actionName, Map<String, Object> payload) {
    }

    private record AgentActionDefinition(
            String actionName,
            String title,
            String routePath,
            boolean readOnly,
            boolean requiresConfirmation,
            int[] requiredRoles,
            Integer requiredPermissionLevel,
            Function<Map<String, Object>, Object> executor
    ) {
    }
}
