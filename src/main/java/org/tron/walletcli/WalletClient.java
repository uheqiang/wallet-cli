package org.tron.walletcli;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.Strings;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.entity.AccountInfo;
import org.tron.common.utils.AbiUtil;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.ByteUtil;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.core.exception.DecodingException;
import org.tron.core.exception.TronException;
import org.tron.protos.Protocol;
import org.tron.walletserver.WalletApi;

import java.io.IOException;
import java.util.*;

/**
 * @author Brian
 * @date 2021/6/18 10:35
 */
@Slf4j
public class WalletClient {

    private WalletApiWrapper walletApiWrapper = new WalletApiWrapper();

    private final static String ASSET_ID = "1000001";

    /**
     * 发布TRC10资产
     * @param ownerBase58 发布者，即资产拥有者
     * @param ownerPriKey owner的私钥
     * @param name 资产名称
     * @param totalSupply 发布总量
     */
    public boolean assetIssue(String ownerBase58, String ownerPriKey, String name, long totalSupply)
            throws IOException, CipherException, CancelException {
        byte[] addressBytes = WalletApi.decodeFromBase58Check(ownerBase58);
        byte[] keyBytes = ByteArray.fromHexString(ownerPriKey);
        return walletApiWrapper.assetIssue(addressBytes,keyBytes,name,totalSupply);
    }

    /**
     * 发布TRC10资产
     * @param owner 发布者，即资产拥有者
     * @param ownerKey owner的私钥
     * @param name 资产名称
     * @param totalSupply 发布总量
     */
    public boolean assetIssue(byte[] owner, byte[] ownerKey, String name, long totalSupply)
            throws IOException, CipherException, CancelException {
        return walletApiWrapper.assetIssue(owner,ownerKey,name,totalSupply);
    }

    /**
     * 转移TRC10资产
     * @param fromBase58 发送者
     * @param fromPrivateKey 发送者的私钥
     * @param toBase58  接收者
     * @param assetId 资产ID
     * @param amount 转移数量
     */
    public boolean assetTransfer(String fromBase58, String fromPrivateKey, String toBase58, String assetId, long amount)
            throws CipherException, IOException, CancelException {
        byte[] from = WalletApi.decodeFromBase58Check(fromBase58);
        byte[] to = WalletApi.decodeFromBase58Check(toBase58);
        byte[] keyBytes = ByteArray.fromHexString(fromPrivateKey);
        return assetTransfer(from,keyBytes,to,assetId,amount);
    }

