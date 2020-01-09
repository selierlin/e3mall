package cn.e3mall.solrJ;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import java.io.IOException;

public class TestSolrCloud {
    private String zkHost = "192.168.222.131:2181,192.168.222.131:2182,192.168.222.131:2183";

    @Test
    public void testAddDocument() throws IOException, SolrServerException {
        // 创建一个集群的连接，应该使用CloudSolrServer创建。
        // zkHost：zookeeper的地址列表
        CloudSolrServer solrServer = new CloudSolrServer(zkHost);
        // 设置一个defaultCollection属性。
        solrServer.setDefaultCollection("collection2");
        // 创建一个文档对象
        SolrInputDocument document = new SolrInputDocument();
        // 向文档中添加域
        document.setField("id", "solrcloud01");
        document.setField("item_title", "商品");
        document.setField("item_price", 100);
        // 把文件写入索引库
        solrServer.add(document);
        // 提交
        solrServer.commit();
    }

    @Test
    public void testQueryDocument() throws SolrServerException {
        // 创建一个集群的连接，应该使用CloudSolrServer创建。
        // zkHost：zookeeper的地址列表
        CloudSolrServer solrServer = new CloudSolrServer(zkHost);
        solrServer.setDefaultCollection("collection2");

        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");

        QueryResponse queryResponse = solrServer.query(query);
        SolrDocumentList results = queryResponse.getResults();
        System.out.println("查询结果总记录数："+results.getNumFound());
        for (SolrDocument result : results) {
            System.out.println(result);
        }


    }
}
