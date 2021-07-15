package org.tron.contract;

import org.junit.Before;
import org.junit.Test;
import org.tron.common.utils.ByteArray;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.walletcli.WalletClient;
import org.tron.walletserver.WalletApi;

import java.io.IOException;

/**
 * 发布NFT
 * @author Brian
 * @date 2021/6/28 16:25
 */
public class NFTTests {

    private WalletClient walletClient = new WalletClient();

    @Before
    public void login() {
        try {
            walletClient.init();
        } catch (IOException | CipherException e) {
            e.printStackTrace();
        }
    }

    private String owner = "TZGeVYoX3HaD1U89GtkqUSrCCkNcaWBiWk";
    private String ownerPrivateKey = "3b8aaabf34ed7de6ab95fd5e48f8c507a031de381e743935cf3a297312cecc08";

    private String contract = "TEcMUNS2Cz5Kp1yhdPhk11j8k2vC8sUxxj";

    @Test
    public void deployContract(){
        String address = "TZGeVYoX3HaD1U89GtkqUSrCCkNcaWBiWk";
        byte[] addr = WalletApi.decodeFromBase58Check(owner);
        String privateKeyStr = "3b8aaabf34ed7de6ab95fd5e48f8c507a031de381e743935cf3a297312cecc08";
        byte[] privateKey = ByteArray.fromHexString(ownerPrivateKey);

        try {
            long energyPay = 10;
            String name = "NFT_CONTRACT";
            String contractAddress = walletClient.deployContract(owner,ownerPrivateKey,name,energyPay);
            System.out.println("contract address: " + contractAddress);
        } catch (CancelException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void callConstantContract() {
        //String address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        //0.6.8
        String contract = "TWGKYukW2H1eRZAsYLAqEdxMdhJ3Je9hAt";
        contract = "TCce7AvnAQuc8bMBwFXPCWjP2heniaDdDo";
        contract = "TMFsQA7W3YvKDQVRRcbTabVad5kft3iBTU";
        contract = "TY7D1E5HpNrxMZNGcinRmFWtwQY6urwfbi";
        contract = "THYkv4ZpSQV1QaQHjj6JWDkDQXA8z5mMNp";
        String method = "name()";
        method = "symbol()";
        String args = "";
        byte[] bytes = walletClient.callConstantContract(contract, owner, method, args);
        System.out.println("nft name: " + ByteArray.toStr(bytes).trim());

    }

    @Test
    public void deployContractProxyPay(){
        String address = "TK4hysxx6poz4zWXfZdc6EF4MURfsZdGpY";//11-3
        String privateKeyStr = "532a5ac5fc42611be31ca6eb42ab9e9367bda4be92a912a1cff11a2e2f849e62";
        String name = "NFT_CONTRACT";

        String sponsorBase58 = "TEzf5rMPbg9koskvykmtaFtRFms1SwHghd";//2-2
        String sponsorPrivateKey = "2b209f726b2fd50603d08df2c7786bf7c8aa446b0ba4aeb66aef133958d07672";
        long limitPerTransaction = 90;

        try {
            long energyPay = 5;
            String contractAddress = walletClient.deployContract(address,
                    privateKeyStr,
                    name,
                    energyPay,
                    sponsorBase58,
                    sponsorPrivateKey,
                    limitPerTransaction);
            System.out.println("contract address: " + contractAddress);
        } catch (CancelException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void mintNft(){
        //String address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        byte[] contractOwner = WalletApi.decodeFromBase58Check(owner);
        //String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] ownerKey = ByteArray.fromHexString(ownerPrivateKey);
        //0.6.8
        //String contract = "TEcMUNS2Cz5Kp1yhdPhk11j8k2vC8sUxxj";
        byte[] contractAddress = WalletApi.decodeFromBase58Check(contract);
        long tokenId = 3;
        String metaData = "This is meta data of the third token!";
        long energyPay = 10;

        boolean result = false;
        try {
            result = walletClient.mintNft(owner,
                    ownerPrivateKey,
                    contract,
                    owner,
                    tokenId,
                    metaData,
                    energyPay);
        } catch (CancelException e) {
            e.printStackTrace();
        }
        if (result) {
            System.out.println("Create contract successful !!");
        } else {
            System.out.println("Create contract failed !!");
        }
    }

    @Test
    public void mintNftProxyPay(){
        String address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        byte[] contractOwner = WalletApi.decodeFromBase58Check(owner);
        String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] ownerKey = ByteArray.fromHexString(ownerPrivateKey);
        //0.6.8
        //String contract = "TEcMUNS2Cz5Kp1yhdPhk11j8k2vC8sUxxj";
        byte[] contractAddress = WalletApi.decodeFromBase58Check(contract);
        long tokenId = 0L;
        String metaData = "This is meta data of the first token!";
        long energyPay = 10L;

        String sponsorBase58 = "TSNZ43xVfLoJ3RGxfE52xkFfENokwkgEXY";
        String sponsorPrivateKey = "bb3a4643f77cdc584a511cc039d58955f66e341b9d79d43668515c1a5ce979a5";
        long limitPerTransaction = 10L;

        boolean result = false;
        try {
            String to = owner;
            result = walletClient.mintNft(owner,
                    ownerPrivateKey,
                    contract,
                    to,
                    tokenId,
                    metaData,
                    energyPay,
                    sponsorBase58,
                    sponsorPrivateKey,
                    limitPerTransaction);
        } catch (CancelException e) {
            e.printStackTrace();
        }
        if (result) {
            System.out.println("Mint nft successful !!");
        } else {
            System.out.println("Mint nft failed !!");
        }
    }

    @Test
    public void balanceOfAndTokenInfo(){
        String address = owner;
        address = "TTtQ5cFAN9cxytfcRuW6bGu7dGSJbAp45H";
        //0.6.8
        //String contract = "TEcMUNS2Cz5Kp1yhdPhk11j8k2vC8sUxxj";

        long balance = walletClient.balanceFromContract(contract,address);
        System.out.println("balance = " + balance);

        for (long index = 0; index < balance; index++) {
            long tokenId = walletClient.tokenByIndexFromContract(contract, address, index);
            System.out.println("token id: " + tokenId);
            String tokenMetaData = walletClient.tokenMateDataFromContract(contract, address, tokenId);
            System.out.println("token meta data: " + tokenMetaData);
        }
    }

    @Test
    public void transferNft(){
        String address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        byte[] addr = WalletApi.decodeFromBase58Check(owner);
        String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] ownerKey = ByteArray.fromHexString(ownerPrivateKey);
        //0.6.8
        //String contract = "TEcMUNS2Cz5Kp1yhdPhk11j8k2vC8sUxxj";
        byte[] contractAddr = WalletApi.decodeFromBase58Check(contract);
        long tokenId = 0L;
        long energyPay = 10L;
        String to = "TTtQ5cFAN9cxytfcRuW6bGu7dGSJbAp45H";

        boolean result = false;
        try {
            result = walletClient.transferNft(owner,
                    ownerPrivateKey,
                    contract,
                    to,
                    tokenId,
                    energyPay);
        } catch (CancelException e) {
            e.printStackTrace();
        }
        if (result) {
            System.out.println("Create contract successful !!");
        } else {
            System.out.println("Create contract failed !!");
        }
    }


    @Test
    public void transferNftProxyPay(){
        String address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        byte[] addr = WalletApi.decodeFromBase58Check(address);
        String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] ownerPrivateKey = ByteArray.fromHexString(privateKeyStr);
        //0.6.8
        //String contract = "TEcMUNS2Cz5Kp1yhdPhk11j8k2vC8sUxxj";
        byte[] contractAddr = WalletApi.decodeFromBase58Check(contract);
        long tokenId = 0L;
        long energyPay = 10L;
        String to = "TMXnRunmpzLgdP4sG3mYMkZZ8Q6f9DV847";
        byte[] toBytes = WalletApi.decodeFromBase58Check(to);

        String sponsorBase58 = "TSNZ43xVfLoJ3RGxfE52xkFfENokwkgEXY";
        String sponsorPrivateKey = "bb3a4643f77cdc584a511cc039d58955f66e341b9d79d43668515c1a5ce979a5";
        long limitPerTransaction = 10L;

        boolean result = false;
        try {
            result = walletClient.transferNft(owner,
                    owner,
                    contract,
                    to,
                    tokenId,
                    energyPay,
                    sponsorBase58,
                    sponsorPrivateKey,
                    limitPerTransaction);
        } catch (CancelException e) {
            e.printStackTrace();
        }
        if (result) {
            System.out.println("Create contract successful !!");
        } else {
            System.out.println("Create contract failed !!");
        }
    }


    @Test
    public void setMetaData() {
        String metaData = "new meta info";
        String address = "TTtQ5cFAN9cxytfcRuW6bGu7dGSJbAp45H";
        String privateKeyStr = "9e8b37d64fc121331674406ec2ac856cb8db3acf33eb47f1ed2fd6b53fa5d460";
        //0.6.8
        //String contract = "TEcMUNS2Cz5Kp1yhdPhk11j8k2vC8sUxxj";
        long tokenId = 0L;
        long energyPay = 10L;
        boolean result = false;
        try {
            result = walletClient.setTokenMetaData(owner,
                    ownerPrivateKey,
                    contract,
                    tokenId,
                    metaData,
                    energyPay);
        } catch (CancelException e) {
            e.printStackTrace();
        }
        if (result) {
            System.out.println("Update meta successful !!");
        } else {
            System.out.println("Update meta failed !!");
        }
    }

    @Test
    public void setMetaDataProxyPay() {
        String metaData = "new meta info";
        String address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        //0.6.8
        //String contract = "TEcMUNS2Cz5Kp1yhdPhk11j8k2vC8sUxxj";
        long tokenId = 0L;
        long energyPay = 10L;

        String sponsorBase58 = "TSNZ43xVfLoJ3RGxfE52xkFfENokwkgEXY";
        String sponsorPrivateKey = "bb3a4643f77cdc584a511cc039d58955f66e341b9d79d43668515c1a5ce979a5";
        long limitPerTransaction = 10L;

        boolean result = false;
        try {
            result = walletClient.setTokenMetaData(owner,
                    ownerPrivateKey,
                    contract,
                    tokenId,
                    metaData,
                    energyPay,
                    sponsorBase58,
                    sponsorPrivateKey,
                    limitPerTransaction);
        } catch (CancelException e) {
            e.printStackTrace();
        }
        if (result) {
            System.out.println("Update meta successful !!");
        } else {
            System.out.println("Update meta failed !!");
        }
    }
}
