package org.tron.walletcli;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.tron.common.entity.AccountInfo;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.core.exception.DecodingException;
import org.tron.core.exception.TronException;
import org.tron.protos.Protocol;
import org.tron.walletserver.WalletApi;

import java.io.IOException;
import java.util.Map;

/**
 * @author Brian
 * @date 2021/6/18 10:35
 */
@Slf4j
public class WalletClient {

    private WalletApiWrapper walletApiWrapper = new WalletApiWrapper();

    private final static String ASSET_ID = "1000001";

    /**
     * 首先，商家登陆
     */
    public boolean login() throws IOException, CipherException {
        boolean result = walletApiWrapper.login();
        if (result) {
            logger.info("Login successful !!!");
        } else {
            logger.info("Login failed !!!");
        }
        return result;
    }

    /**
     * 发布TRC10资产
     * @param ownerBase58 发布者，即资产拥有者
     * @param name 资产名称
     * @param totalSupply 发布总量
     */
    public boolean assetIssue(String ownerBase58, String name, long totalSupply)
            throws IOException, CipherException, CancelException {
        byte[] addressBytes = WalletApi.decodeFromBase58Check(ownerBase58);
        return walletApiWrapper.assetIssue(addressBytes,name,totalSupply);
    }

    /**
     * 转移TRC10资产
     * @param fromBase58 发送者
     * @param toBase58  接收者
     * @param assetId 资产ID
     * @param amount 转移数量
     */
    public boolean assetTransfer(String fromBase58, String toBase58, String assetId, long amount)
            throws CipherException, IOException, CancelException {
        byte[] from = WalletApi.decodeFromBase58Check(fromBase58);
        byte[] to = WalletApi.decodeFromBase58Check(toBase58);
        if (StringUtils.isEmpty(assetId)) {
            assetId = ASSET_ID;
        }
        return walletApiWrapper.transferAsset(from,to,assetId,amount);
    }

    /**
     * 查询用户的资产余额
     * @param addressBase58 用户地址
     */
    public long queryTrc10Balance(String addressBase58) {
        try {
            AccountInfo account = getAccount(addressBase58);
            Map<String, Long> assetV2 = account.getAssetV2();
            String assetId = ASSET_ID;
            if (!assetV2.isEmpty() && assetV2.containsKey(assetId)) {
                return assetV2.get(assetId);
            }
        } catch (TronException e) {
            logger.error("Account[{}] query asset balance failed !!!", addressBase58);
        }
        return 0L;
    }

    /**
     * 查询账户信息：账户地址、账户发布的资产、账户资产余额、账户能量余额
     */
    public AccountInfo getAccount(String addressBase58) throws TronException {
        AccountInfo.AccountInfoBuilder builder = AccountInfo.builder();
        byte[] addressBytes = WalletApi.decodeFromBase58Check(addressBase58);
        if (addressBytes == null) {
            logger.error("Address[{}] decode failed", addressBase58);
            throw new DecodingException("Address decode failed");
        }

        Protocol.Account account = WalletApi.queryAccount(addressBytes);
        if (account == null) {
            logger.error("Query account failed !!!!");
            return builder.build();
        } else {
            return builder.addressBase58(addressBase58)
                    .createTime(account.getCreateTime())
                    .assetIssuedId(account.getAssetIssuedID().toStringUtf8())
                    .assetIssuedName(account.getAssetIssuedName().toStringUtf8())
                    .balanceOfEnergy(account.getAccountResource().getFrozenBalanceForEnergy().getFrozenBalance())
                    .personalInfo(account.getPersonalInfo())
                    .assetV2(account.getAssetV2Map()).build();
        }
    }

    /**
     * 兑换能量
     * @param ownerBase58 兑换者
     * @param frozenBalance 用于兑换能量的TRC10资产的数量
     */
    public boolean freezeBalanceForMe(String ownerBase58, long frozenBalance)
            throws CipherException, IOException, CancelException {
        ByteString assertId = ByteString.copyFromUtf8(ASSET_ID);
        return freezeBalance(ownerBase58,frozenBalance,assertId,ownerBase58);
    }

    private boolean freezeBalance(String ownerBase58, long frozenBalance,
                                  ByteString assertId, String receiverBase58)
            throws IOException, CipherException, CancelException {
        byte[] owner = WalletApi.decodeFromBase58Check(ownerBase58);
        byte[] receiver = WalletApi.decodeFromBase58Check(receiverBase58);
        return walletApiWrapper.freezeBalance(owner,frozenBalance,assertId,receiver);
    }


    /**
     * 为用户注册链上账户
     */
    public void createAccount()
            throws CipherException, IOException, CancelException {

    }

    // 商家发布NFT，部署合约
    public void deployContract()
            throws IOException, CipherException, CancelException {
    }

    // 调用合约转账NFT
}
