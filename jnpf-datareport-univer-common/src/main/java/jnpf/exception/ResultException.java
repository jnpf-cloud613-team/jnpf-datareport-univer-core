package jnpf.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SameTokenInvalidException;
import com.alibaba.fastjson.JSON;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jnpf.base.ActionResult;
import jnpf.base.ActionResultCode;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 10:10
 */
@Slf4j
@Controller
@ControllerAdvice
public class ResultException extends BasicErrorController {

    @Autowired
    private ConfigValueUtil configValueUtil;

    public ResultException(){
        super(new DefaultErrorAttributes(), new ErrorProperties());
    }

    @ResponseBody
    @ExceptionHandler(value = LoginException.class)
    public ActionResult loginException(LoginException e) {
        ActionResult result = ActionResult.fail(ActionResultCode.FAIL.getCode(), e.getMessage());
        result.setData(e.getData());
        return result;
    }

    @ResponseBody
    @ExceptionHandler(value = ImportException.class)
    public ActionResult loginException(ImportException e) {
        ActionResult result = ActionResult.fail(ActionResultCode.FAIL.getCode(), e.getMessage());
        return result;
    }

    /**
     * 自定义异常内容返回
     *
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = DataException.class)
    public ActionResult dataException(DataException e) {
        ActionResult result = ActionResult.fail(ActionResultCode.FAIL.getCode(), e.getMessage());
        return result;
    }

    /**
     * 租户数据库异常
     *
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = TenantDatabaseException.class)
    public ActionResult<String> tenantDatabaseException(TenantDatabaseException e) {
        String msg;
        if(e.getMessage() == null){
            if (configValueUtil.getMultiTenancyUrl().contains("https")) {
                // https 官网提示
                msg = MsgCode.LOG109.get();
            } else {
                msg = MsgCode.LOG110.get();
            }
        }else{
            msg = e.getMessage();
        }
        return ActionResult.fail(ActionResultCode.FAIL.getCode(), msg);
    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ActionResult methodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> map = new HashMap<>(16);
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        for (int i = 0; i < allErrors.size(); i++) {
            String s = allErrors.get(i).getCodes()[0];
            //用分割的方法得到字段名
            String[] parts = s.split("\\.");
            String part1 = parts[parts.length - 1];
            map.put(part1, allErrors.get(i).getDefaultMessage());
        }
        String json = JSON.toJSONString(map);
        ActionResult result = ActionResult.fail(ActionResultCode.VALIDATEERROR.getCode(), json);
        return result;
    }

    @ResponseBody
    @ExceptionHandler(value = WorkFlowException.class)
    public ActionResult workFlowException(WorkFlowException e) {
        if (e.getCode() == 200) {
            List<Map<String, Object>> list = JsonUtil.getJsonToListMap(e.getMessage());
            return ActionResult.success(list);
        } else {
            return ActionResult.fail(e.getMessage());
        }
    }

    @ResponseBody
    @ExceptionHandler(value = WxErrorException.class)
    public ActionResult wxErrorException(WxErrorException e) {
        return ActionResult.fail(e.getError().getErrorCode(), MsgCode.AD103.get());
    }

    @ResponseBody
    @ExceptionHandler(value = ServletException.class)
    public void exception(ServletException e) throws Exception {
        log.error("系统异常:" + e.getMessage(), e);
        throw new Exception();
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ActionResult exception(Exception e) {
        log.error("系统异常:" + e.getMessage(), e);
        if(e instanceof ConnectDatabaseException || e.getCause() instanceof ConnectDatabaseException){
            Throwable t = e;
            if(e.getCause() instanceof ConnectDatabaseException){
                t = e.getCause();
            }
            return ActionResult.fail(ActionResultCode.FAIL.getCode(), t.getMessage());
        }
        return ActionResult.fail(ActionResultCode.FAIL.getCode(), MsgCode.AD102.get());
    }

    /**
     * 权限码异常
     */
    @ResponseBody
    @ExceptionHandler(NotPermissionException.class)
    public ActionResult<Void> handleNotPermissionException(NotPermissionException e) {
        return ActionResult.fail(ActionResultCode.FAIL.getCode(),  MsgCode.AD104.get());
    }

    /**
     * 角色权限异常
     */
    @ResponseBody
    @ExceptionHandler(NotRoleException.class)
    public ActionResult<Void> handleNotRoleException(NotRoleException e) {
        return ActionResult.fail(ActionResultCode.VALIDATEERROR.getCode(), MsgCode.AD104.get());
    }

    /**
     * 认证失败
     */
    @ResponseBody
    @ExceptionHandler(NotLoginException.class)
    public ActionResult<Void> handleNotLoginException(NotLoginException e) {
        return ActionResult.fail(ActionResultCode.SESSIONOVERDUE.getCode(), MsgCode.AD105.get());
    }

    /**
     * 无效认证
     */
    @ResponseBody
    @ExceptionHandler(SameTokenInvalidException.class)
    public ActionResult<Void> handleIdTokenInvalidException(SameTokenInvalidException e) {
        return ActionResult.fail(ActionResultCode.SESSIONOVERDUE.getCode(), MsgCode.AD106.get());
    }

    /**
     * 覆盖默认的JSON响应
     */
    @Override
    @RequestMapping
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);

        if (status == HttpStatus.NOT_FOUND) {
            return new ResponseEntity<>(status);
        }
        return super.error(request);
    }

}
