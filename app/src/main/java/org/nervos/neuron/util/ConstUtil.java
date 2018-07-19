package org.nervos.neuron.util;

import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class ConstUtil {

    public static final String ETH = "ETH";
    public static final String NOS = "NOS";

    public static final BigInteger DECIMAL_POW_18 = BigInteger.TEN.pow(18);
    public static final BigInteger ETHDecimal = DECIMAL_POW_18;
    public static final BigInteger NervosDecimal = DECIMAL_POW_18;
    public static final long LONG_6 = 1000000;
    public static final double DOUBLE_6 = 1000000.0;

    public static final String ETH_MAIN_NET = "Ethereum Mainnet";
    public static final int ETH_CHAIN_ID = -1;
    public static final String SOURCE_CODE_GITHUB_URL = "https://github.com/cryptape/Neuron-Android";
    public static final String PRODUCT_AGREEMENT_URL = "https://cryptape.github.io/Neuron-Android/#/zh-CN/product-agreement";
    public static final String CONTACT_US_RUL = "http://appchain.nervos.org//#/contact-us";

    // transaction list page config information
    public static final String ETHER_SCAN_API_KEY = "T9GV1IF4V7YDXQ8F53U1FK2KHCE2KUUD8Z";
    public static final String NERVOS_SERVER_URL = "http://47.97.171.140:4000";
    public static final String NERVOS_TRANSACTION_URL = NERVOS_SERVER_URL + "/api/transactions?account=";
    public static final String ETH_TRANSACTION_URL = "http://api.etherscan.io/api?apikey="
            + ETHER_SCAN_API_KEY + "&module=account&action=txlist&sort=asc&address=";

    // discover page config information
    public static final String DISCOVER_URL = "http://47.97.171.140:8866/dapps";
    public static final String INNER_URL = "http://47.97.171.140:8866/";

    public static final String NERVOS_CHAIN_NAME = "Nervos Chain";
    public static final long DEFAULT_NERVOS_DEFAULT_CHAIN_ID = 1;
    public static final long DEFAULT_QUATO = LONG_6;
    public static final String NERVOS_NODE_IP = "http://121.196.200.225:1337";
//    public static final String ETH_NODE_IP = "https://mainnet.infura.io/h3iIzGIN6msu3KeUrdlt";
    public static final String ETH_NODE_IP = "https://rinkeby.infura.io/llyrtzQ3YhkdESt2Fzrk";

    public static final BigInteger GAS_LIMIT = Numeric.toBigInt("0x5208");  // default eth gas limit is 21000
    public static final BigInteger GAS_ERC20_LIMIT = Numeric.toBigInt("0x23280");  // default eth gas limit is 21000
    public static final BigInteger GAS_PRICE = Numeric.toBigInt("0x4E3B29200");
    public static final String RPC_RESULT_ZERO = "0x";

    public static final String ZERO_16 = "000000000000000000000000";
    public static final String NAME_HASH = "06fdde03";
    public static final String SYMBOL_HASH = "95d89b41";
    public static final String DECIMALS_HASH = "313ce567";
    public static final String BALANCEOF_HASH = "70a08231";

    public static final long VALID_BLOCK_NUMBER_DIFF = 80L;


}
