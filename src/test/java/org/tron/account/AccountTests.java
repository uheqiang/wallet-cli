package org.tron.account;

import org.junit.Before;
import org.junit.Test;
import org.tron.common.crypto.ECKey;
import org.tron.common.entity.AccountInfo;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.JsonFormat;
import org.tron.common.utils.JsonFormatUtil;
import org.tron.common.utils.Utils;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.core.exception.TronException;
import org.tron.protos.Protocol;
import org.tron.walletcli.WalletApiWrapper;
import org.tron.walletcli.WalletClient;
import org.tron.walletserver.WalletApi;

import java.io.IOException;

/**
 * 个人用户创建
 * @author Brian
 * @date 2021/6/28 10:29
 */
public class AccountTests {

    private WalletApiWrapper walletApiWrapper = new WalletApiWrapper();

    @Before
    public void login() {
        try {
            walletApiWrapper.login();
        } catch (IOException | CipherException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void createAccountPersonal() {
        ECKey ecKey1 = new ECKey(Utils.getRandom());
        byte[] address = ecKey1.getAddress();
        String addressStr = WalletApi.encode58Check(address);
        System.out.println(addressStr);
        String privateKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
        System.out.println(privateKey);
        //String addressSt = "TMXnRunmpzLgdP4sG3mYMkZZ8Q6f9DV847";
        //0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda
        String identity = "2";
        boolean result = false;
        try {
            result = walletApiWrapper.createAccount(address/*WalletApi.decodeFromBase58Check(addressSt)*/, identity);
        } catch (CipherException | IOException | CancelException e) {
            e.printStackTrace();
        }
        if (result) {
            System.out.println("CreateAccount successful !!");
        } else {
            System.out.println("CreateAccount failed !!");
        }
    }

    /**
     * address: TRpE26Mm4GUTLgw7iwVACwFE1b8H8jcJt9
     * privateKey：d127965d6518e9093324d7479ff8a99c6e9a3d46a9ee24528d02dc0287f8d4fb
     */
    @Test
    public void queryAccountPersonal(){
        String address = "TMXnRunmpzLgdP4sG3mYMkZZ8Q6f9DV847";
        address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        address = "TWKbzhEugLfinaKTCrtpZBLvMF588rkY6q";
//        byte[] addr = WalletApi.decodeFromBase58Check(address);
//        Protocol.Account account = WalletApi.queryAccount(addr);
//        System.out.println(JsonFormatUtil.formatJson(JsonFormat.printToString(account, true)));

        try {
            AccountInfo account1 = new WalletClient().getAccount(address);
            System.out.println(account1.toString());
        } catch (TronException e) {
            e.printStackTrace();
        }
    }

}
