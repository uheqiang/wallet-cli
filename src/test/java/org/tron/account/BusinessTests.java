package org.tron.account;

import org.junit.Before;
import org.junit.Test;
import org.tron.common.entity.AccountInfo;
import org.tron.common.utils.ByteArray;
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
            walletClient.init();
        } catch (IOException | CipherException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createAccountBusiness() throws CancelException {
        String ownerAddressStr = "TZGeVYoX3HaD1U89GtkqUSrCCkNcaWBiWk";
        String privateKeyStr = "3b8aaabf34ed7de6ab95fd5e48f8c507a031de381e743935cf3a297312cecc08";
        byte[] ownerAddress = WalletApi.decodeFromBase58Check(ownerAddressStr);
        byte[] privateKey = ByteArray.fromHexString(privateKeyStr);
        String identity = "This is my identity";
        boolean result = walletClient.createBusiness(ownerAddress, privateKey, identity);
        System.out.println("business: " + result);
    }

    @Test
    public void queryAccountBusiness() throws TronException {
        String ownerAddressStr = "TZGeVYoX3HaD1U89GtkqUSrCCkNcaWBiWk";
        //String privateKeyStr = "3b8aaabf34ed7de6ab95fd5e48f8c507a031de381e743935cf3a297312cecc08";
        byte[] ownerAddress = WalletApi.decodeFromBase58Check(ownerAddressStr);
        Protocol.Account account = WalletApi.queryAccount(ownerAddress);
        System.out.println(JsonFormatUtil.formatJson(JsonFormat.printToString(account, true)));
        AccountInfo accountInfo = walletClient.getAccount(ownerAddress);
        System.out.println(accountInfo.toString());
    }

}
