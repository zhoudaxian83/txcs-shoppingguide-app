package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.context;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

public class UserInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**用户ID*/
    @Setter
    private Long userId;

    /**用户nick*/
    @Getter @Setter
    private String userNick;

    /**未登陆用户唯一身份ID*/
    @Getter @Setter
    private String cna;

    /**设备ID*/
    @Getter @Setter
    private String ttid;

    /**请求来源渠道*/
    @Setter@Getter
    private String channel;

    /*public Long getUserId(){
        //如果是压测流量，则强制塞入正式用户ID
        if (RenderUserUtil.isFromTest()) {
            return RenderUserUtil.getPressureTestUserId();
        }
        if(userId == null || !userLogin()){
            return 0L;
        }
        return this.userId;
    }*/

    /*public boolean userLogin(){
        //如果是压测流量，则默认用户是登录状态
        if (RenderUserUtil.isFromTest()) {
            return true;
        }
        return userId != null && userId > 0;
    }*/
}
