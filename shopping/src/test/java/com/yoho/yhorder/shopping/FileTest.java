package com.yoho.yhorder.shopping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wujiexiang on 16/4/18.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:META-INF/spring/spring-mybatis-datasource.xml",
        "classpath*:META-INF/spring/spring*.xml"})
public class FileTest {

    @Test
    public void testJson()
    {
        String line ="{\"mode\":\"post\",\"params\":{\"data\":\"{\\\"order_code\\\":1618637916,\\\"union_name\\\":\\\"多麦网wap\\\",\\\"unionid\\\":3019}\"},\"url\":\"http://portal.admin.yohobuy.com/api/orderunion/updateunion\"}";
        JSONObject postJson = JSON.parseObject(line);

        System.out.println(postJson);

    }

    @Test
    public void readFile()throws Exception
    {
        List<Pair<String,String>> orders = new ArrayList<>();
        String fileName ="/Users/wujiexiang/Downloads/4.13-3019.txt";

        orders.addAll(readPushedOrderList(fileName));

        fileName ="/Users/wujiexiang/Downloads/4.13-3019-2.txt";

        orders.addAll(readPushedOrderList(fileName));



        fileName ="/Users/wujiexiang/Downloads/4.14-3019.txt";

        orders.addAll(readPushedOrderList(fileName));

        fileName ="/Users/wujiexiang/Downloads/4.14-3019-2.txt";

        orders.addAll(readPushedOrderList(fileName));

        fileName ="/Users/wujiexiang/Downloads/4.14-3017.txt";

        orders.addAll(readPushedOrderList(fileName));

        System.out.println("*****************\r\n");
        StringBuilder builder_3019 = new StringBuilder("");
        StringBuilder builder_3017 = new StringBuilder("");
        for(Pair<String,String> pair:orders)
        {
            if(pair.getValue().equals("3019"))
            {
                builder_3019.append(pair.getKey()).append(",\r\n");
            }else if("3017".equals(pair.getValue()))
            {
                builder_3017.append(pair.getKey()).append(",");
            }
        }

        System.out.println(builder_3019.toString());

        System.out.println("*****************\r\n");

        System.out.println(builder_3017.toString());


    }


    public List<Pair<String,String>> readPushedOrderList(String fileName)throws Exception
    {
        List<Pair<String,String>> pairs = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));

        String line = null;
        while((line = reader.readLine()) != null)
        {
            int index = line.indexOf("{");
            String text = line.substring(index,line.length());

            JSONObject postJson = JSON.parseObject(text);
            JSONObject paramsJson = postJson.getJSONObject("params");
            String dataText = paramsJson.getString("data");

            JSONObject dataJson = JSON.parseObject(dataText);
            pairs.add(Pair.of(dataJson.getString("order_code"),dataJson.getString("unionid")));
        }

        return pairs;
    }
}
