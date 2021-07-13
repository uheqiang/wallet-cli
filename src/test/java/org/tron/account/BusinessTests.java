package org.tron.account;

import org.junit.Before;
import org.junit.Test;
import org.tron.common.utils.JsonFormat;
import org.tron.common.utils.JsonFormatUtil;
import org.tron.core.exception.CipherException;
import org.tron.protos.Protocol;
import org.tron.walletcli.WalletApiWrapper;
import org.tron.walletserver.WalletApi;

import java.io.IOException;

/**
 * 商家或可信节点账户创建
 * @author Brian
 * @date 2021/6/28 10:46
 */
public class BusinessTests {
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
    public void createAccountBusiness() {
        String ownerAddressStr = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        //String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] ownerAddress = WalletApi.decodeFromBase58Check(ownerAddressStr);
        String identity = "This is my identity";
        String result = walletApiWrapper.createBusiness(ownerAddress, identity);
        System.out.println("business id: " + result);
    }

    @Test
    public void queryAccountBusiness(){
        String ownerAddressStr = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        //String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] ownerAddress = WalletApi.decodeFromBase58Check(ownerAddressStr);
        Protocol.Account account = walletApiWrapper.queryAccount(ownerAddress);
        System.out.println(JsonFormatUtil.formatJson(JsonFormat.printToString(account, true)));
    }

}
