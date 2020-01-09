package cn.e3mall.content.service;

import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.pojo.TbContent;

import java.util.List;

public interface ContentService {
    /**
     * 通过cid分页查询
     *
     * @param categoryId
     * @param page
     * @param rows
     * @return
     */
    EasyUIDataGridResult getContentList(long categoryId, int page, int rows);

    /**
     * 添加内容
     *
     * @param content
     * @return
     */
    E3Result addContent(TbContent content);

    /**
     * 根据cid查询相应的所有内容
     * 用于前台页面展示
     *
     * @param cid
     * @return
     */
    List<TbContent> getContentListByCid(long cid);

    /**
     * 编辑内容
     *
     * @param content
     * @return
     */
    E3Result editContent(TbContent content);

    /**
     * 删除多个内容
     *
     * @param ids
     * @return
     */
    E3Result deleteContents(Long[] ids);
}
