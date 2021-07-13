package org.tron.account;

import org.junit.Before;
import org.junit.Test;
import org.tron.common.entity.AccountInfo;
import org.tron.common.utils.JsonFormat;
import org.tron.common.utils.JsonFormatUtil;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.core.exception.TronException;
import org.tron.protos.Protocol;
import org.tron.walletcli.WalletClient;
import org.tron.walletserver.WalletApi;

import java.io.IOException;

/**
 * 商家或可信节点账户创建
 * @author Brian
 * @date 2021/6/28 10:46
 */
public class BusinessTests {

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
    public void createAccountBusiness() throws IOException, CipherException, CancelException {
        String ownerAddressStr = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        //String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] ownerAddress = WalletApi.decodeFromBase58Check(ownerAddressStr);
        String identity = "This is my identity";
        String result = walletClient.createBusiness(ownerAddress, identity);
        System.out.println("business id: " + result);
    }

    @Test
    public void queryAccountBusiness() throws TronException {
        String ownerAddressStr = "TJch7vVyMx49r63krvbBEFwn3wda3qE3WG";
        //String privateKeyStr = "0b19153fe92ae75915afa83bc6cd9cba78a1e5fbedb8cebb6bb6a845aad9adda";
        byte[] ownerAddress = WalletApi.decodeFromBase58Check(ownerAddressStr);
        Protocol.Account account = WalletApi.queryAccount(ownerAddress);
        System.out.println(JsonFormatUtil.formatJson(JsonFormat.printToString(account, true)));
        AccountInfo accountInfo = walletClient.getAccount(ownerAddress);
        System.out.println(accountInfo.toString());
    }

}
