package lj.dao;

import lj.model.AuditInfo;

public interface AuditInfoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(AuditInfo record);

    int insertSelective(AuditInfo record);

    AuditInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(AuditInfo record);

    int updateByPrimaryKey(AuditInfo record);
}