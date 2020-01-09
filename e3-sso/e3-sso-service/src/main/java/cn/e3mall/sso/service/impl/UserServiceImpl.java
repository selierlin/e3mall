package cn.e3mall.sso.service.impl;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.mapper.TbUserMapper;
import cn.e3mall.pojo.TbUser;
import cn.e3mall.pojo.TbUserExample;
import cn.e3mall.sso.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private TbUserMapper userMapper;

    @Autowired
    private JedisClient jedisClient;

    @Value("${SESSION_EXPIRE}")
    private Integer SESSION_EXPIRE;

    @Override
    public E3Result checkData(String param, int type) {
        //1. 从tb_user表中查询数据
        TbUserExample example = new TbUserExample();
        TbUserExample.Criteria criteria = example.createCriteria();
        //2. 查询条件根据参数动态生成
        //1:用户名 2：手机 3：邮箱
        if (type == 1) {
            criteria.andUsernameEqualTo(param);
        } else if (type == 2) {
            criteria.andPhoneEqualTo(param);
        } else if (type == 3) {
            criteria.andEmailEqualTo(param);
        } else {
            return E3Result.build(400, "非法的参数");
        }
        //执行查询
        List<TbUser> list = userMapper.selectByExample(example);
        //3. 判断查询结果，如果查询到数据返回false
        if (list != null && list.size() > 0) {
            return E3Result.ok(false);
        }
        //5. 使用E3result包装并返回
        return E3Result.ok(true);
    }

    @Override
    public E3Result createUser(TbUser user) {
        //1、数据有效性校验
        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())
                || StringUtils.isBlank(user.getPhone())) {
            return E3Result.build(400, "用户数据不完整，注册失败");
        }
        //校验数据类型 1：用户名 2：手机号 3：邮箱
        E3Result result = checkData(user.getUsername(), 1);
        if (!(boolean) result.getData()) {
            return E3Result.build(400, "此用户名已经被占用");
        }
        result = checkData(user.getPhone(), 2);
        if (!(boolean) result.getData()) {
            return E3Result.build(400, "手机号已经被占用");
        }
        //2、补全pojo的属性
        user.setCreated(new Date());
        user.setUpdated(new Date());
        //3、对密码进行MD5加密
        String md5Pass = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        user.setPassword(md5Pass);
        //4、把用户数据插入到数据库中
        userMapper.insert(user);

        //5、返回添加成功
        return E3Result.ok();
    }

    @Override
    public E3Result login(String username, String password) {
        //1、判断用户名和密码是否正确
        //根据用户名查询用户信息
        TbUserExample example = new TbUserExample();
        TbUserExample.Criteria criteria = example.createCriteria();
        criteria.andUsernameEqualTo(username);
        //执行查询
        List<TbUser> list = userMapper.selectByExample(example);
        if (list == null || list.size() == 0) {
            //返回登录失败
            return E3Result.build(400, "用户名或密码错误");
        }
        //取用户信息
        TbUser user = list.get(0);
        //判断密码是否正确
        if (!user.getPassword().equals(DigestUtils.md5DigestAsHex(password.getBytes()))) {
            //2、如果不正确，返回登录失败
            return E3Result.build(400, "用户名或密码错误");
        }
        //3、如果正确则使用UUID生成token
        String token = UUID.randomUUID().toString();
        //把用户信息写入redis，key:token value:用户信息
        //需要注意不可把用户的密码写入
        user.setPassword(null);
        jedisClient.set("SESSION:" + token, JsonUtils.objectToJson(user));
        //5、设置Session的过期时间
        jedisClient.expire("SESSION:" + token, SESSION_EXPIRE);
        //6、把token返回
        return E3Result.ok(token);
    }

    @Override
    public E3Result getUserByToken(String token) {
        //1、根据token到redis中查询用户信息
        String json = jedisClient.get("SESSION:" + token);
        //2、取不到用户信息，就表示用户登录已经过期，重新登录
        if (StringUtils.isBlank(json)) {
            return E3Result.build(201, "用户登录已经过期，请重新登录！");
        }
        //3、取到用户信息并重新设置token的过期时间
        jedisClient.expire("SESSION:" + token, SESSION_EXPIRE);
        //4、返回结果，将user对象包装到E3Result
        TbUser user = JsonUtils.jsonToPojo(json, TbUser.class);
        return E3Result.ok(user);
    }

    @Override
    public E3Result removeUserByToken(String token) {
        jedisClient.del("SESSION:" + token);
        return E3Result.ok();
    }
}
