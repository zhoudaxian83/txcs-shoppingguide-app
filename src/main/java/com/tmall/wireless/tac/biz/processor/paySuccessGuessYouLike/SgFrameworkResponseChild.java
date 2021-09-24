package com.tmall.wireless.tac.biz.processor.paySuccessGuessYouLike;

import com.tmall.tcls.gs.sdk.framework.model.EntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import lombok.Data;

@Data
public class SgFrameworkResponseChild<T extends EntityVO> extends SgFrameworkResponse {

    public boolean minimumGuarantee;

    public boolean isMinimumGuarantee() {
        return minimumGuarantee;
    }

    public void setMinimumGuarantee(boolean minimumGuarantee) {
        this.minimumGuarantee = minimumGuarantee;
    }
}
