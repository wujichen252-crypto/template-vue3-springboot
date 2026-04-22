package com.template.common.result;

import lombok.Data;
import java.io.Serializable;

@Data
public class ApiResult<T> implements Serializable {

    private Integer code;
    private T data;
    private String msg;
    private String requestId;

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(200);
        result.setData(data);
        result.setMsg("ok");
        result.setRequestId(RequestIdUtil.getCurrentId());
        return result;
    }

    public static <T> ApiResult<T> success() {
        return success(null);
    }

    public static <T> ApiResult<T> error(Integer code, String msg) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setRequestId(RequestIdUtil.getCurrentId());
        return result;
    }

    public static <T> ApiResult<T> error(ResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMsg());
    }
}
