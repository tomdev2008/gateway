package com.yoho.yhorder.invoice.webservice.manager;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by chenchao on 2016/6/7.
 */
@Component
public class XStreamManager {
    /**/
    private static XStream xstream;

    @PostConstruct
    public void init(){
        //默认构造，使用XmlFriendlyNameCoder，这是一个大坑，_->__，你能接受吗
        xstream = new XStream(new XppDriver(new NoNameCoder()));
        xstream.autodetectAnnotations(true);
    }

    public static XStream getXstream() {
        return xstream;
    }

}