    /**
     * 转移TRC10资产
     * @param from 发送者
     * @param fromPrivateKey 发送者的私钥
     * @param to  接收者
     * @param assetId 资产ID
     * @param amount 转移数量
     */
    public boolean assetTransfer(byte[] from, byte[] fromPrivateKey, byte[] to, String assetId, long amount)
            throws CipherException, IOException, CancelException {
        if (StringUtils.isEmpty(assetId)) {
            assetId = ASSET_ID;
        }
        return walletApiWrapper.transferAsset(from, fromPrivateKey, to, assetId, amount);
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
     * 查询用户的资产余额
     * @param address 用户地址
     */
    public long queryTrc10Balance(byte[] address) {
        try {
            AccountInfo account = getAccount(address);
            Map<String, Long> assetV2 = account.getAssetV2();
            String assetId = ASSET_ID;
            if (!assetV2.isEmpty() && assetV2.containsKey(assetId)) {
                return assetV2.get(assetId);
            }
        } catch (TronException e) {
            String addressBase58 = WalletApi.encode58Check(address);
            logger.error("Account[{}] query asset balance failed !!!", addressBase58);
        }
        return 0L;
    }

    /**
     * 查询账户信息：账户地址、账户发布的资产、账户资产余额、账户能量余额
     * @param addressBase58 账户地址
     */
    public AccountInfo getAccount(String addressBase58) throws TronException {
        byte[] addressBytes = WalletApi.decodeFromBase58Check(addressBase58);
        return getAccount(addressBytes);
    }

    /**
     * 查询账户信息：账户地址、账户发布的资产、账户资产余额、账户能量余额
     * @param ownerAddress 账户地址
     */
    public AccountInfo getAccount(byte[] ownerAddress) throws TronException {
        AccountInfo.AccountInfoBuilder builder = AccountInfo.builder();
        String addressBase58 = WalletApi.encode58Check(ownerAddress);
        if (ownerAddress == null) {
            logger.error("Address[{}] decode failed", addressBase58);
            throw new DecodingException("Address decode failed");
        }

        Protocol.Account account = WalletApi.queryAccount(ownerAddress);
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
     * @param ownerAddress 兑换者
     * @param ownerPrivateKey 兑换者的私钥
     * @param frozenBalance 用于兑换能量的TRC10资产的数量
     */
    public boolean freezeBalanceForMyself(byte[] ownerAddress, byte[] ownerPrivateKey, long frozenBalance)
            throws IOException, CipherException, CancelException {
        ByteString assertId = ByteString.copyFromUtf8(ASSET_ID);
        return walletApiWrapper.freezeBalance(ownerAddress,ownerPrivateKey,frozenBalance,assertId,ownerAddress);
    }

    /**
     * 兑换能量
     * @param ownerBase58 兑换者
     * @param ownerPrivateKey 兑换者的私钥
     * @param frozenBalance 用于兑换能量的TRC10资产的数量
     */
    public boolean freezeBalanceForMyself(String ownerBase58, String ownerPrivateKey, long frozenBalance)
            throws CipherException, IOException, CancelException {
        ByteString assertId = ByteString.copyFromUtf8(ASSET_ID);
        return freezeBalance(ownerBase58,ownerPrivateKey,frozenBalance,assertId,ownerBase58);
    }

    private boolean freezeBalance(String ownerBase58, String ownerPrivateKey, long frozenBalance,
                                  ByteString assertId, String receiverBase58)
            throws IOException, CipherException, CancelException {
        byte[] owner = WalletApi.decodeFromBase58Check(ownerBase58);
        byte[] receiver = WalletApi.decodeFromBase58Check(receiverBase58);
        byte[] keyBytes = ByteArray.fromHexString(ownerPrivateKey);
        return walletApiWrapper.freezeBalance(owner,keyBytes,frozenBalance,assertId,receiver);
    }


    /**
     * 为用户注册链上账户
     * @param owner 商家或可信节点的地址
     * @param ownerPrivateKey 商家或可信节点的私钥
     * @param address 用户地址
     * @param identity 用户身份（hash值）
     * @return 交易发送结果
     */
    public boolean createAccount(byte[] owner, byte[] ownerPrivateKey, byte[] address, String identity)
            throws CipherException, IOException, CancelException {
        return walletApiWrapper.createAccount(owner, ownerPrivateKey, address, identity);
    }

    /**
     * 为用户注册链上账户
     * @param ownerBase58 商家或可信节点的地址
     * @param ownerPrivateKey 商家或可信节点的私钥
     * @param addressBase58 用户地址
     * @param identity 用户身份（hash值）
     * @return 交易发送结果
     */
    public boolean createAccount(String ownerBase58, String ownerPrivateKey, String addressBase58, String identity)
            throws CipherException, IOException, CancelException {
        byte[] owner = WalletApi.decodeFromBase58Check(ownerBase58);
        byte[] address = WalletApi.decodeFromBase58Check(addressBase58);
        byte[] keyBytes = ByteArray.fromHexString(ownerPrivateKey);
        return walletApiWrapper.createAccount(owner, keyBytes, address, identity);
    }

    /**
     * 创建商家
     */
    //todo 设计逻辑待优化
    public boolean createBusiness()
            throws IOException, CipherException, CancelException {
        return walletApiWrapper.createBusiness();
    }

    /**
     * 商家发布NFT，部署合约
     * @param name NFT名称
     * @param symbol NFT代表符号
     * @param ownerBase58 NFT发布者
     */
    public boolean deployContract(String name, String symbol, String ownerBase58, String ownerPrivateKey)
            throws CipherException, IOException, CancelException {
        byte[] owner = WalletApi.decodeFromBase58Check(ownerBase58);
        byte[] keyBytes = ByteArray.fromHexString(ownerPrivateKey);
        return deployContract(name,symbol,owner, keyBytes);
    }

    /**
     * 商家发布NFT，部署合约
     * @param name NFT名称
     * @param symbol NFT代表符号
     * @param ownerAddress NFT发布者
     */
    public boolean deployContract(String name, String symbol, byte[] ownerAddress, byte[] ownerPrivateKey)
            throws IOException, CipherException, CancelException {
        long feeLimit = 0L;
        long value = 0L;
        long consumeUserResourcePercent = 0L;
        long originEnergyLimit = 1000000L;
        long tokenValue = 0L;
        String tokenId = "0";
        String libraryAddressPair = null;
        String compilerVersion = null;
        List<Object> parameters = Arrays.asList(name, symbol);
        String argsStr = parametersString(parameters);
        String constructorStr = "constructor(string,string)";
        String codeStr = "";//Objects.requireNonNull
        String abiStr = "";//Objects.requireNonNull
        codeStr += Hex.toHexString(Objects.requireNonNull(AbiUtil.encodeInput(constructorStr, argsStr)));
        //todo 参数优化！！！
        return walletApiWrapper.deployContract(ownerAddress,ownerPrivateKey,name,abiStr,codeStr,feeLimit,value,
                consumeUserResourcePercent,originEnergyLimit,tokenValue,tokenId,libraryAddressPair,compilerVersion);
    }

    /**
     * 调用合约方法，查询发布NFT的总量
     * @param contractAddress 合约地址
     * @param ownerAddress 合约拥有者
     */
    public long totalSupplyFromContract(byte[] contractAddress, byte[] ownerAddress){
        String argsStr = "";
        String method = "totalSupply()";
        byte[] result = callConstantContract(contractAddress,ownerAddress,method,argsStr);
        return ByteUtil.byteArrayToLong(result);
    }

    /**
     * 调用合约方法，查询账户NFT个数
     * @param contractAddress 合约地址
     * @param ownerAddress NFT拥有者
     */
    public long balanceFromContract(byte[] contractAddress, byte[] ownerAddress) {
        String argsStr = "";
        String method = "balanceOf()";
        byte[] result =  callConstantContract(contractAddress, ownerAddress, method, argsStr);
        return ByteUtil.byteArrayToLong(result);
    }

    /**
     * 根据所有查询NFT的ID
     * @param contractAddress 合约地址
     * @param ownerAddress NFT拥有者
     * @param index
     */
    public long tokenByIndexFromContract(byte[] contractAddress, byte[] ownerAddress, long index) {
        List<Object> parameters = Collections.singletonList(index);
        String argsStr = parametersString(parameters);
        String method = "tokenByIndex(uint256)";
        byte[] result =  callConstantContract(contractAddress, ownerAddress, method, argsStr);
        return ByteUtil.byteArrayToLong(result);
    }

    /**
     * 根据NFT的ID查询NFT的元信息
     * @param contractAddress 合约地址
     * @param ownerAddress NFT拥有者
     * @param tokenId NFT的ID
     */
    public String tokenUriFromContract(byte[] contractAddress, byte[] ownerAddress, long tokenId) {
        List<Object> parameters = Collections.singletonList(tokenId);
        String argsStr = parametersString(parameters);
        String method = "tokenURI(uint256)";
        byte[] result =  callConstantContract(contractAddress, ownerAddress, method, argsStr);
        return Strings.fromByteArray(result).trim();
    }

    /**
     * 根据NFT的ID查询NFT的拥有者
     * @param contractAddress 合约地址
     * @param ownerAddress 合约拥有者
     * @param tokenId NFT的ID
     */
    public String ownerFromContract(byte[] contractAddress, byte[] ownerAddress, long tokenId) {
        List<Object> parameters = Collections.singletonList(tokenId);
        String argsStr = parametersString(parameters);
        String method = "ownerOf(uint256)";
        byte[] result =  callConstantContract(contractAddress, ownerAddress, method, argsStr);
        return WalletApi.encode58Check(result);
    }

    private byte[] callConstantContract(byte[] contractAddress,byte[] ownerAddress, String method, String argsStr) {
        byte[] data = Hex.decode(AbiUtil.parseMethod(method, argsStr, false));
        return walletApiWrapper.triggerConstantContract(ownerAddress, contractAddress, data);
    }

    public boolean mintNftToMyself(byte[] contractOwner, byte[] ownerPrivateKey, byte[] contractAddress,
                           long tokenId, String metaData)
            throws IOException, CipherException, CancelException {
        return mintNft(contractOwner,ownerPrivateKey,contractAddress,contractOwner,tokenId,metaData);
    }

    public boolean mintNft(byte[] contractOwner, byte[] ownerPrivateKey, byte[] contractAddress,
                        byte[] mintTo, long tokenId, String metaData)
            throws IOException, CipherException, CancelException {
        String method = "mint(address,uint256,string)";
        List<Object> parameters = Arrays.asList(mintTo,tokenId,metaData);
        String argsStr = parametersString(parameters);
        byte[] data = Hex.decode(AbiUtil.parseMethod(method, argsStr, false));
        return walletApiWrapper.triggerContract(contractOwner,ownerPrivateKey,contractAddress,data);
    }

    public boolean transferNft(byte[] tokenOwner, byte[] ownerPrivateKey, byte[] contractAddress,
                            byte[] to, long tokenId)
            throws IOException, CipherException, CancelException {
        String method = "transferFrom(address,address,uint256)";
        List<Object> parameters = Arrays.asList(tokenOwner,to,tokenId);
        String argsStr = parametersString(parameters);
        byte[] data = Hex.decode(AbiUtil.parseMethod(method, argsStr, false));
        return walletApiWrapper.triggerContract(tokenOwner,ownerPrivateKey,contractAddress,data);
    }

    public boolean setTokenURI(byte[] tokenOwner, byte[] ownerPrivateKey, byte[] contractAddress,
                            long tokenId, String metaData)
            throws IOException, CipherException, CancelException {
        String method = "setTokenURI(uint256,string)";
        List<Object> parameters = Arrays.asList(tokenId,metaData);
        String argsStr = parametersString(parameters);
        byte[] data = Hex.decode(AbiUtil.parseMethod(method, argsStr, false));
        return walletApiWrapper.triggerContract(tokenOwner,ownerPrivateKey,contractAddress,data);
    }

    private String parametersString(List<Object> parameters) {
        String[] inputArr = new String[parameters.size()];
        int i = 0;
        for (Object parameter : parameters) {
            if (parameter instanceof List) {
                StringBuilder sb = new StringBuilder();
                for (Object item : (List) parameter) {
                    if (sb.length() != 0) {
                        sb.append(",");
                    }
                    sb.append("\"").append(item).append("\"");
                }
                inputArr[i++] = "[" + sb.toString() + "]";
            } else {
                inputArr[i++] =
                        (parameter instanceof String) ? ("\"" + parameter + "\"") : ("" + parameter);
            }
        }
        String input = StringUtils.join(inputArr, ',');
        return input;
    }
}
