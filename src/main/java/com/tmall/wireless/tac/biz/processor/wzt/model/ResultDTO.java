package com.tmall.wireless.tac.biz.processor.wzt.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ResultDTO<T> implements Serializable {

    private static final long serialVersionUID = 6979363470596419332L;

    protected boolean success = true;

    protected String resultCode;

    protected String errorDetail;


    protected T module;

    //保存详细的校验错误信息
    protected Map<String, String> checkErrorInfo = new HashMap<String, String>();

    public ResultDTO() {
    }

    public ResultDTO(T module) {
        this.module = module;
    }


    public ResultDTO(String errorDetail){
        this.success=false;
        this.errorDetail=errorDetail;
    }

    public ResultDTO(T module, boolean success,String resultCode, String errorDetail){
        this.module=module;
        this.success=success;
        this.resultCode=resultCode;
        this.errorDetail=errorDetail;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFail() {
        return !success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
        if (success) {
            this.resultCode = "SUCCESS";
        }
    }

    public void setResultCode(String resultCode) {
        if (resultCode != null && !resultCode.trim().equals("")) {
            this.success = false;
            this.resultCode = resultCode;
        }
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    public void setErrorDetail(String format, Object... args) {
        this.errorDetail = String.format(format, args);
    }

    /**
     * 将两个结果进行合并, 主要是error合并
     *
     * @param result
     */
    public void merge(ResultDTO<?> result) {
        setResultCode(result.getResultCode());
        setErrorDetail(result.getErrorDetail());
        setSuccess(result.isSuccess());
        mergeCheckErrorInfo(result);
    }

    public void mergeCheckErrorInfo(ResultDTO<?> result) {
        if (null != result && result.getCheckErrorInfo() != null && result.getCheckErrorInfo().size() > 0) {
            for (String key : result.getCheckErrorInfo().keySet()) {
                addCheckErrorInfo(key, result.getCheckErrorInfo().get(key));
            }
        }
    }

    public String getCheckErrorDesc() {
        if (checkErrorInfo != null && checkErrorInfo.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String key : checkErrorInfo.keySet()) {
                sb.append(checkErrorInfo.get(key)).append("|");
            }
            return sb.toString();
        }
        return null;
    }

    public T getModule() {
        return module;
    }

    public void setModule(T module) {
        this.module = module;
    }

    public void addCheckErrorInfo(String code, String message) {
        if (checkErrorInfo.containsKey(code)) {
            checkErrorInfo.put(code, checkErrorInfo.get(code) + "|" + message);
        } else {
            checkErrorInfo.put(code, message);
        }
    }

    public void addCheckErrorInfos(Map<String, String> infos) {
        checkErrorInfo.putAll(infos);
    }

    public void setError(String errorCode, String message) {
        if (errorCode != null) {
            this.success = false;
            resultCode = errorCode;
            errorDetail = message;
        }
    }

    public void copyResult(ResultDTO<?> src, ResultDTO<?> tgt) {
        tgt.setError(src.getResultCode(), src.getErrorDetail());
        tgt.setSuccess(src.isSuccess());
    }
    
    public static <T> ResultDTO<T> of() {
        return new ResultDTO<T>();
    }

    public Map<String, String> getCheckErrorInfo() {
        return checkErrorInfo;
    }

    public static <T> ResultDTO<T> of(String format, Object... args) {
        ResultDTO<T> result = ResultDTO.of();
        result.setErrorDetail(format, args);
        return result;
    }

    public static <T> ResultDTO<T> of(ResultDTO resultDTO, T module) {
        ResultDTO<T> result = ResultDTO.of();
        result.merge(resultDTO);
        result.setModule(module);
        return result;
    }
    
    public static <T> ResultDTO<T> fail(String errorCode, String message) {
        ResultDTO<T> result = ResultDTO.of();
        result.setSuccess(false);
        result.setError(errorCode, message);
        return result;
    }

    public static <T> ResultDTO<T> fail(String message) {
        ResultDTO<T> result = ResultDTO.of();
        result.setSuccess(false);
        result.setErrorDetail(message);
        return result;
    }
    
    public static <T> ResultDTO<T> success(T module) {
        ResultDTO<T> result = ResultDTO.of();
        result.setSuccess(true);
        result.setModule(module);
        return result;
    }

    public static <T> ResultDTO<T> getResult() {
        return new ResultDTO<T>();
    }

}