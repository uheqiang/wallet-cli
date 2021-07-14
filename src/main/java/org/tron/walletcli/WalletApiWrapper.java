package org.tron.walletcli;

import ch.qos.logback.core.pattern.color.BoldYellowCompositeConverter;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.*;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.keystore.StringUtils;
import org.tron.keystore.WalletFile;
import org.tron.protos.Contract;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Protocol.*;
import org.tron.walletserver.WalletApi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
public class WalletApiWrapper {

  private WalletApi wallet;

  public String registerWallet(char[] password) throws CipherException, IOException {
    if (!WalletApi.passwordValid(password)) {
      return null;
    }

    byte[] passwd = StringUtils.char2Byte(password);

    WalletFile walletFile = WalletApi.CreateWalletFile(passwd);
    StringUtils.clear(passwd);

    String keystoreName = WalletApi.store2Keystore(walletFile);
//    logout();
    return keystoreName;
  }

  public String importWallet(char[] password, byte[] priKey) throws CipherException, IOException {
    if (!WalletApi.passwordValid(password)) {
      return null;
    }
    if (!WalletApi.priKeyValid(priKey)) {
      return null;
    }

    byte[] passwd = StringUtils.char2Byte(password);

    WalletFile walletFile = WalletApi.CreateWalletFile(passwd, priKey);
    StringUtils.clear(passwd);

    String keystoreName = WalletApi.store2Keystore(walletFile);
//    logout();
    return keystoreName;
  }

  public boolean changePassword(char[] oldPassword, char[] newPassword)
      throws IOException, CipherException {
//    logout();
    if (!WalletApi.passwordValid(newPassword)) {
      System.out.println("Warning: ChangePassword failed, NewPassword is invalid !!");
      return false;
    }

    byte[] oldPasswd = StringUtils.char2Byte(oldPassword);
    byte[] newPasswd = StringUtils.char2Byte(newPassword);

    boolean result = WalletApi.changeKeystorePassword(oldPasswd, newPasswd);
    StringUtils.clear(oldPasswd);
    StringUtils.clear(newPasswd);

    return result;
  }

  public boolean isLoginState() {
    if (wallet == null || !wallet.isLoginState()) {
      return false;
    } else {
      return true;
    }
  }

  public boolean initConfig() throws IOException, CipherException {
    WalletApi.init();
    wallet = new WalletApi();
    return true;
  }

//  public boolean login() throws IOException, CipherException {
//    logout();
//    wallet = WalletApi.loadWalletFromKeystore();
//    char[] password = Utils.inputPassword(false);
//    byte[] passwd = StringUtils.char2Byte(password);
//    StringUtils.clear(password);
//    wallet.checkPassword(passwd);
//    StringUtils.clear(passwd);
//
//    if (wallet == null) {
//      System.out.println("Warning: Login failed, Please registerWallet or importWallet first !!");
//      return false;
//    }
//    wallet.setLogin();
//    return true;
//  }
//
//  public void logout() {
//    if (wallet != null) {
//      wallet.logout();
//      wallet = null;
//    }
//    //Neddn't logout
//  }



  //password is current, will be enc by password2.
  public byte[] backupWallet() throws IOException, CipherException {
    if (wallet == null || !wallet.isLoginState()) {
      wallet = WalletApi.loadWalletFromKeystore();
      if (wallet == null) {
        System.out.println("Warning: BackupWallet failed, no wallet can be backup !!");
        return null;
      }
    }

    System.out.println("Please input your password.");
    char[] password = Utils.inputPassword(false);
    byte[] passwd = StringUtils.char2Byte(password);
    StringUtils.clear(password);
    byte[] privateKey = WalletApi.getPrivateBytes(passwd);
    StringUtils.clear(passwd);

    return privateKey;
  }

  public Account queryAccount(byte[] address) {
    return WalletApi.queryAccount(address);
  }

  public boolean transferAsset(byte[] ownerAddress, byte[] fromPrivateKey, byte[] toAddress, String assertId, long amount)
          throws IOException, CipherException, CancelException {
    return wallet.transferAsset(ownerAddress, fromPrivateKey, toAddress, assertId.getBytes(), amount);
  }

