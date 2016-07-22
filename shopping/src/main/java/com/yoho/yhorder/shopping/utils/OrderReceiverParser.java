package com.yoho.yhorder.shopping.utils;

import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.response.AreaRspBo;
import com.yoho.service.model.response.UserAddressRspBO;
import com.yoho.yhorder.shopping.model.OrderReceiver;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by wujiexiang on 16/5/24.
 */
public class OrderReceiverParser {
    private OrderReceiverParser() {

    }


    private static boolean isYohoodSaleChannel(String saleChannel) {
        return "yohood".equals(saleChannel);
    }


    private static boolean isInValidAddressId(Integer addressId) {
        return !isValidAddressId(addressId);
    }


    public static boolean isValidAddressId(Integer addressId) {
        return addressId != null && addressId > 0;
    }

    public static boolean isYohoodSite(String saleChannel, Integer addressId) {
        return isYohoodSaleChannel(saleChannel) && isInValidAddressId(addressId);
    }


    public static OrderReceiver parseUserAddressRspBO(UserAddressRspBO userAddressRspBO) {

        if (userAddressRspBO == null || MyStringUtils.isEmpty(userAddressRspBO.getAreaCode())) {
            throw new ServiceException(ServiceError.SHOPPING_ORDER_ADDRESS_IS_NULL);
        }
        // 默认值为"",不然erp订单校验会异常
        String address = "", areaCodeTemp = "", zipCode = "", consigneeName = "", phone = "", mobile = "", province = "", city = "", county = "", email = "";
        OrderReceiver receiver = new OrderReceiver();
        if (userAddressRspBO != null) {
            if (StringUtils.isNotEmpty(userAddressRspBO.getAddress())) {
                address = userAddressRspBO.getAddress();
            }
            if (StringUtils.isNotEmpty(userAddressRspBO.getAreaCode()) && !Constants.NULL_STR.equals(userAddressRspBO.getAreaCode())) {
                areaCodeTemp = userAddressRspBO.getAreaCode();
            }

            if (StringUtils.isNotEmpty(userAddressRspBO.getZipCode()) && !Constants.NULL_STR.equals(userAddressRspBO.getZipCode())) {
                zipCode = userAddressRspBO.getZipCode();
            }

            if (StringUtils.isNotEmpty(userAddressRspBO.getAddresseeName())) {
                consigneeName = userAddressRspBO.getAddresseeName();
            }

            if (StringUtils.isNotEmpty(userAddressRspBO.getPhone())) {
                phone = userAddressRspBO.getPhone();
            }

            if (StringUtils.isNotEmpty(userAddressRspBO.getMobile())) {
                mobile = userAddressRspBO.getMobile();
            }

            //
            if (StringUtils.isNotEmpty(userAddressRspBO.getEmail())) {
                email = userAddressRspBO.getEmail();
            }

            //province
            String areaCode = userAddressRspBO.getAreaCode();
            AreaRspBo areaRspBo = userAddressRspBO.getArea();
            if (areaCode.endsWith("0000")) {
                //后四位为0，表示为省
                province = areaRspBo.getCaption() == null ? "" : areaRspBo.getCaption();
            } else if (areaCode.endsWith("00")) {
                //后两位为0，表示为市
                AreaRspBo parentArea = areaRspBo.getParent();
                if (parentArea != null) {
                    province = parentArea.getCaption() == null ? "" : parentArea.getCaption();
                }
                city = areaRspBo.getCaption() == null ? "" : areaRspBo.getCaption();
            } else {
                county = areaRspBo.getCaption() == null ? "" : areaRspBo.getCaption();
                AreaRspBo cityArea = areaRspBo.getParent();
                if (cityArea != null) {
                    city = cityArea.getCaption() == null ? "" : cityArea.getCaption();
                    AreaRspBo provinceArea = cityArea.getParent();
                    if (provinceArea != null) {
                        province = provinceArea.getCaption() == null ? "" : provinceArea.getCaption();
                    }
                }
            }
        }

        receiver.setAddress(address);
        receiver.setAreaCode(areaCodeTemp);
        receiver.setZipCode(zipCode);
        receiver.setConsigneeName(consigneeName);
        receiver.setPhone(phone);
        receiver.setMobile(mobile);
        receiver.setProvince(province);
        receiver.setCity(city);
        receiver.setDistrict(county);
        receiver.setEmail(email);
        return receiver;
    }


}
