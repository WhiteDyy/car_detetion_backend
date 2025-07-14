package cn.dhbin.isme.common.enums;

public enum JobStatus {
    /**
     * 作业未开始
     */
    NOT_STARTED("not_started", "未开始"),

    /**
     * 作业进行中
     */
    IN_PROGRESS("in_progress", "进行中"),

    /**
     * 作业已完成
     */
    COMPLETED("completed", "已完成"),

    /**
     * 作业已取消
     */
    CANCELLED("cancelled", "已取消");

    private final String code;
    private final String description;

    JobStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    // 根据code获取枚举
    public static JobStatus fromCode(String code) {
        for (JobStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的状态码: " + code);
    }
}