  /**
   * 发布资产，可以是商家发布Token
   * @param ownerAddress token拥有者
   * @param ownerPriKey owner的私钥
   * @param name token名称
   * @param totalSupply token发布总量
   */
  public boolean assetIssue(byte[] ownerAddress, byte[] ownerPriKey, String name, long totalSupply)
          throws CipherException, IOException, CancelException {
    Contract.AssetIssueContract.Builder builder = Contract.AssetIssueContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(ownerAddress));
    builder.setName(ByteString.copyFrom(name.getBytes()));

    if (totalSupply <= 0) {
      System.out.println("totalSupply should greater than 0. but really is " + totalSupply);
      return false;
    }
    builder.setTotalSupply(totalSupply);
    return wallet.createAssetIssue(builder.build(), ownerPriKey);
  }

  public boolean createAccount(byte[] owner, byte[] ownerPrivateKey, byte[] address, String identity)
      throws CipherException, IOException, CancelException {
    return wallet.createAccount(owner, ownerPrivateKey, address, identity);
  }

  public boolean createBusiness(byte[] address,byte[] privateKey,String identity)
          throws CancelException {
    return wallet.createBusiness(address,privateKey,identity);
  }

  public AddressPrKeyPairMessage generateAddress() {
    return WalletApi.generateAddress();
  }

  public boolean createWitness(byte[] ownerAddress,byte[] privateKey, String url)
      throws CipherException, IOException, CancelException {
    return wallet.createWitness(ownerAddress, privateKey,url.getBytes());
  }

  public boolean updateWitness(byte[] ownerAddress,byte[] privateKey, String url)
      throws CipherException, IOException, CancelException {
    return wallet.updateWitness(ownerAddress, privateKey,url.getBytes());
  }

  public Block getBlock(long blockNum) {
    return WalletApi.getBlock(blockNum);
  }

  public long getTransactionCountByBlockNum(long blockNum) {
    return WalletApi.getTransactionCountByBlockNum(blockNum);
  }

  public BlockExtention getBlock2(long blockNum) {
    return WalletApi.getBlock2(blockNum);
  }

  public boolean voteWitness(byte[] ownerAddress, byte[] password, HashMap<String, String> witness)
      throws CipherException, IOException, CancelException {
    return wallet.voteWitness(ownerAddress, password,witness);
  }

  public Optional<WitnessList> listWitnesses() {
    try {
      return WalletApi.listWitnesses();
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<AssetIssueList> getAssetIssueList() {
    try {
      return WalletApi.getAssetIssueList();
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<AssetIssueList> getAssetIssueList(long offset, long limit) {
    try {
      return WalletApi.getAssetIssueList(offset, limit);
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public AssetIssueContract getAssetIssueByName(String assetName) {
    return WalletApi.getAssetIssueByName(assetName);
  }

  public Optional<AssetIssueList> getAssetIssueListByName(String assetName) {
    try {
      return WalletApi.getAssetIssueListByName(assetName);
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public AssetIssueContract getAssetIssueById(String assetId) {
    return WalletApi.getAssetIssueById(assetId);
  }

  public Optional<ProposalList> getProposalListPaginated(long offset, long limit) {
    try {
      return WalletApi.getProposalListPaginated(offset, limit);
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<NodeList> listNodes() {
    try {
      return WalletApi.listNodes();
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public GrpcAPI.NumberMessage getTotalTransaction() {
    return WalletApi.getTotalTransaction();
  }

  public GrpcAPI.NumberMessage getNextMaintenanceTime() {
    return WalletApi.getNextMaintenanceTime();
  }

  public boolean updateAsset(byte[] ownerAddress, byte[] privateKey, String assetId, long mintTokens)
          throws CipherException, IOException, CancelException {
    return wallet.updateAsset(ownerAddress,privateKey,assetId, mintTokens);
  }

  /**
   * 冻结TRC10资源，获取能量
   * 一个账户可以通过冻结TRC10来获取能量
   * 同时，也可以把冻结TRC10获取的者能量委托（delegate）给其他地址
   * @param ownerAddress trc10拥有者
   * @param ownerPrivateKey 拥有者的私钥
   * @param frozen_balance 冻结的trc10的数量
   * @param receiverAddress 能量接收者，可以是自己，也可以是他人
   */
  public boolean freezeBalance(byte[] ownerAddress, byte[] ownerPrivateKey, long frozen_balance,
                               ByteString assertId, byte[] receiverAddress)
      throws CipherException, IOException, CancelException {
    return wallet.freezeBalance(ownerAddress,
            ownerPrivateKey,
            frozen_balance,
            Contract.ResourceCode.ENERGY_VALUE,
            assertId,
            receiverAddress);
  }

  public boolean createProposal(byte[] ownerAddress,byte[] privateKey, HashMap<Long, Long> parametersMap)
      throws CipherException, IOException, CancelException {
    return wallet.createProposal(ownerAddress,privateKey, parametersMap);
  }


  public Optional<ProposalList> getProposalsList() {
    try {
      return WalletApi.listProposals();
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<Proposal> getProposals(String id) {
    try {
      return WalletApi.getProposal(id);
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<ChainParameters> getChainParameters() {
    try {
      return WalletApi.getChainParameters();
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }


  public boolean approveProposal(byte[] ownerAddress,byte[] privateKey, long id, boolean is_add_approval)
      throws CipherException, IOException, CancelException {
    return wallet.approveProposal(ownerAddress, privateKey,id, is_add_approval);
  }

  public boolean deleteProposal(byte[] ownerAddress,byte[] privateKey, long id)
      throws CipherException, IOException, CancelException {
    return wallet.deleteProposal(ownerAddress,privateKey, id);
  }

  public boolean exchangeCreate(byte[] ownerAddress, byte[] privateKey, byte[] firstTokenId, long firstTokenBalance,
      byte[] secondTokenId, long secondTokenBalance)
      throws CancelException {
    return wallet.exchangeCreate(ownerAddress, privateKey,firstTokenId, firstTokenBalance,
        secondTokenId, secondTokenBalance);
  }

  public boolean exchangeInject(byte[] ownerAddress, byte[] privateKey,long exchangeId, byte[] tokenId, long quant)
      throws CipherException, IOException, CancelException {
    return wallet.exchangeInject(ownerAddress, privateKey,exchangeId, tokenId, quant);
  }

  public boolean exchangeTransaction(byte[] ownerAddress, byte[] ownerPrivatekey,long exchangeId, byte[] tokenId,
      long quant, long expected) throws CancelException {
    return wallet.exchangeTransaction(ownerAddress, ownerPrivatekey,exchangeId, tokenId, quant, expected);
  }

//  public boolean updateEnergyLimit(byte[] ownerAddress, byte[] contractAddress,
//      long originEnergyLimit) throws CipherException, IOException, CancelException {
//    return wallet.updateEnergyLimit(ownerAddress, contractAddress, originEnergyLimit);
//  }

  public String deployContract(byte[] ownerAddress, byte[] ownerPrivatekey, String name,
                               String abiStr, String codeStr, long originEnergyLimit, long feeLimit, long value,
                               long energyPay, Contract.DelegationPay delegationPay,
                               byte[] delegationPrivateKey,
                               long tokenValue, String tokenId, String libraryAddressPair,
                               String compilerVersion) throws CancelException {

    return wallet.deployContract(ownerAddress, ownerPrivatekey, name, abiStr, codeStr, feeLimit, value,
            energyPay, delegationPay, delegationPrivateKey, originEnergyLimit, tokenValue, tokenId,
            libraryAddressPair, compilerVersion);
  }

  public byte[] triggerConstantContract(String ownerBase58, String contractBase58, byte[] data) {
      byte[] ownerAddress = WalletApi.decodeFromBase58Check(ownerBase58);
      byte[] contractAddress = WalletApi.decodeFromBase58Check(contractBase58);
    return wallet.triggerConstantContract(ownerAddress,contractAddress,data);
  }

  public boolean triggerContract(String ownerBase58, String ownerPrivateKey,
                                 String contractBase58, byte[] data, long energyPay,
                                 Contract.DelegationPay delegationPay,
                                 String delegationPrivateKey)
          throws CancelException {
    long originEnergyLimit = 100000L;
      byte[] ownerAddress = WalletApi.decodeFromBase58Check(ownerBase58);
      byte[] ownerKey = ByteArray.fromHexString(ownerPrivateKey);
      byte[] contractAddress = WalletApi.decodeFromBase58Check(contractBase58);
      byte[] delegationKey = ByteArray.fromHexString(delegationPrivateKey);
    return wallet.triggerContract(ownerAddress, ownerKey, contractAddress,
            data,originEnergyLimit,energyPay,delegationPay,delegationKey);
  }
}
