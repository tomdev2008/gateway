package com.yoho.yhorder.common.message;

import com.alibaba.fastjson.JSON;
import com.yoho.error.event.RabbitmqSentEvent;
import org.apache.commons.codec.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.util.Map;

/**
 * 微信商城消息生产者专用
 * @author maelk-liu
 * fork by {@link com.yoho.core.message.YhProducerTemplate}
 * 
 */
public class WechatProducerTemplate implements ApplicationEventPublisherAware{

    private final Logger logger = LoggerFactory.getLogger(WechatProducerTemplate.class);

    private AmqpTemplate amqpTemplate;

    final String TOPIC_EXCHAGE = "amq.topic";
    private ApplicationEventPublisher publisher;


    /**
     * 延迟发送消息
     * @param topic 消息的主题
     * @param object  消息对象，框架会自动转化为JSON
     * @param attributes 消息属性
     * @param delayInMinutes 延迟多少分钟，必须和consumers配置的保持一致
     */
    public void send(String topic, Object object,  Map<String, Object> attributes, int delayInMinutes){
        //yoho_delay.2m.topic
        String sent_topic = "yoho_delay." + delayInMinutes + "m." + topic;
        this.send(sent_topic, object, attributes);
    }

    /**
     * 发送消息
     * @param  topic 消息主题
     * @param object 消息体，java对象
     */
    public void send(String topic, Object object)
    {
        this.send(topic, object, null);
    }

    /**
     * 发送消息
     * @param  topic 消息主题
     * @param object 消息体 会转化为JSON，然后发送
     * @param attributes 消息属性
     */
    public void send(String topic, Object object, Map<String, Object> attributes)
    {
        //消息的属性
        MessageProperties properties = new MessageProperties();
        properties.setContentType("text");
        if(attributes != null) {
            for(Map.Entry<String, Object> entry : attributes.entrySet()) {
                properties.setHeader(entry.getKey(), entry.getValue());
            }
        }
        //消息体
        byte[] body = JSON.toJSONString(object).getBytes(Charsets.toCharset("UTF-8"));
        Message amqpMsg = new Message(body, properties);


        this.amqpTemplate.send(TOPIC_EXCHAGE, topic, amqpMsg);


        //发送事件
        this.publisher.publishEvent(new RabbitmqSentEvent(topic, object, attributes));

        logger.debug("send mq message success. exchange:{}, topic:{}, message:{}",  TOPIC_EXCHAGE, topic, object);

    }


    //spring setting, do not call
    public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }



}
