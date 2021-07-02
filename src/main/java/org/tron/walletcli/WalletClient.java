package org.tron.walletcli;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.util.encoders.Hex;
import org.tron.common.utils.AbiUtil;
import org.tron.common.utils.Utils;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.core.exception.EncodingException;
import org.tron.protos.Protocol;
import org.tron.walletserver.WalletApi;

import java.io.IOException;

/**
 * @author Brian
 * @date 2021/6/18 10:35
 */
@Slf4j
public class WalletClient {

    private WalletApiWrapper walletApiWrapper = new WalletApiWrapper();

    // 首先，商家登陆
    public boolean login() throws IOException, CipherException {
        boolean result = walletApiWrapper.login();
        if (result) {
            logger.info("Login successful !!!");
        } else {
            logger.info("Login failed !!!");
        }
        return result;
    }

    // 查询账户的燃料费
    private void getBalance(String[] parameters) {
        Protocol.Account account;
        if (ArrayUtils.isEmpty(parameters)) {
            account = walletApiWrapper.queryAccount();
        } else if (parameters.length == 1) {
            byte[] addressBytes = WalletApi.decodeFromBase58Check(parameters[0]);
            if (addressBytes == null) {
                return;
            }
            account = WalletApi.queryAccount(addressBytes);
        } else {
            System.out.println("GetBalance needs no parameter or 1 parameter like the following: ");
            System.out.println("GetBalance Address ");
            return;
        }

        if (account == null) {
            System.out.println("GetBalance failed !!!!");
        } else {
            long balance = account.getBalance();
            System.out.println("Balance = " + balance);
        }
    }

    // 查询账户所有信息
    private void getAccount(String[] parameters) {
        if (parameters == null || parameters.length != 1) {
            System.out.println("GetAccount needs 1 parameter like the following: ");
            System.out.println("GetAccount Address ");
            return;
        }
        String address = parameters[0];
        byte[] addressBytes = WalletApi.decodeFromBase58Check(address);
        if (addressBytes == null) {
            return;
        }

        Protocol.Account account = WalletApi.queryAccount(addressBytes);
        if (account == null) {
            System.out.println("GetAccount failed !!!!");
        } else {
            System.out.println(Utils.formatMessageString(account));
        }
    }

    // 创建账户
    private void createAccount(String[] parameters)
            throws CipherException, IOException, CancelException {
        if (parameters == null || (parameters.length != 1 && parameters.length != 2)) {
            System.out.println("CreateAccount needs 1 parameter using the following syntax: ");
            System.out.println("CreateAccount [OwnerAddress] Address");
            return;
        }

        int index = 0;
        byte[] ownerAddress = null;
        if (parameters.length == 2) {
            ownerAddress = WalletApi.decodeFromBase58Check(parameters[index++]);
            if (ownerAddress == null) {
                System.out.println("Invalid OwnerAddress.");
                return;
            }
        }

        byte[] address = WalletApi.decodeFromBase58Check(parameters[index++]);
        if (address == null) {
            System.out.println("Invalid Address.");
            return;
        }

        String identity = "";

        boolean result = walletApiWrapper.createAccount(address, identity);
        if (result) {
            System.out.println("CreateAccount successful !!");
        } else {
            System.out.println("CreateAccount failed !!");
        }
    }

