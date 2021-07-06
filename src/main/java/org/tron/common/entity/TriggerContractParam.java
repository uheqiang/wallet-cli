package org.tron.common.entity;

import lombok.Getter;
import lombok.NonNull;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.utils.AbiUtil;

import java.util.List;

public class TriggerContractParam {

    @NonNull
    private String triggerMethod;

    @NonNull
    private List<Object> args;

    @NonNull
    @Getter
    private long callValue;

    @NonNull
    @Getter
    private boolean isConstant;

    @NonNull
    @Getter
    private byte[] contractAddress;

    private byte[] data;

    public byte[] getData() {
        return Hex.decode(AbiUtil.parseMethod(triggerMethod, args));
    }

    public TriggerContractParam setTriggerMethod(String triggerMethod) {
        this.triggerMethod = triggerMethod;
        return this;
    }

    public TriggerContractParam setArgs(List<Object> args) {
        this.args = args;
        return this;
    }

    public TriggerContractParam setCallValue(long callValue) {
        this.callValue = callValue;
        return this;
    }

    public TriggerContractParam setConstant(boolean constant) {
        isConstant = constant;
        return this;
    }

    public TriggerContractParam setContractAddress(byte[] contractAddress) {
        this.contractAddress = contractAddress;
        return this;
    }
}
