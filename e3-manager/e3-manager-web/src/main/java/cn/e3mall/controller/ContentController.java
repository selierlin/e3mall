package cn.e3mall.controller;

import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.content.service.ContentService;
import cn.e3mall.pojo.TbContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ContentController {

    @Autowired
    private ContentService contentService;

    @RequestMapping("/content/list")
    @ResponseBody
    public EasyUIDataGridResult getContentList(@RequestParam(name = "categoryId", defaultValue = "0") Long categoryId, Integer page, Integer rows) {
        EasyUIDataGridResult contentList = contentService.getContentList(categoryId, page, rows);
        return contentList;
    }

    @RequestMapping(value = "/content/save", method = RequestMethod.POST)
    @ResponseBody
    public E3Result addContent(TbContent content) {
        E3Result result = contentService.addContent(content);
        return result;
    }

    @RequestMapping(value = "/content/edit", method = RequestMethod.POST)
    @ResponseBody
    public E3Result editContent(TbContent content) {
        E3Result result = contentService.editContent(content);
        return result;
    }

    @RequestMapping(value = "/content/delete", method = RequestMethod.POST)
    @ResponseBody
    public E3Result editContent(Long[] ids) {
        E3Result result = contentService.deleteContents(ids);
        return result;
    }
}
