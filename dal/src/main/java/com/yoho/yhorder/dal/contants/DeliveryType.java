package com.yoho.yhorder.dal.contants;

/**
 * Created by lijian on 2015/12/5.
 */
public enum  DeliveryType {

        TYPE_SEND(10,"寄回换货","Y"),TYPE_DOOR(20,"上门换货(白金会员专享服务)","N");

        private String type;
        private int id;
        private String defaultType;

        private DeliveryType(int id,String type,String defaultType){
            this.id=id;
            this.type=type;
            this.defaultType=defaultType;
        }


        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getDefaultType() {
            return defaultType;
        }

        public void setDefaultType(String defaultType) {
            this.defaultType = defaultType;
        }
}
