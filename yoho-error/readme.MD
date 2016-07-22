# 错误码定义

错误码定义在下面的目录下：`src/main/resources`. 预先定义了如下的yml文件：服务的异常分开在不同的文件中
  - gateway-error.yml
  - service-error.yml
  - service-error-*.yml

分别用来表示`API Gateway` 和 服务内部的错误码。 注意，错误码需要用`UTF-8`来编码，否则会出错。

# 如何添加新的错误码
1. 先在yml文件中添加错误码（注意使用 `UTF-8`编码）
2. 然后在对应的枚举类型`Java`类中添加对应的枚举类型。例如：

```
public enum GatewayError implements  ErrorCode {
    PROFILE_IS_NULL(421),
    PASSWORD_IS_NULL(422);

    PASSWORD_IS_INVALIDATE(424);
    }
```

# 使用说明
1.先在maven中添加依赖：

```
 <dependency>
         <groupId>com.yoho.common</groupId>
         <artifactId>error-code</artifactId>
 </dependency>
 
```

2.在你的spring context中拉起这个`src/resources/META-INF/spring/spring-error.xml`

3.然后可以直接使用如下错误码的枚举类型
- `com.yoho.error.GatewayError`
- `com.yoho.error.ServiceError`

# 服务的异常码自动映射到gateway的异常
服务的异常码可以自动映射到gateway，可以通过下面的配置实现：

```yml

300000001:
  message: "request error"
  gwError: 22222:消息不能为空

```

其中，`gwError`定义了输出到gateway的异常（这个异常信息会通过json发送给app）。用`:`分割异常码和消息



# 服务间错误码的规范
`[100][001][001]` 模块编码+子模块编码+错误码， 例如 `001001002`

## 模块编码
 - `100`: 用户(Passport)
 - `200`: 购物车
 - `300`: 资源(Resource)
 - `400`: 订单
 - `500`: 产品
 - `600`: 消息(Message)
 - `700`: 促销(Promotion)
 
### 子模块编码
 - 用户001：  auth(`001`), vip(`002`), profile(`003`), sso(`004`)
