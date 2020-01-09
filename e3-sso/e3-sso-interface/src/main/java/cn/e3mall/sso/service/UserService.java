package cn.e3mall.sso.service;

import cn.e3mall.common.utils.E3Result;
import cn.e3mall.pojo.TbUser;

public interface UserService {
    E3Result checkData(String param, int type);

    E3Result createUser(TbUser user);

    E3Result login(String username, String password);

    E3Result getUserByToken(String token);

    E3Result removeUserByToken(String token);
}
