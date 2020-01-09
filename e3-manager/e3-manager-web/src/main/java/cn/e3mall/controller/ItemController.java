package cn.e3mall.controller;

import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 商品管理Controller
 */
@Controller
public class ItemController {
    @Autowired
    private ItemService itemService;

    @RequestMapping("/item/{itemId}")
    @ResponseBody
    public TbItem getItemById(@PathVariable Long itemId) {

        TbItem tbItem = itemService.getItemById(itemId);
        System.out.println(itemId);
        return tbItem;
    }

    /**
     * 商品显示功能
     *
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/item/list")
    @ResponseBody
    public EasyUIDataGridResult getItemList(Integer page, Integer rows) {
        EasyUIDataGridResult result = itemService.getItemList(page, rows);
        return result;
    }

    @RequestMapping(value = "/item/save", method = RequestMethod.POST)
    @ResponseBody
    public E3Result addItem(TbItem item, String desc) {

        E3Result result = itemService.addItem(item, desc);
        return result;
    }

    @RequestMapping("/item/update")
    @ResponseBody
    public E3Result updateItem(TbItem item, String desc) {
        E3Result result = itemService.updateItem(item, desc);
        return result;
    }

    @RequestMapping("/item/delete")
    @ResponseBody
    public E3Result deleteItems(Long[] ids) {
        E3Result result = itemService.deleteItems(ids);
        return result;
    }

    @RequestMapping("/item/desc/{itemId}")
    @ResponseBody
    public E3Result getItemDescById(@PathVariable Long itemId) {
        E3Result result = itemService.getItemDescById(itemId);
        return result;
    }

    @RequestMapping("/item/param/{itemId}")
    @ResponseBody
    public E3Result getItemParamById(@PathVariable Long itemId) {
        E3Result result = itemService.getItemDescById(itemId);
        return result;
    }

    @RequestMapping("/item/reshelf")
    @ResponseBody
    public E3Result reshelfItems(Long[] ids) {
        E3Result result = itemService.changeStatusItems(ids, (byte) 1);
        return result;
    }

    @RequestMapping("/item/instock")
    @ResponseBody
    public E3Result instockItems(Long[] ids) {
        E3Result result = itemService.changeStatusItems(ids, (byte) 2);
        return result;
    }
}
