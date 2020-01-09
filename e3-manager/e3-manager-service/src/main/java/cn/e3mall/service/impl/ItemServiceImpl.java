package cn.e3mall.service.impl;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.IDUtils;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.mapper.TbItemDescMapper;
import cn.e3mall.mapper.TbItemMapper;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbItemDesc;
import cn.e3mall.pojo.TbItemExample;
import cn.e3mall.pojo.TbItemExample.Criteria;
import cn.e3mall.service.ItemService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jms.*;
import java.util.Date;
import java.util.List;

/**
 * 商品管理Service
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemDescMapper itemDescMapper;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Resource
    private Destination topicDestination;
    @Autowired
    private JedisClient jedisClient;
    @Value("${REDIS_ITEM_PRE}")
    private String REDIS_ITEM_PRE;
    @Value("${ITEM_CACHE_EXPIRE}")
    private Integer ITEM_CACHE_EXPIRE;


    @Override
    public TbItem getItemById(long itemId) {
        try {
            //查询缓存
            String json = jedisClient.get(REDIS_ITEM_PRE + ":" + itemId + ":BASE");
            if (StringUtils.isNotBlank(json)) {
                //把json转换为java对象
                TbItem item = JsonUtils.jsonToPojo(json, TbItem.class);
                return item;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //根据主键查询
        //TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
        TbItemExample example = new TbItemExample();
        Criteria criteria = example.createCriteria();
        //设置查询条件
        criteria.andIdEqualTo(itemId);
        //执行查询
        List<TbItem> list = itemMapper.selectByExample(example);
        if (list != null && list.size() > 0) {
            TbItem item = list.get(0);
            try {
                jedisClient.set(REDIS_ITEM_PRE + ":" + itemId + ":BASE", JsonUtils.objectToJson(item));
                jedisClient.expire(REDIS_ITEM_PRE + ":" + itemId + ":BASE", ITEM_CACHE_EXPIRE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return item;
        }
        return null;
    }

    @Override
    public TbItemDesc getItemDescByitemId(long itemId) {
        try {
            String json = jedisClient.get(REDIS_ITEM_PRE + ":" + itemId + ":DESC");
            //判断缓存是否命中
            if (StringUtils.isNotBlank(json)) {
                TbItemDesc itemDesc = JsonUtils.jsonToPojo(json, TbItemDesc.class);
                return itemDesc;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        TbItemDesc itemDesc = itemDescMapper.selectByPrimaryKey(itemId);

        try {
            jedisClient.set(REDIS_ITEM_PRE + ":" + itemId + ":DESC", JsonUtils.objectToJson(itemDesc));
            //设置过期时间
            jedisClient.expire(REDIS_ITEM_PRE + ":" + itemId + ":DESC", ITEM_CACHE_EXPIRE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemDesc;
    }


    @Override
    public EasyUIDataGridResult getItemList(int page, int rows) {
        //设置分页信息
        PageHelper.startPage(page, rows);
        //执行查询
        TbItemExample example = new TbItemExample();
        List<TbItem> list = itemMapper.selectByExample(example);//此时查询出来的数据有rows条
        //创建一个返回值对象
        EasyUIDataGridResult result = new EasyUIDataGridResult();
        result.setRows(list);
        //取分页结果
        PageInfo<TbItem> pageInfo = new PageInfo<>(list);
        //取总记录数
        long total = pageInfo.getTotal();
        result.setTotal(total);
        return result;
    }

    @Override
    public E3Result addItem(TbItem tbItem, String desc) {
        //1. 生成商品ID
        final long itemId = IDUtils.genItemId();
        //2. 补全TbItem对象的属性
        tbItem.setId(itemId);
        //商品状态：1-正常，2-下架，3-删除
        tbItem.setStatus((byte) 1);

        Date date = new Date();
        tbItem.setCreated(date);
        tbItem.setUpdated(date);
        //3. 向商品表插入数据
        itemMapper.insert(tbItem);
        //4. 创建一个TbItemDesc对象
        TbItemDesc tbItemDesc = new TbItemDesc();
        //5. 补全TbItemDesc对象的属性
        tbItemDesc.setItemId(itemId);
        tbItemDesc.setItemDesc(desc);
        tbItemDesc.setCreated(date);
        tbItemDesc.setUpdated(date);
        //6. 向商品描述表插入数据
        itemDescMapper.insert(tbItemDesc);

        //发送一个商品添加消息
        jmsTemplate.send(topicDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage(itemId + "");
                return message;
            }
        });
        //7. 返回200
        return E3Result.ok(null);
    }

    @Override
    public E3Result updateItem(TbItem tbItem, String desc) {
        final TbItem itemById = getItemById(tbItem.getId());
        final Long itemId = itemById.getId();
        Date date = new Date();
        tbItem.setUpdated(date);
        tbItem.setCreated(itemById.getCreated());
        itemMapper.updateByPrimaryKey(tbItem);

        final TbItemDesc tbItemDesc = new TbItemDesc();

        tbItemDesc.setItemId(tbItem.getId());
        tbItemDesc.setItemDesc(desc);
        tbItemDesc.setCreated(itemById.getCreated());
        tbItemDesc.setUpdated(date);

        itemDescMapper.updateByPrimaryKey(tbItemDesc);

        try {
            jedisClient.set(REDIS_ITEM_PRE + ":" + itemId + ":BASE", JsonUtils.objectToJson(tbItem));
            jedisClient.set(REDIS_ITEM_PRE + ":" + itemId + ":DESC", JsonUtils.objectToJson(tbItemDesc));
            jedisClient.expire(REDIS_ITEM_PRE + ":" + itemId + ":BASE", ITEM_CACHE_EXPIRE);
            jedisClient.expire(REDIS_ITEM_PRE + ":" + itemId + ":DESC", ITEM_CACHE_EXPIRE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //发送一个商品添加消息
        jmsTemplate.send(topicDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage(itemId + "");
                return message;
            }
        });

        return E3Result.ok(null);
    }

    @Override
    public E3Result getItemDescById(Long itemId) {
        TbItemDesc tbItemDesc = itemDescMapper.selectByPrimaryKey(itemId);
        return E3Result.ok(tbItemDesc);
    }


    @Override
    public E3Result deleteItems(Long[] ids) {
        for (Long id : ids) {
            itemMapper.deleteByPrimaryKey(id);
            itemDescMapper.deleteByPrimaryKey(id);

            try {
                jedisClient.del(REDIS_ITEM_PRE + ":" + id + ":BASE");
                jedisClient.del(REDIS_ITEM_PRE + ":" + id + ":DESC");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return E3Result.ok();
    }

    @Override
    public E3Result changeStatusItems(Long[] ids, Byte status) {
        for (Long id : ids) {
            TbItem tbItem = getItemById(id);
            tbItem.setStatus(status);
            itemMapper.updateByPrimaryKey(tbItem);

            try {
                jedisClient.del(REDIS_ITEM_PRE + ":" + id + ":BASE");
                jedisClient.del(REDIS_ITEM_PRE + ":" + id + ":DESC");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return E3Result.ok();
    }

}
