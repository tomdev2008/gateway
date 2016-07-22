package com.yoho.error;

import java.text.MessageFormat;
import java.util.Map;

/**
 *
 *  Gateway的错误码定义
 * Created by chang@yoho.cn on 2015/11/3.
 */
public enum GatewayError implements  ErrorCode {



    // ----------------------- begin profile error ---------------------//

	CODE_SUCCESS(200),
	
//    PROFILE_IS_NULL(421),
//    PASSWORD_IS_NULL(422),
    PASSWORD_NOT_RULE(423),
    
    SEND_ERROR(425),
    SEND_SUCCESS(200),
    
    //第三方登录code开始
//    SOURCE_TYPE_IS_NULL(502),
    LOGIN_ERROR(503),
    OTHER_ERROR(412),
//    NOT_BIND(201),
  //第三方登录code结束
    
    UPDATE_MOBILE_ERROR(411),
    UPDATE_PASSWORD_ERROR(420),
    PLEASE_INPUT_PASSWORD(413),
    REGISTER_ERROR(414),

//    PROFILE_MUST_BE_MOBILE_OR_EMAIL(10001),
    PASSWORD_ERROR(10012),
    USER_NOT_EXISTS(10010),
    
    //以下为 address code
    BIND_SUCCESS(200),//200
    ADDRESS_LIST(200),//200
    ADDRESS_ID_ERROR(452),//500
    USER_ID_ERROR(453),//500
    ADDRESSEENAME_ERROR(454),//500
    ADDRESS_NULL(455),//500
    PROVINCE_MUST(456),//500
    MOBILE_PHONE_ONE(457),//500
    ADD_SUCCESS(200),//200
    ADD_FALSE(458),//404
    UPDATE_SUCCESS(200),//200
    ID_UID_NULL(459),//500
    DEL_SUCCESS(200),//200
    DEL_FALSE(460),//404
    PROVINCE_LIST(200),//200
    ID_IS_NULL(461),//405
    SET_SUCCESS(200),//200
    SET_FALSE(462),//201
    
    PHONE_UPDATE_SUCCESS(200),//200
    PHONE_UPDATE_FALSE(464),//409


    
    
//    GET_USER_SSO_INFO_ERROR(601),
//    SSO_LOGIN_INFO_NULL(602),

    
    UID_MAIL_MOBILE_MUST_NOT_NULL(603),
    GET_SSO_INFO_ERROR(604),
    SSO_UPDATE_ERROR(605),

    SHOPPING_ACTION_SUCCESS(200),//200
    REQUEST_PAREMENT_ERROR(413),





    ///*************** 通用的异常 ****************************
    //未映射的服务异常
    SERVICE_ERROR(998),
    SERVICE_NOT_AVAILBLE(999),
    
    
    //**************************************ERP BEGIN*******************************
    SERVICE_ID_NOT_AVAILBLE(1000)
    //**************************************ERP END*******************************
    ;



    //-------------------------------------/


    static String DEFAULT_ERROR_MSG = "操作失败";
    static int DEFAULT_ERROR_CODE = 400;

    private final int code;
    private String message;

    GatewayError(int code)
    {
        this.code=code;
    }


    /**
     * 根据code查找错误
     * @param code code
     * @return error info
     */
    public static GatewayError getGatewayErrorByCode(int code){
       for(GatewayError error : GatewayError.values()){
           if(error.code == code){
               return error;
           }
       }
        return null;
    }



    @Override
    public int getCode() {
        return code;
    }

    public String getMessage(Object ... param) {
        if( param != null && param.length > 0){
            return MessageFormat.format(this.message, param);
        }else {
            return message;
        }
    }

    @Override
    public void setErrorContent(Map<String, Object> content) {

        this.message = (String)content.get("message");
    }


    /**
     * 直接调用 {@link GatewayError#getMessage(Object...)}
     *
     * @param param 参数
     * @return 格式化之后的消息
     */
    @Deprecated
    public String getFormattedMessage(Object ... param){
        return MessageFormat.format(this.message, param);
    }
}
