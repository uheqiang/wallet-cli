package org.tron.common.entity;

import lombok.Builder;
import lombok.Getter;
import org.tron.protos.Protocol.PersonalInfo;

import java.util.Map;

/**
 * @author Brian
 * @date 2021/7/2 15:39
 */
@Builder
@Getter
public class AccountInfo {

    private String addressBase58;

    private long createTime;

    /**
     * 本账户发布的TRC10资产ID，默认是1000001
     */
    private String assetIssuedId = "1000001";

    /**
     * 本账户发布的TRC10资产名称
     */
    private String assetIssuedName;

    /**
     * 能量余额
     */
    private long balanceOfEnergy;

    /**
     * 账户个人信息：identity and appId
     */
    private PersonalInfo personalInfo;

    /**
     * 资产集合
     */
    private Map<String,Long> assetV2;

    @Override
    public String toString() {
        return "AccountInfo{" +
                "addressBase58='" + addressBase58 + '\'' +
                ", createTime='" + createTime + '\'' +
                ", assetIssuedId='" + assetIssuedId + '\'' +
                ", assetIssuedName='" + assetIssuedName + '\'' +
                ", balanceOfEnergy=" + balanceOfEnergy +
                ", personalInfo=" + personalInfo +
                ", assetV2=" + assetV2 +
                '}';
    }
}
