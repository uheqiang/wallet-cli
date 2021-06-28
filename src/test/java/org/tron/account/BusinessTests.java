package org.tron.account;

import org.junit.Before;
import org.junit.Test;
import org.tron.common.utils.JsonFormat;
import org.tron.common.utils.JsonFormatUtil;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.protos.Protocol;
import org.tron.walletcli.WalletApiWrapper;

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
        boolean result = false;
        try {
            result = walletApiWrapper.createBusiness();
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
    public void queryAccountBusiness(){
        Protocol.Account account = walletApiWrapper.queryAccount();
        System.out.println(JsonFormatUtil.formatJson(JsonFormat.printToString(account, true)));
    }

}
