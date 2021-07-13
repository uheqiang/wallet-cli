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
            walletClient.login();
        } catch (IOException | CipherException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deployContract(){
        String address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        //byte[] addr = WalletApi.decodeFromBase58Check(address);
        String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        String name = "NFT_CONTRACT";

        boolean result = false;
        try {
            long energyPay = 10;
            walletClient.deployContract(address,privateKeyStr,name,energyPay);
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
    public void deployContractProxyPay(){
        String address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        //byte[] addr = WalletApi.decodeFromBase58Check(address);
        String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        String name = "NFT_CONTRACT";

        String sponsorBase58 = "TSNZ43xVfLoJ3RGxfE52xkFfENokwkgEXY";
        String sponsorPrivateKey = "bb3a4643f77cdc584a511cc039d58955f66e341b9d79d43668515c1a5ce979a5";
        long limitPerTransaction = 10;

        boolean result = false;
        try {
            long energyPay = 10;
            walletClient.deployContract(address,
                    privateKeyStr,
                    name,
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
    public void mintNft(){
        String address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        byte[] contractOwner = WalletApi.decodeFromBase58Check(address);
        String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] ownerPrivateKey = ByteArray.fromHexString(privateKeyStr);
        //0.6.8
        String contract = "TLWYaGcWj7bg6CTpQ4dYwjENdKAVq2DqvJ";
        byte[] contractAddress = WalletApi.decodeFromBase58Check(contract);
        long tokenId = 0;
        String metaData = "This is meta data of the first token!";
        long energyPay = 10;

        boolean result = false;
        try {
            result = walletClient.mintNft(contractOwner,
                    ownerPrivateKey,
                    contractAddress,
                    contractOwner,
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
        byte[] contractOwner = WalletApi.decodeFromBase58Check(address);
        String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] ownerPrivateKey = ByteArray.fromHexString(privateKeyStr);
        //0.6.8
        String contract = "TLWYaGcWj7bg6CTpQ4dYwjENdKAVq2DqvJ";
        byte[] contractAddress = WalletApi.decodeFromBase58Check(contract);
        long tokenId = 0L;
        String metaData = "This is meta data of the first token!";
        long energyPay = 10L;

        String sponsorBase58 = "TSNZ43xVfLoJ3RGxfE52xkFfENokwkgEXY";
        byte[] sponsorAddress = WalletApi.decodeFromBase58Check(sponsorBase58);
        String sponsorPrivateKey = "bb3a4643f77cdc584a511cc039d58955f66e341b9d79d43668515c1a5ce979a5";
        byte[] sponsorPrivateKeys = ByteArray.fromHexString(sponsorPrivateKey);
        long limitPerTransaction = 10L;

        boolean result = false;
        try {
            result = walletClient.mintNft(contractOwner,
                    ownerPrivateKey,
                    contractAddress,
                    contractOwner,
                    tokenId,
                    metaData,
                    energyPay,
                    sponsorAddress,
                    sponsorPrivateKeys,
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
    public void balanceOfAndTokenInfo(){
        String address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        address = "TMXnRunmpzLgdP4sG3mYMkZZ8Q6f9DV847";
        byte[] addr = WalletApi.decodeFromBase58Check(address);
        //0.6.8
        String contract = "TLWYaGcWj7bg6CTpQ4dYwjENdKAVq2DqvJ";
        byte[] contractAddr = WalletApi.decodeFromBase58Check(contract);

        long balance = walletClient.balanceFromContract(contractAddr,addr);
        System.out.println("balance = " + balance);

        for (long index = 0; index < balance; index++) {
            long tokenId = walletClient.tokenByIndexFromContract(contractAddr, addr, index);
            System.out.println("token id: " + tokenId);
            String tokenMetaData = walletClient.tokenUriFromContract(contractAddr, addr, tokenId);
            System.out.println("token meta data: " + tokenMetaData);
        }
    }

    @Test
    public void transferNft(){
        String address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        byte[] addr = WalletApi.decodeFromBase58Check(address);
        String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] ownerPrivateKey = ByteArray.fromHexString(privateKeyStr);
        //0.6.8
        String contract = "TLWYaGcWj7bg6CTpQ4dYwjENdKAVq2DqvJ";
        byte[] contractAddr = WalletApi.decodeFromBase58Check(contract);
        long tokenId = 0L;
        long energyPay = 10L;
        String to = "TMXnRunmpzLgdP4sG3mYMkZZ8Q6f9DV847";
        byte[] toBytes = WalletApi.decodeFromBase58Check(to);

        boolean result = false;
        try {
            result = walletClient.transferNft(addr,
                    ownerPrivateKey,
                    contractAddr,
                    toBytes,
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
        String contract = "TLWYaGcWj7bg6CTpQ4dYwjENdKAVq2DqvJ";
        byte[] contractAddr = WalletApi.decodeFromBase58Check(contract);
        long tokenId = 0L;
        long energyPay = 10L;
        String to = "TMXnRunmpzLgdP4sG3mYMkZZ8Q6f9DV847";
        byte[] toBytes = WalletApi.decodeFromBase58Check(to);

        String sponsorBase58 = "TSNZ43xVfLoJ3RGxfE52xkFfENokwkgEXY";
        byte[] sponsorAddress = WalletApi.decodeFromBase58Check(sponsorBase58);
        String sponsorPrivateKey = "bb3a4643f77cdc584a511cc039d58955f66e341b9d79d43668515c1a5ce979a5";
        byte[] sponsorPrivateKeys = ByteArray.fromHexString(sponsorPrivateKey);
        long limitPerTransaction = 10L;

        boolean result = false;
        try {
            result = walletClient.transferNft(addr,
                    ownerPrivateKey,
                    contractAddr,
                    toBytes,
                    tokenId,
                    energyPay,
                    sponsorAddress,
                    sponsorPrivateKeys,
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
        String address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        byte[] addr = WalletApi.decodeFromBase58Check(address);
        String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] ownerPrivateKey = ByteArray.fromHexString(privateKeyStr);
        //0.6.8
        String contract = "TLWYaGcWj7bg6CTpQ4dYwjENdKAVq2DqvJ";
        byte[] contractAddr = WalletApi.decodeFromBase58Check(contract);
        long tokenId = 0L;
        long energyPay = 10L;
        boolean result = false;
        try {
            result = walletClient.setTokenURI(addr,
                    ownerPrivateKey,
                    contractAddr,
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
        byte[] addr = WalletApi.decodeFromBase58Check(address);
        String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] ownerPrivateKey = ByteArray.fromHexString(privateKeyStr);
        //0.6.8
        String contract = "TLWYaGcWj7bg6CTpQ4dYwjENdKAVq2DqvJ";
        byte[] contractAddr = WalletApi.decodeFromBase58Check(contract);
        long tokenId = 0L;
        long energyPay = 10L;

        String sponsorBase58 = "TSNZ43xVfLoJ3RGxfE52xkFfENokwkgEXY";
        byte[] sponsorAddress = WalletApi.decodeFromBase58Check(sponsorBase58);
        String sponsorPrivateKey = "bb3a4643f77cdc584a511cc039d58955f66e341b9d79d43668515c1a5ce979a5";
        byte[] sponsorPrivateKeys = ByteArray.fromHexString(sponsorPrivateKey);
        long limitPerTransaction = 10L;

        boolean result = false;
        try {
            result = walletClient.setTokenURI(addr,
                    ownerPrivateKey,
                    contractAddr,
                    tokenId,
                    metaData,
                    energyPay,
                    sponsorAddress,
                    sponsorPrivateKeys,
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
