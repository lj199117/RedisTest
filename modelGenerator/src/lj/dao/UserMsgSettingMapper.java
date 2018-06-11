package lj.dao;

import lj.model.UserMsgSetting;

public interface UserMsgSettingMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserMsgSetting record);

    int insertSelective(UserMsgSetting record);

    UserMsgSetting selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserMsgSetting record);

    int updateByPrimaryKey(UserMsgSetting record);
}