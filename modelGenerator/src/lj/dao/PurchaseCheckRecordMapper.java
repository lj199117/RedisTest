package lj.dao;

import lj.model.PurchaseCheckRecord;

public interface PurchaseCheckRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PurchaseCheckRecord record);

    int insertSelective(PurchaseCheckRecord record);

    PurchaseCheckRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PurchaseCheckRecord record);

    int updateByPrimaryKey(PurchaseCheckRecord record);
}