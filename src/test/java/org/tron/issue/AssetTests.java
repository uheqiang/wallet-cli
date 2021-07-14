package org.tron.issue;

import org.junit.Before;
import org.junit.Test;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.JsonFormat;
import org.tron.common.utils.JsonFormatUtil;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.walletcli.WalletClient;
import org.tron.walletserver.WalletApi;

import java.io.IOException;
import java.util.Map;

/**
 * 资产发布、资产转移、能量兑换
 * @author Brian
 * @date 2021/6/28 16:25
 */
public class AssetTests {

    private WalletClient walletClient = new WalletClient();

    @Before
    public void login() {
        try {
            walletClient.init();
        } catch (IOException | CipherException e) {
            e.printStackTrace();
        }
    }

    //发布资产
    @Test
    public void createIssue(){
        String ownerAddressStr = "TZGeVYoX3HaD1U89GtkqUSrCCkNcaWBiWk";
        byte[] ownerAddress = WalletApi.decodeFromBase58Check(ownerAddressStr);
        String privateKeyStr = "3b8aaabf34ed7de6ab95fd5e48f8c507a031de381e743935cf3a297312cecc08";
        byte[] privateKey = ByteArray.fromHexString(privateKeyStr);
        boolean result = false;
        try {
            result = walletClient.assetIssue(ownerAddress, privateKey,"NFTCoin", 1000);
        } catch (CipherException | IOException | CancelException e) {
            e.printStackTrace();
        }
        if (result) {
            System.out.println("CreateBusiness successful !!");
        } else {
            System.out.println("CreateBusiness failed !!");
        }
    }

    @Test
    public void queryIssue() {
        String ownerAddressStr = "TZGeVYoX3HaD1U89GtkqUSrCCkNcaWBiWk";
        byte[] address = WalletApi.decodeFromBase58Check(ownerAddressStr);
        Protocol.Account owner = WalletApi.queryAccount(address);
        Contract.AssetIssueContract nftCoin = walletClient.getAssetIssueById(owner.getAssetIssuedID().toStringUtf8());
        System.out.println(JsonFormatUtil.formatJson(JsonFormat.printToString(nftCoin, true)));
        nftCoin = walletClient.getAssetIssueByName(owner.getAssetIssuedName().toStringUtf8());
        System.out.println(JsonFormatUtil.formatJson(JsonFormat.printToString(nftCoin, true)));
    }

    //增发资产
    @Test
    public void updateAsset() {
        String address = "TZGeVYoX3HaD1U89GtkqUSrCCkNcaWBiWk";
        byte[] addr = WalletApi.decodeFromBase58Check(address);
        String privateKeyStr = "3b8aaabf34ed7de6ab95fd5e48f8c507a031de381e743935cf3a297312cecc08";
        byte[] privateKey = ByteArray.fromHexString(privateKeyStr);
        String assetId = "1000001";
        boolean result = false;
        try {
            result = walletClient.updateAsset(addr, privateKey, assetId, 99999999L);
        } catch (CipherException | IOException | CancelException e) {
            e.printStackTrace();
        }
        if (result) {
            System.out.println("CreateBusiness successful !!");
        } else {
            System.out.println("CreateBusiness failed !!");
        }
    }

    //查询资产余额
    @Test
    public void queryBalance() {
        String address = "";
        address = "TZGeVYoX3HaD1U89GtkqUSrCCkNcaWBiWk";
        address = "TTtQ5cFAN9cxytfcRuW6bGu7dGSJbAp45H";
        address = "TPdRzuhbVUBbyMAa7XEhvhejyZA2grxprd";
        byte[] addr = WalletApi.decodeFromBase58Check(address);
        Protocol.Account account = WalletApi.queryAccount(addr);
        System.out.println(JsonFormatUtil.formatJson(JsonFormat.printToString(account, true)));

        //trc10余额
        String id = "1000001";
        Map<String, Long> assetV2Map = account.getAssetV2Map();
        long value = assetV2Map.get(id);
        System.out.println("trc10 balance: " + value);

        //能量余额
        long energy = account.getAccountResource().getFrozenBalanceForEnergy().getFrozenBalance();
        System.out.println("energy balance: " + energy);
    }


    //资产转移
    @Test
    public void transferIssue() {
        String owner = "TZGeVYoX3HaD1U89GtkqUSrCCkNcaWBiWk";
        String privateKeyStr = "3b8aaabf34ed7de6ab95fd5e48f8c507a031de381e743935cf3a297312cecc08";
        byte[] privateKey = ByteArray.fromHexString(privateKeyStr);
        String to = "TPdRzuhbVUBbyMAa7XEhvhejyZA2grxprd";
        //String privateKey = "9e8b37d64fc121331674406ec2ac856cb8db3acf33eb47f1ed2fd6b53fa5d460";
        String assertId = "1000001";
        long num = 997;
        boolean result = false;
        try {
            result = walletClient.assetTransfer(WalletApi.decodeFromBase58Check(owner),privateKey,
                    WalletApi.decodeFromBase58Check(to), assertId, num);
        } catch (IOException | CipherException | CancelException e) {
            e.printStackTrace();
        }
        if (result) {
            System.out.println("CreateBusiness successful !!");
        } else {
            System.out.println("CreateBusiness failed !!");
        }
    }

    //兑换能量(交易手续费)
    @Test
    public void exchangeFree() {
        /*String owner = "TTtQ5cFAN9cxytfcRuW6bGu7dGSJbAp45H";
        byte[] ownerAddress = WalletApi.decodeFromBase58Check(owner);
        String privateKeyStr = "9e8b37d64fc121331674406ec2ac856cb8db3acf33eb47f1ed2fd6b53fa5d460";*/
        String owner = "TZGeVYoX3HaD1U89GtkqUSrCCkNcaWBiWk";
        byte[] ownerAddress = WalletApi.decodeFromBase58Check(owner);
        String privateKeyStr = "3b8aaabf34ed7de6ab95fd5e48f8c507a031de381e743935cf3a297312cecc08";
        byte[] privateKey = ByteArray.fromHexString(privateKeyStr);
        boolean result = false;
        try {
            long num = 10000;
            result = walletClient.freezeBalanceForMyself(ownerAddress, privateKey, num);
        } catch (CipherException | IOException | CancelException e) {
            e.printStackTrace();
        }
        if (result) {
            System.out.println("CreateBusiness successful !!");
        } else {
            System.out.println("CreateBusiness failed !!");
        }
    }
}
