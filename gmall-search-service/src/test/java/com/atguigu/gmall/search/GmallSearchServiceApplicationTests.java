package com.atguigu.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;

import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

    @Reference
    SkuService skuService;      //查询mysql

    @Autowired
    JestClient jestClient;

    @Test
    public void contextLoads() throws IOException {
        search();
    }


    public void search() throws IOException {
        List<PmsSearchSkuInfo> results=new ArrayList<>();

        //Jest的DSL工具
        SearchSourceBuilder sourceBuilder=new SearchSourceBuilder();
        //bool
        BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();
        //filter
        TermQueryBuilder termQueryBuilder=new TermQueryBuilder("skuAttrValueList.valueId","39");
        boolQueryBuilder.filter(termQueryBuilder);
        TermQueryBuilder termQueryBuilder1=new TermQueryBuilder("skuAttrValueList.valueId","43");
        boolQueryBuilder.filter(termQueryBuilder1);
        //must
        MatchQueryBuilder matchQueryBuilder=new MatchQueryBuilder("skuName","小米");
        boolQueryBuilder.must(matchQueryBuilder);
        //query
        sourceBuilder.query(boolQueryBuilder);
        //from
        sourceBuilder.from(0);
        //size
        sourceBuilder.size(50);
        //highlight
        sourceBuilder.highlight(null);

        String DSL=sourceBuilder.toString();
        System.out.println(DSL);

        Search search = new Search.Builder(DSL).addIndex("gmall").addType("PmsSkuInfo").build();
        SearchResult execute = jestClient.execute(search);

        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;
            results.add(source);
        }

        System.out.println(results.size());
    }

    /**
     * 将商品Sku信息从数据库导入到elasticsearch中
     * @throws IOException
     */
    public void put() throws IOException {
        //查询mysql数据
        List<PmsSkuInfo> pmsSkuInfoList;
        pmsSkuInfoList=skuService.getAllSku("61");
        //转化为ES数据结构
        ArrayList<PmsSearchSkuInfo> pmsSearchSkuInfos= new ArrayList<>();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
        }
        //存入ES
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            Index build = new Index.Builder(pmsSearchSkuInfo).index("gmall").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()).build();
            jestClient.execute(build);
        }
    }

}
