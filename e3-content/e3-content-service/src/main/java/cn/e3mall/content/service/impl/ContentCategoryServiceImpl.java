package cn.e3mall.content.service.impl;

import cn.e3mall.common.pojo.EasyUITreeNode;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.content.service.ContentCategoryService;
import cn.e3mall.mapper.TbContentCategoryMapper;
import cn.e3mall.pojo.TbContentCategory;
import cn.e3mall.pojo.TbContentCategoryExample;
import cn.e3mall.pojo.TbContentCategoryExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ContentCategoryServiceImpl implements ContentCategoryService {

    @Autowired
    private TbContentCategoryMapper tbContentCategoryMapper;

    @Override
    public List<EasyUITreeNode> getContentCatList(long parentId) {
        //1. 取查询参数 id,parentId
        //2. 根据parentId查询 tb_content_category 查询子节点列表
        TbContentCategoryExample example = new TbContentCategoryExample();
        Criteria criteria = example.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        //执行查询

        //3. 得到 List<TbContentCatetory>
        List<TbContentCategory> list = tbContentCategoryMapper.selectByExample(example);

        //4. 把列表转换成List<EasyUITreeNode>
        ArrayList<EasyUITreeNode> resultList = new ArrayList<>();
        for (TbContentCategory tbContentCategory : list) {
            EasyUITreeNode node = new EasyUITreeNode();
            node.setId(tbContentCategory.getId());
            node.setState(tbContentCategory.getIsParent() ? "closed" : "open");
            node.setText(tbContentCategory.getName());
            //添加到列表
            resultList.add(node);

        }
        return resultList;

    }

    @Override
    public E3Result addContentCategory(long parentId, String name) {
        TbContentCategory tbContentCategory = new TbContentCategory();
        tbContentCategory.setParentId(parentId);
        tbContentCategory.setName(name);
        //1-正常 2-删除
        tbContentCategory.setStatus(1);
        tbContentCategory.setSortOrder(1);
        //新增加的节点一定是叶子节点
        tbContentCategory.setIsParent(false);
        Date date = new Date();
        tbContentCategory.setCreated(date);
        tbContentCategory.setUpdated(date);

        tbContentCategoryMapper.insert(tbContentCategory);

        //判断父节点的isParent属性，如果不是true则改为true
        //根据parendId查询父节点
        TbContentCategory parent = tbContentCategoryMapper.selectByPrimaryKey(parentId);
        if (!parent.getIsParent()) {
            parent.setIsParent(true);
            //更新到数据库中
            tbContentCategoryMapper.updateByPrimaryKey(parent);
        }
        return E3Result.ok(tbContentCategory);
    }

    @Override
    public E3Result updateContentCategory(Long id, String name) {
        TbContentCategory tbContentCategory = tbContentCategoryMapper.selectByPrimaryKey(id);
        tbContentCategory.setName(name);
        tbContentCategoryMapper.updateByPrimaryKey(tbContentCategory);
        return E3Result.ok();
    }

    @Override
    public E3Result deleteContentCategory(Long id) {
        //判断是否为父节点
        TbContentCategory tbContentCategory = tbContentCategoryMapper.selectByPrimaryKey(id);

        //如果是父节点，判断是否有子节点，如果没有子节点则删除，如果有子节点则提示不删除
        if (tbContentCategory.getIsParent()) {
            TbContentCategoryExample tbContentCategoryExample = new TbContentCategoryExample();
            Criteria criteria = tbContentCategoryExample.createCriteria();
            criteria.andParentIdEqualTo(id);
            int i = tbContentCategoryMapper.countByExample(tbContentCategoryExample);
            if (i > 0) {
                return E3Result.build(400, "不可以删除包含子节点");
            }
        }
        tbContentCategoryMapper.deleteByPrimaryKey(id);
        Long parentId = tbContentCategory.getParentId();
        //获取此节点的父节点，判断该父节点的子节点是否大于一，如果小于则将该父节点设置为非父节点
        if (parentId != 0) {
            TbContentCategory parentContentCategory = tbContentCategoryMapper.selectByPrimaryKey(parentId);
            TbContentCategoryExample example = new TbContentCategoryExample();
            Criteria criteria1 = example.createCriteria();
            criteria1.andParentIdEqualTo(parentId);
            int i1 = tbContentCategoryMapper.countByExample(example);
            if (i1 <= 1) {
                parentContentCategory.setIsParent(false);
                tbContentCategoryMapper.updateByPrimaryKey(parentContentCategory);
            }
        }
        return E3Result.ok();
    }
}
