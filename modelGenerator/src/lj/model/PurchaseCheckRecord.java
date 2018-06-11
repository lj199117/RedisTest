package lj.model;

import java.util.Date;

public class PurchaseCheckRecord {
    private Long id;

    private Integer userId;

    private String transId;

    private String innerItemId;

    private String thirdItemId;

    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId == null ? null : transId.trim();
    }

    public String getInnerItemId() {
        return innerItemId;
    }

    public void setInnerItemId(String innerItemId) {
        this.innerItemId = innerItemId == null ? null : innerItemId.trim();
    }

    public String getThirdItemId() {
        return thirdItemId;
    }

    public void setThirdItemId(String thirdItemId) {
        this.thirdItemId = thirdItemId == null ? null : thirdItemId.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}