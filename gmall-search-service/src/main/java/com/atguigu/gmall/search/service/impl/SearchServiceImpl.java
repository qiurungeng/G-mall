package com.atguigu.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsSearchParam;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@CrossOrigin
public class SearchServiceImpl implements SearchService {

    @Autowired
    JestClient jestClient;

    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) {
        List<PmsSearchSkuInfo> results=new ArrayList<>();

        String DSL=getSearchDSL(pmsSearchParam);
//        System.err.println(DSL);
        Search search = new Search.Builder(DSL).addIndex("gmall").addType("PmsSkuInfo").build();

        SearchResult execute = null;
        try {
            execute = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;
            //替换为搜索高亮字段
            Map<String, List<String>> highlight = hit.highlight;
            if (highlight!=null){
                String skuName=highlight.get("skuName").get(0);
                source.setSkuName(skuName);
            }
            //添加到查询结果列表
            results.add(source);
        }
        return results;
    }

    /**
     * 由传入搜索参数得到DSL
     * @param pmsSearchParam 传入搜索参数
     * @return DSL
     */
    private String getSearchDSL(PmsSearchParam pmsSearchParam){
        String[] skuAttrValueList = pmsSearchParam.getValueId();
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        //Jest的DSL工具
        SearchSourceBuilder sourceBuilder=new SearchSourceBuilder();
        //bool
        BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();
        //filter
        if (StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder termQueryBuilder=new TermQueryBuilder("catalog3Id",catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if (skuAttrValueList!=null){
            for (String pmsSkuAttrValue : skuAttrValueList) {
                TermQueryBuilder termQueryBuilder=new TermQueryBuilder("skuAttrValueList.valueId",pmsSkuAttrValue);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        //must
        if (StringUtils.isNotBlank(keyword)){
            MatchQueryBuilder matchQueryBuilder=new MatchQueryBuilder("skuName",keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }
        //query
        sourceBuilder.query(boolQueryBuilder);
        //from
        sourceBuilder.from(0);
        //size
        sourceBuilder.size(100);
        //highlight
        HighlightBuilder highlightBuilder=new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red;'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlight(highlightBuilder);
        //sort
        sourceBuilder.sort("productId", SortOrder.DESC);

        TermsBuilder groupby_attr= AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        sourceBuilder.aggregation(groupby_attr);

        return sourceBuilder.toString();
    }
}
