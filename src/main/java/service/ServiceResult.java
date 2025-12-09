package service;

/**
 * 通用业务响应结果封装类
 * 用于统一 Service 层向 UI 层返回的数据格式
 * * @param <T> 泛型，表示成功时携带的具体数据类型 (如 Member, Integer, Void 等)
 */
public class ServiceResult<T> {

    // 是否成功
    private boolean success;

    // 提示信息 (成功消息 或 失败原因)
    private String message;

    // 返回的数据 (可选，例如返回新创建的 ID，或者查询到的对象)
    private T data;

    // --- 构造函数 ---
    public ServiceResult() {
    }

    public ServiceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ServiceResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // --- 静态工厂方法 (方便快速调用) ---

    /**
     * 成功，不带数据
     */
    public static <T> ServiceResult<T> success(String msg) {
        return new ServiceResult<>(true, msg);
    }

    /**
     * 成功，带数据 (例如返回刚刚注册的会员对象)
     */
    public static <T> ServiceResult<T> success(String msg, T data) {
        return new ServiceResult<>(true, msg, data);
    }

    /**
     * 失败
     */
    public static <T> ServiceResult<T> failure(String msg) {
        return new ServiceResult<>(false, msg);
    }

    // --- Getter 和 Setter ---

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}