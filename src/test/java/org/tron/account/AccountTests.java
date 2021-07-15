package org.tron.account;

import lombok.ToString;
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
import org.tron.walletcli.WalletClient;
import org.tron.walletserver.WalletApi;

import java.io.IOException;

/**
 * 个人用户创建
 * @author Brian
 * @date 2021/6/28 10:29
 */
public class AccountTests {

    private WalletClient walletClient = new WalletClient();

    @Before
    public void login() {
        try {
            walletClient.init();
        } catch (IOException | CipherException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void registerWallet() {
        String pwd = "123456";
        try {
            String name = walletClient.registerWallet(pwd);
            System.out.println(name);
        } catch (CipherException | IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void importWallet() {
        String pwd = "123456";
        String userPrivateStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] userPrivateKey = ByteArray.fromHexString(userPrivateStr);
        try {
            String name = walletClient.importWallet(pwd, userPrivateKey);
            System.out.println(name);
        } catch (CipherException | IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void createAccount() {
        ECKey ecKey1 = new ECKey(Utils.getRandom());
        byte[] address = ecKey1.getAddress();
        String addressStr = WalletApi.encode58Check(address);
        System.out.println(addressStr);
        String privateKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
        System.out.println(privateKey);
    }


    @Test
    public void createAccountPersonal() {
        String userAddressStr = "TK4hysxx6poz4zWXfZdc6EF4MURfsZdGpY";

        String businessAddressStr = "TZGeVYoX3HaD1U89GtkqUSrCCkNcaWBiWk";
        String businessPrivateKeyStr = "3b8aaabf34ed7de6ab95fd5e48f8c507a031de381e743935cf3a297312cecc08";
        byte[] businessPrivateKey = ByteArray.fromHexString(businessPrivateKeyStr);
        String identity = "This is my identity, address is " + userAddressStr;
        boolean result = false;
        try {
            result = walletClient.createAccount(WalletApi.decodeFromBase58Check(businessAddressStr),
                    businessPrivateKey,
                    WalletApi.decodeFromBase58Check(userAddressStr),
                    identity);
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
        String address = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        //byte[] addr = WalletApi.decodeFromBase58Check(address);
        //Protocol.Account account = WalletApi.queryAccount(addr);
        //System.out.println(JsonFormatUtil.formatJson(JsonFormat.printToString(account, true)));

        try {
            AccountInfo account1 = walletClient.getAccount(address);
            System.out.println(account1.toString());
        } catch (TronException e) {
            e.printStackTrace();
        }
    }

}
