package org.nervos.neuron.service;


import org.nervos.neuron.R;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.util.LogUtil;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static org.nervos.neuron.util.ConstantUtil.ETHDecimal;

public class EthNativeRpcService extends EthRpcService{

    public static EthGetBalance getEthBalance(String address) {
        try {
            return service.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static TokenItem getDefaultEth(String address) {
        EthGetBalance ethGetBalance = EthNativeRpcService.getEthBalance(address);
        if (ethGetBalance != null) {
            double balance = ethGetBalance.getBalance().multiply(BigInteger.valueOf(10000))
                    .divide(ETHDecimal).doubleValue()/10000.0;
            LogUtil.d("eth balanceOf: " + balance);
            return new TokenItem(ETH, R.drawable.ethereum, balance, -1);
        }
        return null;
    }

    public static Observable<BigInteger> getEthGasPrice() {
        return Observable.fromCallable(new Callable<BigInteger>() {
            @Override
            public BigInteger call() {
                BigInteger gasPrice = Numeric.toBigInt("0x4E3B29200");
                try {
                    gasPrice = service.ethGasPrice().send().getGasPrice();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LogUtil.d("gasPrice: " + gasPrice.toString());
                return gasPrice;
            }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<EthSendTransaction> transferEth(String address, double value, BigInteger gasPrice) {
        return Observable.fromCallable(new Callable<BigInteger>() {
            @Override
            public BigInteger call() throws Exception {
                EthGetTransactionCount ethGetTransactionCount = service
                        .ethGetTransactionCount(walletItem.address, DefaultBlockParameterName.LATEST).send();
                return ethGetTransactionCount.getTransactionCount();
            }
        }).flatMap(new Func1<BigInteger, Observable<String>>() {
            @Override
            public Observable<String> call(BigInteger nonce) {
                Credentials credentials = Credentials.create(walletItem.privateKey);
                BigInteger transferValue = ETHDecimal
                        .multiply(BigInteger.valueOf((long)(10000*value))).divide(BigInteger.valueOf(10000));
                RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce,
                        gasPrice, GAS_LIMIT, address, transferValue);
                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                return Observable.just(Numeric.toHexString(signedMessage));
            }
        }).flatMap(new Func1<String, Observable<EthSendTransaction>>() {
            @Override
            public Observable<EthSendTransaction> call(String hexValue){
                try {
                    EthSendTransaction ethSendTransaction =
                            service.ethSendRawTransaction(hexValue).sendAsync().get();
                    return Observable.just(ethSendTransaction);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }


    public static void getTransactionReceipt(String hash) {
        Observable.fromCallable(new Callable<EthGetTransactionReceipt>() {
            @Override
            public EthGetTransactionReceipt call() {
                try {
                    return service.ethGetTransactionReceipt(hash).send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<EthGetTransactionReceipt>() {
            @Override
            public void onCompleted() {

            }
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
            @Override
            public void onNext(EthGetTransactionReceipt ethGetTransactionReceipt) {
                LogUtil.d("transaction receipt: " + ethGetTransactionReceipt.getTransactionReceipt());
            }
        });
    }





}
