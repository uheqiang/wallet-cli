package org.tron.walletcli;

import com.google.protobuf.ByteString;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.*;
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
    logout();
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
    logout();
    return keystoreName;
  }

  public boolean changePassword(char[] oldPassword, char[] newPassword)
      throws IOException, CipherException {
    logout();
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

  public boolean login() throws IOException, CipherException {
    logout();
    wallet = WalletApi.loadWalletFromKeystore();

//    System.out.println("Please input your password.");

//    char[] password = Utils.inputPassword(false);
//    byte[] passwd = StringUtils.char2Byte(password);
//    StringUtils.clear(password);
//    wallet.checkPassword();
//    StringUtils.clear(passwd);

    if (wallet == null) {
      System.out.println("Warning: Login failed, Please registerWallet or importWallet first !!");
      return false;
    }
    wallet.setLogin();
    return true;
  }

  public void logout() {
    if (wallet != null) {
      wallet.logout();
      wallet = null;
    }
    //Neddn't logout
  }



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
    byte[] privateKey = wallet.getPrivateBytes(passwd);
    StringUtils.clear(passwd);

    return privateKey;
  }

  public String getAddress() {
    return WalletApi.encode58Check(wallet.getAddress());
  }

  public Account queryAccount() {
    return wallet.queryAccount();
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
    if (ownerAddress == null) {
      ownerAddress = wallet.getAddress();
      System.out.println("ownerAddress: " + WalletApi.encode58Check(ownerAddress));
    }
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

  public String createBusiness() {
    return wallet.createBusiness();
  }

  public AddressPrKeyPairMessage generateAddress() {
    return WalletApi.generateAddress();
  }

  public boolean createWitness(byte[] ownerAddress, String url)
      throws CipherException, IOException, CancelException {
    return wallet.createWitness(ownerAddress, url.getBytes());
  }

  public boolean updateWitness(byte[] ownerAddress, String url)
      throws CipherException, IOException, CancelException {
    return wallet.updateWitness(ownerAddress, url.getBytes());
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

  public boolean voteWitness(byte[] ownerAddress, HashMap<String, String> witness)
      throws CipherException, IOException, CancelException {
    return wallet.voteWitness(ownerAddress, witness);
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

  public boolean updateAccount(byte[] ownerAddress, byte[] accountNameBytes)
      throws CipherException, IOException, CancelException {
    return wallet.updateAccount(ownerAddress, accountNameBytes);
  }

  public boolean updateAsset(byte[] ownerAddress, String assetId, long mintTokens)
          throws CipherException, IOException, CancelException {
    return wallet.updateAsset(ownerAddress,assetId, mintTokens);
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

  public boolean unfreezeBalance(byte[] ownerAddress, int resourceCode, byte[] receiverAddress)
      throws CipherException, IOException, CancelException {
    return wallet.unfreezeBalance(ownerAddress, resourceCode, receiverAddress);
  }


  public boolean unfreezeAsset(byte[] ownerAddress)
      throws CipherException, IOException, CancelException {
    return wallet.unfreezeAsset(ownerAddress);
  }

  public boolean createProposal(byte[] ownerAddress, HashMap<Long, Long> parametersMap)
      throws CipherException, IOException, CancelException {
    return wallet.createProposal(ownerAddress, parametersMap);
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


  public boolean approveProposal(byte[] ownerAddress, long id, boolean is_add_approval)
      throws CipherException, IOException, CancelException {
    return wallet.approveProposal(ownerAddress, id, is_add_approval);
  }

  public boolean deleteProposal(byte[] ownerAddress, long id)
      throws CipherException, IOException, CancelException {
    return wallet.deleteProposal(ownerAddress, id);
  }

  public boolean exchangeCreate(byte[] ownerAddress, byte[] firstTokenId, long firstTokenBalance,
      byte[] secondTokenId, long secondTokenBalance)
      throws CipherException, IOException, CancelException {
    return wallet.exchangeCreate(ownerAddress, firstTokenId, firstTokenBalance,
        secondTokenId, secondTokenBalance);
  }

  public boolean exchangeInject(byte[] ownerAddress, long exchangeId, byte[] tokenId, long quant)
      throws CipherException, IOException, CancelException {
    return wallet.exchangeInject(ownerAddress, exchangeId, tokenId, quant);
  }

  public boolean exchangeWithdraw(byte[] ownerAddress, long exchangeId, byte[] tokenId, long quant)
      throws CipherException, IOException, CancelException {
    return wallet.exchangeWithdraw(ownerAddress, exchangeId, tokenId, quant);
  }

  public boolean exchangeTransaction(byte[] ownerAddress, long exchangeId, byte[] tokenId,
      long quant, long expected) throws CipherException, IOException, CancelException {
    return wallet.exchangeTransaction(ownerAddress, exchangeId, tokenId, quant, expected);
  }

  public boolean updateSetting(byte[] ownerAddress, byte[] contractAddress,
      long consumeUserResourcePercent) throws CipherException, IOException, CancelException {
    return wallet.updateSetting(ownerAddress, contractAddress, consumeUserResourcePercent);

  }

  public boolean updateEnergyLimit(byte[] ownerAddress, byte[] contractAddress,
      long originEnergyLimit) throws CipherException, IOException, CancelException {
    return wallet.updateEnergyLimit(ownerAddress, contractAddress, originEnergyLimit);
  }

  public String deployContract(byte[] ownerAddress, byte[] ownerPrivatekey, String name,
                                String abiStr, String codeStr, long feeLimit, long value,
                                long consumeUserResourcePercent, long energyPay,
                                long tokenValue, String tokenId, String libraryAddressPair,
                                String compilerVersion) throws CancelException {
    long originEnergyLimit = 1000000L;
    return wallet.deployContract(ownerAddress, ownerPrivatekey, name, abiStr, codeStr, feeLimit, value,
            consumeUserResourcePercent, energyPay, originEnergyLimit, tokenValue, tokenId,
            libraryAddressPair, compilerVersion);
  }

  public byte[] triggerConstantContract(byte[] owner, byte[] contractAddress, byte[] data) {
    return wallet.triggerConstantContract(owner,contractAddress,data);
  }

  public boolean triggerContract(byte[] owner, byte[] ownerPrivateKey,
                                 byte[] contractAddress, byte[] data, long energyPay)
          throws CancelException {
    long originEnergyLimit = 100000L;
    return wallet.triggerContract(owner, ownerPrivateKey, contractAddress, data,originEnergyLimit,energyPay);
  }

  // TODO 在这里设置可信节点对用户发起的交易进行签名
  public Transaction addTransactionSign(Transaction transaction)
      throws IOException, CipherException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      System.out.println("Warning: addTransactionSign failed,  Please login first !!");
      return null;
    }
    return wallet.addTransactionSign(transaction);
  }

}
