package cn.e3mall.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class FreemarkerTest {
    @Test
    public void genFile() throws Exception {
        //第一步：创建一个Configuration对象，直接new一个对象，构造方法的参数就是Freemarker对应的版本号。
        Configuration configuration = new Configuration(Configuration.getVersion());
        //第二步：设置模板文件所在的路径
        configuration.setDirectoryForTemplateLoading(new File("D:\\idea\\e3mall\\e3-item-web\\src\\main\\webapp\\WEB-INF\\ftl"));
        //第三步：设置模板文件使用的字符集，一般就是utf-8
        configuration.setDefaultEncoding("utf-8");
        //第四步：加载一个模板，创建一个模板对象
        Template template = configuration.getTemplate("hello.ftl");
        //第五步：创建一个模板使用的数据集，可以是Pojo也可以是map，一般是map
        Map dataModel = new HashMap<>();
        //向数据集中添加数据
        dataModel.put("hello", "this is my first freemarker test.");
        //第六步：创建一个Writer对象，一般创建一个FileWrier对象，指定生成的文件名
        Writer out = new FileWriter("D:\\idea\\e3mall\\freemarker\\hello.html");
        //第七步：调用模板对象的process方法输出文件
        template.process(dataModel, out);
        //第八步：关闭流
        out.close();


    }
}
