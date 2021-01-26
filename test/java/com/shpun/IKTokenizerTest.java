package com.shpun;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.common.settings.Settings;
import org.junit.Test;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;

/**
 * @Description:
 * @Author: sun
 * @Date: 2021/1/26 15:10
 */
public class IKTokenizerTest {

    @Test
    public void testAnalyzer() throws IOException {
        Settings settings =  Settings.builder()
                .put("use_smart", false)
                .put("enable_lowercase", false)
                .put("enable_remote_dict", false)
                .put("dict_url", "http://localhost:9502/search/openapi/sys/searcher/indexAnalyzerItem/list/1")
                .build();
        Configuration configuration = new Configuration(null,settings) ;
        IKAnalyzer ik = new IKAnalyzer(configuration);

        String t = "美媒曝光特朗普离任后第一天动态";
        TokenStream tokenStream = ik.tokenStream("", new StringReader(t));
        tokenStream.reset();
        CharTermAttribute termAtt = tokenStream.addAttribute(CharTermAttribute.class);
        while(tokenStream.incrementToken()){
            System.out.println(termAtt);
        }
        tokenStream.end();
        tokenStream.close();
    }

}
