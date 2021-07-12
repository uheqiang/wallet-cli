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
            walletClient.login();
        } catch (IOException | CipherException e) {
            e.printStackTrace();
        }
    }

    //发布资产
    @Test
    public void createIssue(){
        /*ECKey ecKey1 = new ECKey(Utils.getRandom());
        byte[] ownerAddress = ecKey1.getAddress();
        String addressStr = WalletApi.encode58Check(ownerAddress);
        System.out.println(addressStr);
        String privateKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
        System.out.println(privateKey);*/
        String ownerAddressStr = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        byte[] ownerAddress = WalletApi.decodeFromBase58Check(ownerAddressStr);
        String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] privateKey = ByteArray.fromHexString(privateKeyStr);
        boolean result = false;
        try {
            result = walletClient.assetIssue(/*ownerAddress*/ownerAddress, privateKey,"NFTCoin", /*"nft",*/ 1000);
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
        String ownerAddressStr = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
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
        String address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        byte[] addr = WalletApi.decodeFromBase58Check(address);
        String assetId = "1000001";
        boolean result = false;
        try {
            result = walletClient.updateAsset(addr, assetId, 99999999L);
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
        address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        address = "TMXnRunmpzLgdP4sG3mYMkZZ8Q6f9DV847";
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
        String owner = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] privateKey = ByteArray.fromHexString(privateKeyStr);
        String to = "TMXnRunmpzLgdP4sG3mYMkZZ8Q6f9DV847";
        String assertId = "1000001";
        long num = 13;
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
        //byte[] ownerAddress, long frozen_balance, int resourceCode, byte[] receiverAddress
        String owner = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        byte[] ownerAddress = WalletApi.decodeFromBase58Check(owner);
        String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] privateKey = ByteArray.fromHexString(privateKeyStr);
        boolean result = false;
        try {
            result = walletClient.freezeBalanceForMyself(ownerAddress, privateKey,999999);
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
