package lj.model;

import java.util.Date;

public class UserMsgSetting {
    private Long id;

    private Integer userId;

    private String receiveOffs;

    private String clearPoints;

    private String fetchPoints;

    private Date createTime;

    private Date updateTime;

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

    public String getReceiveOffs() {
        return receiveOffs;
    }

    public void setReceiveOffs(String receiveOffs) {
        this.receiveOffs = receiveOffs == null ? null : receiveOffs.trim();
    }

    public String getClearPoints() {
        return clearPoints;
    }

    public void setClearPoints(String clearPoints) {
        this.clearPoints = clearPoints == null ? null : clearPoints.trim();
    }

    public String getFetchPoints() {
        return fetchPoints;
    }

    public void setFetchPoints(String fetchPoints) {
        this.fetchPoints = fetchPoints == null ? null : fetchPoints.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}