    // 商家发布NFT，部署合约
    private void deployContract(String[] parameter)
            throws IOException, CipherException, CancelException {

//        String[] parameters = getParas(parameter);
//        if (parameters == null ||
//                parameters.length < 11) {
//            System.out.println("Using deployContract needs at least 11 parameters like: ");
//            System.out.println(
//                    "DeployContract [ownerAddress] contractName ABI byteCode constructor params isHex fee_limit consume_user_resource_percent origin_energy_limit value token_value token_id(e.g: TRXTOKEN, use # if don't provided) <library:address,library:address,...> <lib_compiler_version(e.g:v5)>");
////      System.out.println(
////          "Note: Please append the param for constructor tightly with byteCode without any space");
//            return;
//        }
//
//        int idx = 0;
//        byte[] ownerAddress = getAddressBytes(parameters[idx]);
//        if (ownerAddress != null) {
//            idx++;
//        }
//
//        String contractName = parameters[idx++];
//        String abiStr = parameters[idx++];
//        String codeStr = parameters[idx++];
//        String constructorStr = parameters[idx++];
//        String argsStr = parameters[idx++];
//        boolean isHex = Boolean.parseBoolean(parameters[idx++]);
//        long feeLimit = Long.parseLong(parameters[idx++]);
//        long consumeUserResourcePercent = Long.parseLong(parameters[idx++]);
//        long originEnergyLimit = Long.parseLong(parameters[idx++]);
//        if (consumeUserResourcePercent > 100 || consumeUserResourcePercent < 0) {
//            System.out.println("consume_user_resource_percent should be >= 0 and <= 100");
//            return;
//        }
//        if (originEnergyLimit <= 0) {
//            System.out.println("origin_energy_limit must > 0");
//            return;
//        }
//        if (!constructorStr.equals("#")) {
//            if (isHex) {
//                codeStr += argsStr;
//            } else {
//                codeStr += Hex.toHexString(AbiUtil.encodeInput(constructorStr, argsStr));
//            }
//        }
//        long value = 0;
//        value = Long.valueOf(parameters[idx++]);
//        long tokenValue = Long.valueOf(parameters[idx++]);
//        String tokenId = parameters[idx++];
//        if (tokenId == "#") {
//            tokenId = "";
//        }
//        String libraryAddressPair = null;
//        if (parameters.length > idx) {
//            libraryAddressPair = parameters[idx++];
//        }
//
//        String compilerVersion = null;
//        if (parameters.length > idx) {
//            compilerVersion = parameters[idx];
//        }
//
//        // TODO: consider to remove "data"
//        /* Consider to move below null value, since we append the constructor param just after bytecode without any space.
//         * Or we can re-design it to give other developers better user experience. Set this value in protobuf as null for now.
//         */
//        boolean result = walletApiWrapper
//                .deployContract(ownerAddress, contractName, abiStr, codeStr, feeLimit, value,
//                        consumeUserResourcePercent, originEnergyLimit, tokenValue, tokenId, libraryAddressPair,
//                        compilerVersion);
//        if (result) {
//            System.out.println("Broadcast the createSmartContract successful.\n"
//                    + "Please check the given transaction id to confirm deploy status on blockchain using getTransactionInfoById command.");
//        } else {
//            System.out.println("Broadcast the createSmartContract failed !!!");
//        }
    }

    // 调用合约转账NFT

    // 用户购买平台Token

    // 用户使用平台Token兑换燃料

    // 调用合约，private
    private void triggerContract(String[] parameters, boolean isConstant)
            throws IOException, CipherException, CancelException, EncodingException {
        String cmdMethodStr = isConstant ? "TriggerConstantContract" : "TriggerContract";

        if (isConstant) {
            if (parameters == null || (parameters.length != 4 && parameters.length != 5)) {
                System.out.println(cmdMethodStr + " needs 4 or 5 parameters like: ");
                System.out.println(cmdMethodStr + " [OwnerAddress] contractAddress method args isHex");
                return;
            }
        } else {
            if (parameters == null || (parameters.length != 8 && parameters.length != 9)) {
                System.out.println(cmdMethodStr + " needs 8 or 9 parameters like: ");
                System.out.println(cmdMethodStr + " [OwnerAddress] contractAddress method args isHex"
                        + " fee_limit value token_value token_id(e.g: TRXTOKEN, use # if don't provided)");
                return;
            }
        }

        int index = 0;
        byte[] ownerAddress = null;
        if (parameters.length == 5 || parameters.length == 9) {
            ownerAddress = WalletApi.decodeFromBase58Check(parameters[index++]);
            if (ownerAddress == null) {
                System.out.println("Invalid OwnerAddress.");
                return;
            }
        }

        String contractAddrStr = parameters[index++];
        String methodStr = parameters[index++];
        String argsStr = parameters[index++];
        boolean isHex = Boolean.valueOf(parameters[index++]);
        long feeLimit = 0;
        long callValue = 0;
        long tokenCallValue = 0;
        String tokenId = "";

        if (!isConstant) {
            feeLimit = Long.valueOf(parameters[index++]);
            callValue = Long.valueOf(parameters[index++]);
            tokenCallValue = Long.valueOf(parameters[index++]);
            tokenId = parameters[index++];
        }
        if (argsStr.equalsIgnoreCase("#")) {
            argsStr = "";
        }
        if (tokenId.equalsIgnoreCase("#")) {
            tokenId = "";
        }
        byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, argsStr, isHex));
        byte[] contractAddress = WalletApi.decodeFromBase58Check(contractAddrStr);

        /*boolean result = walletApiWrapper
                .callContract(ownerAddress, contractAddress, callValue, input, feeLimit, tokenCallValue,
                        tokenId,
                        isConstant);
        if (!isConstant) {
            if (result) {
                System.out.println("Broadcast the " + cmdMethodStr + " successful.\n"
                        + "Please check the given transaction id to get the result on blockchain using getTransactionInfoById command");
            } else {
                System.out.println("Broadcast the " + cmdMethodStr + " failed");
            }
        }*/
    }
}
