package org.tron.walletcli;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.Strings;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.crypto.SignUtils;
import org.tron.common.entity.AccountInfo;
import org.tron.common.utils.AbiUtil;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.ByteUtil;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.core.exception.DecodingException;
import org.tron.core.exception.TronException;
import org.tron.keystore.Wallet;
import org.tron.protos.Contract;
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
    public boolean assetIssue(String ownerBase58,
                              String ownerPriKey,
                              String name,
                              long totalSupply)
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
    public boolean assetIssue(byte[] owner,
                              byte[] ownerKey,
                              String name,
                              long totalSupply)
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
    public boolean assetTransfer(String fromBase58,
                                 String fromPrivateKey,
                                 String toBase58,
                                 String assetId,
                                 long amount)
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
    public boolean assetTransfer(byte[] from,
                                 byte[] fromPrivateKey,
                                 byte[] to,
                                 String assetId,
                                 long amount)
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
    public boolean freezeBalanceForMyself(byte[] ownerAddress,
                                          byte[] ownerPrivateKey,
                                          long frozenBalance)
            throws IOException, CipherException, CancelException {
        ByteString assertId = ByteString.copyFromUtf8(ASSET_ID);
        return walletApiWrapper.freezeBalance(ownerAddress,ownerPrivateKey,
                frozenBalance,assertId,ownerAddress);
    }

    /**
     * 兑换能量
     * @param ownerBase58 兑换者
     * @param ownerPrivateKey 兑换者的私钥
     * @param frozenBalance 用于兑换能量的TRC10资产的数量
     */
    public boolean freezeBalanceForMyself(String ownerBase58,
                                          String ownerPrivateKey,
                                          long frozenBalance)
            throws CipherException, IOException, CancelException {
        ByteString assertId = ByteString.copyFromUtf8(ASSET_ID);
        return freezeBalance(ownerBase58,ownerPrivateKey,frozenBalance,assertId,ownerBase58);
    }

    private boolean freezeBalance(String ownerBase58,
                                  String ownerPrivateKey,
                                  long frozenBalance,
                                  ByteString assertId,
                                  String receiverBase58)
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
    public boolean createAccount(byte[] owner,
                                 byte[] ownerPrivateKey,
                                 byte[] address,
                                 String identity)
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
    public boolean createAccount(String ownerBase58, String ownerPrivateKey,
                                 String addressBase58, String identity)
            throws CipherException, IOException, CancelException {
        byte[] owner = WalletApi.decodeFromBase58Check(ownerBase58);
        byte[] address = WalletApi.decodeFromBase58Check(addressBase58);
        byte[] keyBytes = ByteArray.fromHexString(ownerPrivateKey);
        return walletApiWrapper.createAccount(owner, keyBytes, address, identity);
    }

    /**
     * 注册商家
     * @return 商家的ID
     */
    public String createBusiness() {
        return walletApiWrapper.createBusiness();
    }

    /**
     * 商家发布NFT，部署合约，不需要委托支付
     * @param ownerBase58 NFT发布者
     * @param ownerPrivateKey NFT发布者的私钥
     * @param contractName NFT合约名称
     * @param energyPay 部署合约需要支付的手续费
     */
    public String deployContract(String ownerBase58,
                                 String ownerPrivateKey,
                                 String contractName,
                                 long energyPay)
            throws CancelException {
        byte[] owner = WalletApi.decodeFromBase58Check(ownerBase58);
        byte[] keyBytes = ByteArray.fromHexString(ownerPrivateKey);
        return deployContract(owner,keyBytes,contractName,energyPay,null,null);
    }

    /**
     * 商家发布NFT，部署合约，需要委托支付
     * @param ownerBase58 NFT发布者
     * @param ownerPrivateKey NFT发布者的私钥
     * @param contractName NFT合约名称
     * @param energyPay  付的手续费
     * @param sponsorBase58  代理支付地址
     * @param sponsorPrivateKey 代理支付私钥
     * @param limitPerTransaction 代理支付最大额度
     */
    public String deployContract(String ownerBase58,
                                 String ownerPrivateKey,
                                 String contractName,
                                 long energyPay,
                                 String sponsorBase58,
                                 String sponsorPrivateKey,
                                 long limitPerTransaction)
            throws CancelException {
        Contract.DelegationPay.Builder delegationPayBuilder = Contract.DelegationPay.newBuilder();
        delegationPayBuilder.setSupport(true);
        byte[] byteSponsor = Objects.requireNonNull(WalletApi.decodeFromBase58Check(sponsorBase58));
        ByteString sponsor = ByteString.copyFrom(byteSponsor);
        delegationPayBuilder.setSponsor(sponsor);
        delegationPayBuilder.setSponsorlimitpertransaction(limitPerTransaction);
        byte[] delegationPrivateKey = null;
        if (StringUtils.isNotEmpty(sponsorPrivateKey)) {
            delegationPrivateKey = ByteArray.fromHexString(ownerPrivateKey);
        }
        return deployContract(ownerBase58,ownerPrivateKey,contractName,energyPay,
                delegationPayBuilder.build(),delegationPrivateKey);

    }


    /**
     * 商家发布NFT，部署合约，可以设置委托支付
     * @param ownerBase58 NFT发布者
     * @param ownerPrivateKey NFT发布者的私钥
     * @param contractName NFT合约名称
     * @param energyPay 部署合约需要支付的手续费
     * @param delegationPay 代理支付信息
     * @param delegationPrivateKey 代理支付者私钥
     */
    private String deployContract(String ownerBase58,
                                  String ownerPrivateKey,
                                  String contractName,
                                 long energyPay,
                                  Contract.DelegationPay delegationPay,
                                 byte[] delegationPrivateKey)
            throws CancelException {
        byte[] owner = WalletApi.decodeFromBase58Check(ownerBase58);
        byte[] keyBytes = ByteArray.fromHexString(ownerPrivateKey);
        return deployContract(owner,keyBytes,contractName,energyPay,delegationPay,delegationPrivateKey);
    }

    /**
     * 商家发布NFT，部署合约
     * @param ownerAddress NFT发布者
     * @param ownerPrivateKey NFT发布者的私钥
     * @param contractName NFT合约名称
     * @param energyPay 部署合约需要支付的手续费
     * @return 合约地址
     */
    public String deployContract(byte[] ownerAddress,
                                 byte[] ownerPrivateKey,
                                 String contractName,
                                 long energyPay,
                                 Contract.DelegationPay delegationPay,
                                 byte[] delegationPrivateKey)
            throws CancelException {
        List<Object> parameters = Arrays.asList("name", "symbol");
        String argsStr = parametersString(parameters);
        String constructorStr = "constructor(string,string)";
        String codeStr = Objects.requireNonNull(WalletApi.contractCode);
        String abiStr = Objects.requireNonNull(WalletApi.contractAbi);
        codeStr += Hex.toHexString(Objects.requireNonNull(AbiUtil.encodeInput(constructorStr, argsStr)));
        return deployContract(codeStr, abiStr, contractName,
                ownerAddress, ownerPrivateKey, energyPay,
                delegationPay,delegationPrivateKey,null, null);
    }

    private String deployContract(String codeStr,
                                  String abiStr,
                                  String contractName,
                                  byte[] ownerAddress,
                                  byte[] ownerPrivateKey,
                                  long energyPay,
                                  Contract.DelegationPay delegationPay,
                                  byte[] delegationPrivateKey,
                                  String libraryAddressPair,
                                  String compilerVersion)
            throws CancelException {
        long feeLimit = 0L;
        long value = 0L;
        long consumeUserResourcePercent = 0L;

        long tokenValue = 0L;
        String tokenId = "0";
        return walletApiWrapper.deployContract(ownerAddress,ownerPrivateKey,
                contractName,abiStr,codeStr,feeLimit,value,
                consumeUserResourcePercent,energyPay,delegationPay,delegationPrivateKey,tokenValue,
                tokenId,libraryAddressPair,compilerVersion);
    }

    /**
     * 调用合约方法，查询发布NFT的总量
     * @param contractAddress 合约地址
     * @param ownerAddress 合约拥有者
     */
    public long totalSupplyFromContract(byte[] contractAddress,
                                        byte[] ownerAddress){
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
    public long balanceFromContract(byte[] contractAddress,
                                    byte[] ownerAddress) {
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
    public long tokenByIndexFromContract(byte[] contractAddress,
                                         byte[] ownerAddress,
                                         long index) {
        List<Object> parameters = Collections.singletonList(index);
        String argsStr = parametersString(parameters);
        String method = "tokenByIndex(uint256)";
        byte[] result =  callConstantContract(contractAddress, ownerAddress, method, argsStr);
        return ByteUtil.byteArrayToLong(result);
    }


    /**
     * 根据NFT的ID查询NFT的拥有者
     * @param contractAddress 合约地址
     * @param ownerAddress 合约拥有者
     * @param tokenId NFT的ID
     */
    public String ownerFromContract(byte[] contractAddress,
                                    byte[] ownerAddress,
                                    long tokenId) {
        List<Object> parameters = Collections.singletonList(tokenId);
        String argsStr = parametersString(parameters);
        String method = "ownerOf(uint256)";
        byte[] result =  callConstantContract(contractAddress, ownerAddress, method, argsStr);
        return WalletApi.encode58Check(result);
    }

    private byte[] callConstantContract(byte[] contractAddress,
                                        byte[] ownerAddress,
                                        String method,
                                        String argsStr) {
        byte[] data = Hex.decode(AbiUtil.parseMethod(method, argsStr, false));
        return walletApiWrapper.triggerConstantContract(ownerAddress, contractAddress, data);
    }

    /**
     * 发布NFT，接收者是自己
     * @param contractOwner  合约owner
     * @param ownerPrivateKey  合约owner的私钥
     * @param contractAddress  合约地址
     * @param tokenId token ID
     * @param metaData token的元数据
     */
    public boolean mintNftToMyself(byte[] contractOwner,
                                   byte[] ownerPrivateKey,
                                   byte[] contractAddress,
                                   long tokenId,
                                   String metaData,
                                   long energyPay,
                                   Contract.DelegationPay delegationPay,
                                   byte[] delegationPrivateKey)
            throws CancelException {
        return mintNft(contractOwner,ownerPrivateKey,contractAddress,
                contractOwner,tokenId,metaData,energyPay,delegationPay,delegationPrivateKey);
    }

    /**
     * 发布NFT，接收者是自己, 可以是他人
     * @param contractOwner 合约owner
     * @param ownerPrivateKey 合约owner的私钥
     * @param contractAddress 合约地址
     * @param mintTo token接收者地址
     * @param tokenId token ID
     * @param metaData token的元数据
     */
    public boolean mintNft(byte[] contractOwner,
                           byte[] ownerPrivateKey,
                           byte[] contractAddress,
                           byte[] mintTo,
                           long tokenId,
                           String metaData,
                           long energyPay,
                           Contract.DelegationPay delegationPay,
                           byte[] delegationPrivateKey)
            throws CancelException {
        String method = "mint(address,uint256,string)";
        List<Object> parameters = Arrays.asList(mintTo,tokenId,metaData);
        String argsStr = parametersString(parameters);
        byte[] data = Hex.decode(AbiUtil.parseMethod(method, argsStr, false));
        return walletApiWrapper.triggerContract(contractOwner,
                ownerPrivateKey,contractAddress,data,energyPay,
                delegationPay,delegationPrivateKey);
    }

    /**
     * 转移NFT
     * @param tokenOwner token拥有者
     * @param ownerPrivateKey token拥有者私钥
     * @param contractAddress 合约地址
     * @param to token接收者
     * @param tokenId 被转移的token ID
     */
    public boolean transferNft(byte[] tokenOwner,
                               byte[] ownerPrivateKey,
                               byte[] contractAddress,
                               byte[] to,
                               long tokenId,
                               long energyPay,
                               Contract.DelegationPay delegationPay,
                               byte[] delegationPrivateKey)
            throws CancelException {
        String method = "transferFrom(address,address,uint256)";
        List<Object> parameters = Arrays.asList(tokenOwner,to,tokenId);
        String argsStr = parametersString(parameters);
        byte[] data = Hex.decode(AbiUtil.parseMethod(method, argsStr, false));
        return walletApiWrapper.triggerContract(tokenOwner,ownerPrivateKey,
                contractAddress,data,energyPay,
                delegationPay,delegationPrivateKey);
    }

    /**
     * 根据NFT的ID查询NFT的元信息
     * @param contractAddress 合约地址
     * @param ownerAddress NFT拥有者
     * @param tokenId NFT的ID
     */
    public String tokenUriFromContract(byte[] contractAddress,
                                       byte[] ownerAddress,
                                       long tokenId) {
        List<Object> parameters = Collections.singletonList(tokenId);
        String argsStr = parametersString(parameters);
        String method = "tokenURI(uint256)";
        byte[] result =  callConstantContract(contractAddress, ownerAddress, method, argsStr);
        return Strings.fromByteArray(result).trim();
    }

    /**
     * 设置Token的元数据
     * @param tokenOwner token拥有者
     * @param ownerPrivateKey token拥有者私钥
     * @param contractAddress 合约地址
     * @param tokenId NFT的ID
     * @param metaData 更新后NFT的元数据
     */
    public boolean setTokenURI(byte[] tokenOwner,
                               byte[] ownerPrivateKey,
                               byte[] contractAddress,
                               long tokenId,
                               String metaData,
                               long energyPay,
                               Contract.DelegationPay delegationPay,
                               byte[] delegationPrivateKey)
            throws CancelException {
        String method = "setTokenURI(uint256,string)";
        List<Object> parameters = Arrays.asList(tokenId,metaData);
        String argsStr = parametersString(parameters);
        byte[] data = Hex.decode(AbiUtil.parseMethod(method, argsStr, false));
        return walletApiWrapper.triggerContract(tokenOwner,
                ownerPrivateKey,contractAddress,data,energyPay,delegationPay,delegationPrivateKey);
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
