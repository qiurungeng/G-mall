package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class SearchController {
    @Reference
    SearchService searchService;
    @Reference
    AttrService attrService;

    @RequestMapping("/")
    public String index(){
        return "index";
    }

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap){
        //调用搜索服务，返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList",pmsSearchSkuInfos);

        //抽取检索结果所包含的平台属性集合
        Set<String> valueIdSet=new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId=pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }
        //根据valueId将属性列表查询出来
        List<PmsBaseAttrInfo> pmsBaseAttrInfos=attrService.getAttrValueListByValueId(valueIdSet);
        modelMap.put("attrList",pmsBaseAttrInfos);

        //剔除已选中属性以构造属性列表，同时将已选中属性制成面包屑
        String[] delValueIds=pmsSearchParam.getValueId();       //已被选中的属性值
        if (delValueIds!=null){
            //面包屑集
            List<PmsSearchCrumb> pmsSearchCrumbs=new ArrayList<>();
            for (String delValueId : delValueIds) {
                Iterator<PmsBaseAttrInfo> iterator=pmsBaseAttrInfos.iterator();
                PmsSearchCrumb pmsSearchCrumb=new PmsSearchCrumb();
                //面包屑参数
                pmsSearchCrumb.setValueId(delValueId);                                              //面包屑的属性值（id）
                pmsSearchCrumb.setUrlParam(getUrlParam(pmsSearchParam,delValueId));         //面包屑的跳转链接参数
                while (iterator.hasNext()){
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    //从属性全集中剔除已选中的属性
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        if (pmsBaseAttrValue.getId().equals(delValueId)){
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());           //面包屑的属性值名称
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
            //将面包屑集返回到前台
            modelMap.put("attrValueSelectedList",pmsSearchCrumbs);
        }
        String urlParam=getUrlParam(pmsSearchParam);
        //将已剔除选中属性的属性列表返回到前台
        modelMap.put("urlParam",urlParam);

        String keyword=pmsSearchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)){
            modelMap.put("keyword",keyword);
        }

        return "list";
    }

    //由请求参数构造请求链接url
    private String getUrlParam(PmsSearchParam pmsSearchParam,String ...delValueId) {
        String keyword=pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();
        StringBuilder urlParam= new StringBuilder();

        //keyword和catalog3Id中必有一个不为空
        if (StringUtils.isNotBlank(keyword)){
            if (StringUtils.isNotBlank(urlParam.toString())){
                urlParam.append("&");
            }
            urlParam.append("keyword=").append(keyword);
        }
        if (StringUtils.isNotBlank(catalog3Id)){
            if (StringUtils.isNotBlank(urlParam.toString())){
                urlParam.append("&");
            }
            urlParam.append("catalog3Id").append(catalog3Id);
        }
        if (skuAttrValueList!=null){
            for (String pmsSkuAttrValue : skuAttrValueList) {
                if (delValueId.length>0){
                    //面包屑的请求url的参数，该url需剔除被点击面包屑所包含的属性值
                    if (!pmsSkuAttrValue.equals(delValueId[0])){
                        urlParam.append("&valueId=").append(pmsSkuAttrValue);
                    }
                }else {
                    //普通请求url的参数
                    urlParam.append("&valueId=").append(pmsSkuAttrValue);
                }
            }
        }
        return urlParam.toString();
    }
}
