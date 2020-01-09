package cn.e3mall.solrJ;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class TestSolrJ {

    @Test
    public void addDocument() throws Exception {
        //创建一个SolrServer对象，创建一个连接。参数so1r服务的url
        SolrServer solrServer = new HttpSolrServer("http://192.168.222.131:8080/solr/collection1");
        //创建一个文档对象So1rInputDocument
        SolrInputDocument document = new SolrInputDocument();
        //向文档对象中添加域。文档中必须包含一个id域，所有的域的名称必须在schema.xml中定义。
        document.addField("id", "doc01");
        document.addField("item_title", "测试商品");
        document.addField("item_price", "1000");

        //把文档写入索引库
        solrServer.add(document);
        //提交
        solrServer.commit();

    }

    @Test
    public void queryDocument() throws SolrServerException {
        //创建一个SolrServer对象
        SolrServer solrServer = new HttpSolrServer("http://192.168.222.131:8080/solr/collection1");
        //创建一个SolrQuery对象
        SolrQuery query = new SolrQuery();
        //向SolrQuery添加查询条件、过滤条件
        //solrQuery.setQuery("*:*");
        query.set("q", "*:*");
        //执行查询。得到一个Response对象
        QueryResponse queryResponse = solrServer.query(query);
        //取查询结果
        SolrDocumentList solrDocumentList = queryResponse.getResults();
        System.out.println("查询结果的总记录数：" + solrDocumentList.getNumFound());
        //遍历结果并打印
        for (SolrDocument solrDocument : solrDocumentList) {
            System.out.println(solrDocument.get("id"));
            System.out.println(solrDocument.get("item_title"));
            System.out.println(solrDocument.get("item_price"));

        }

    }

    @Test
    public void queryDocumentFuza() throws SolrServerException {
        SolrServer solrServer = new HttpSolrServer("http://192.168.222.131:8080/solr/collection1");
        SolrQuery query = new SolrQuery();
        query.setQuery("手机");
        query.setStart(0);
        query.setRows(10);
        query.set("df", "item_title");
        query.setHighlight(true);
        query.addHighlightField("item_title");
        query.setHighlightSimplePre("<em>");
        query.setHighlightSimplePost("</em>");

        QueryResponse queryResponse = solrServer.query(query);
        SolrDocumentList solrDocumentList = queryResponse.getResults();
        System.out.println("查询结果的总记录数：" + solrDocumentList.getNumFound());

        Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
        for (SolrDocument solrDocument : solrDocumentList) {
            System.out.println(solrDocument.get("id"));

            List<String> list = highlighting.get(solrDocument.get("id")).get("item_title");
            String itemTitle = "";
            if (list != null && list.size() > 0) {
                itemTitle = list.get(0);
            } else {
                itemTitle = (String) solrDocument.get("item_title");
            }

            System.out.println(itemTitle);
            System.out.println(solrDocument.get("item_price"));

        }
    }
}
