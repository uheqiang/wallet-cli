package org.tron.account;

import org.junit.Before;
import org.junit.Test;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.protos.Protocol;
import org.tron.walletcli.WalletApiWrapper;

import java.io.IOException;

/**
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
    public void createAccountBusiness() throws IOException, CipherException, CancelException {
        boolean result = walletApiWrapper.createBusiness();
        if (result) {
            System.out.println("CreateBusiness successful !!");
        } else {
            System.out.println("CreateBusiness failed !!");
        }
    }

    @Test
    public void queryAccountBusiness(){
        Protocol.Account account = walletApiWrapper.queryAccount();
        System.out.println();
    }

}
