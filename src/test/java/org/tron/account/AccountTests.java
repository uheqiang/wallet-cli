package org.tron.account;

import org.junit.Before;
import org.junit.Test;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.protos.Protocol;
import org.tron.walletcli.WalletApiWrapper;
import org.tron.walletserver.WalletApi;

import java.io.IOException;

/**
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
    public void createAccountPersonal() throws IOException, CipherException, CancelException {
        String identity = "2";
        ECKey ecKey1 = new ECKey(Utils.getRandom());
        byte[] address = ecKey1.getAddress();
        String addressStr = WalletApi.encode58Check(address);
        System.out.println(addressStr);
        String privateKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
        System.out.println(privateKey);
        String addressSt = "TMXnRunmpzLgdP4sG3mYMkZZ8Q6f9DV847";
        //0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda
        boolean result = walletApiWrapper.createAccount(/*address*/WalletApi.decodeFromBase58Check(addressSt), identity);
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
        String address = "TRpE26Mm4GUTLgw7iwVACwFE1b8H8jcJt9";
        byte[] addr = WalletApi.decodeFromBase58Check(address);
        Protocol.Account account = WalletApi.queryAccount(addr);
        System.out.println(account.getPersonalInfo());
    }

}
