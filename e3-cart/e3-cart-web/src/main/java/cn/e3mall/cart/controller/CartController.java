package cn.e3mall.cart.controller;

import cn.e3mall.cart.service.CartService;
import cn.e3mall.common.utils.CookieUtils;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbUser;
import cn.e3mall.service.ItemService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {

    @Value("${COOKIE_NAME}")
    private String COOKIE_NAME;
    @Value("${CART_EXPIRE}")
    private Integer CART_EXPIRE;

    @Autowired
    private ItemService itemService;
    @Autowired
    private CartService cartService;


    @RequestMapping("/cart/add/{itemId}")
    public String addCartItem(@PathVariable Long itemId, @RequestParam(defaultValue = "1") Integer num, HttpServletRequest request, HttpServletResponse response) {
        //判断用户是否登录
        TbUser user = (TbUser) request.getAttribute("user");
        //如果是登录状态，把购物车写入redis
        if (user != null) {
            cartService.addCart(user.getId(), itemId, num);
            return "cartSuccess";
        }
        //如果未登录使用cookie保存购物车
        //1、从cookie中查询商品列表
        List<TbItem> cartList = getCartListFromCookie(request);
        boolean hasItem = false;
        //2、判断商品列表中是否存在
        for (TbItem tbItem : cartList) {
            //对象比较的是地址，而这应该是比较值
            if (tbItem.getId() == itemId.longValue()) {
                hasItem = true;
                //如果存在就增加数量
                tbItem.setNum(tbItem.getNum() + num);
                break;
            }
        }

        //如果不存在
        if (!hasItem) {
            //4、根据商品id查询商品信息，得到一个TbItem对象
            TbItem tbItem = itemService.getItemById(itemId);
            //5、根据商品id查询商品信息
            String image = tbItem.getImage();
            if (StringUtils.isNotBlank(image)) {
                tbItem.setImage(image.split(",")[0]);
            }
            //设置购买商品数量
            tbItem.setNum(num);
            //6、将商品添加到商品列表
            cartList.add(tbItem);

        }
        //7、写入cookie
        CookieUtils.setCookie(request, response, COOKIE_NAME, JsonUtils.objectToJson(cartList), CART_EXPIRE, true);
        //8、返回添加成功页面
        return "cartSuccess";
    }

    public List<TbItem> getCartListFromCookie(HttpServletRequest request) {
        //取购物车列表
        String json = CookieUtils.getCookieValue(request, COOKIE_NAME, true);
        //判断json是否为null
        if (StringUtils.isNotBlank(json)) {
            List<TbItem> list = JsonUtils.jsonToList(json, TbItem.class);
            return list;
        }
        return new ArrayList<>();
    }

    @RequestMapping("/cart/cart")
    public String cartList(HttpServletRequest request, HttpServletResponse response, Model model) {
        //从cookie中取购物车商品列表
        List<TbItem> cartList = getCartListFromCookie(request);
        //判断用户是否为登录状态
        TbUser user = (TbUser) request.getAttribute("user");
        //如果是登录状态
        if (user != null) {
            //从cookie中取购物车列表
            //把cookie中的购物车商品和服务端的购物车商品合并
            cartService.mergeCart(user.getId(), cartList);
            //把cookie中的购物车删除
            CookieUtils.deleteCookie(request, response, COOKIE_NAME);
            //从服务端取购物车列表
            cartList = cartService.getCartList(user.getId());
        }

        //传递给页面
        model.addAttribute("cartList", cartList);
        return "cart";
    }

    @RequestMapping("/cart/update/num/{itemId}/{num}")
    @ResponseBody
    public E3Result updateCartNum(@PathVariable Long itemId, @PathVariable Integer num, HttpServletRequest request, HttpServletResponse response) {
        //判断用户是否为登录状态
        TbUser user = (TbUser) request.getAttribute("user");
        if (user != null) {
            E3Result e3Result = cartService.updateCartNum(user.getId(), itemId, num);
            return e3Result;
        }
        //1、接收两个参数
        //2、从cookie中取购物车列表
        List<TbItem> cartList = getCartListFromCookie(request);
        //3、遍历商品列表找到对应的商品
        for (TbItem tbItem : cartList) {
            if (tbItem.getId().longValue() == itemId) {
                //4、更新商品数量
                tbItem.setNum(num);
                break;
            }
        }
        //5、把购物车列表写回cookie
        CookieUtils.setCookie(request, response, COOKIE_NAME, JsonUtils.objectToJson(cartList), CART_EXPIRE, true);
        //6、返回成功
        return E3Result.ok();
    }

    @RequestMapping("/cart/delete/{itemId}")
    public String delCartItemCart(@PathVariable Long itemId, HttpServletRequest request, HttpServletResponse response) {
        //判断用户是否为登录状态
        TbUser user = (TbUser) request.getAttribute("user");
        if (user != null) {
            E3Result e3Result = cartService.deleteCartItem(user.getId(), itemId);
        } else {
            //1、从商品中取商品id
            //2、从cookie中取购物车商品列表
            List<TbItem> cartList = getCartListFromCookie(request);
            //3、遍历列表找到对应的商品
            for (TbItem tbItem : cartList) {
                if (tbItem.getId().longValue() == itemId) {
                    //4、移除商品
                    cartList.remove(tbItem);
                    break;
                }
            }
            //5、把商品列表写入cookie
            CookieUtils.setCookie(request, response, COOKIE_NAME, JsonUtils.objectToJson(cartList), CART_EXPIRE, true);
        }
        //6、返回逻辑视图：在逻辑视图中做redirect跳转
        return "redirect:/cart/cart.html";
    }
}
