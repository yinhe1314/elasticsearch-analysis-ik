package org.wltea.analyzer.dic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.Consts;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.SpecialPermission;
import org.wltea.analyzer.help.ESPluginLoggerFactory;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: TODO 词典监控
 * @Author: sun
 * @Date: 2021/1/26 14:24
 */
public class DictMonitor implements Runnable {

    private static final Logger logger = ESPluginLoggerFactory.getLogger(DictMonitor.class.getName());

    private static CloseableHttpClient httpclient = HttpClients.createDefault();

    /*
     * 请求地址
     */
    private String location;
    /*
     * 上次更改时间
     */
    private String modifyTime;
    /*
     * 词典key
     */
    private String dictKey;

    public DictMonitor(String location, String dictKey) {
        this.location = location;
        this.modifyTime = null;
        this.dictKey = dictKey;
    }

    public void run() {
        SpecialPermission.check();
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            this.runUnprivileged();
            return null;
        });
    }

    /**
     * 监控流程：
     *  ①向服务器发送请求
     *  ②从响应中获取是否有值
     *  ③如果没值，休眠1min，返回第①步
     * 	④如果有值，重新加载词典
     *  ⑤休眠1min，返回第①步
     */
    public void runUnprivileged() {
        // 添加请求参数
        List<BasicNameValuePair> paramList = new ArrayList<>();
        if (modifyTime != null && !"".equals(modifyTime)) {
            paramList.add(new BasicNameValuePair("modifyTime", modifyTime));
        }

        // 超时设置
        RequestConfig requestConfig  = RequestConfig.custom()
                .setConnectionRequestTimeout(10*1000)
                .setConnectTimeout(10*1000)
                .setSocketTimeout(15*1000)
                .build();

        // 转换get参数
        String url = location;
        if (!paramList.isEmpty()) {
            String covertParams = "";
            try {
                covertParams = EntityUtils.toString(new UrlEncodedFormEntity(paramList, Consts.UTF_8));
            }catch (IOException e) {
                logger.error("GET 请求参数转换异常!", e);
            }
            url = location + "?" + covertParams;
        }
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);

        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);
            String resultJson = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
            logger.info("请求 {} 结果 {}", url, resultJson);
            JSONObject jsonObject = JSON.parseObject(resultJson);
            String data = jsonObject.getString("data");
            if (data != null && !"".equals(data)) {
                JSONObject dataJSONObject = JSON.parseObject(data);
                String listStr = dataJSONObject.getString("list");
                List<String> dictList = JSONArray.parseArray(listStr, String.class);
                // 重新加载词典，再保存最新时间
                Dictionary.getSingleton().reLoadDict(dictKey, dictList);
                modifyTime = dataJSONObject.getString("modifyTime");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("请求 {} error!", url);
        }finally{
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
