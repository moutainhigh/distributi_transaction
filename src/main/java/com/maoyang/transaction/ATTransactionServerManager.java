package com.maoyang.transaction;

import com.alibaba.fastjson.JSONObject;
import com.maoyang.transaction.netty.NettyClient;
import com.maoyang.transaction.util.SpringContextUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 记录事务组
 *
 * @author maoyang
 */
public class ATTransactionServerManager {
    private static NettyClient nettyClient = (NettyClient) SpringContextUtil.getBean("nettyClient");

    private static Map<String, ATTransaction> transactionCache = new ConcurrentHashMap(16);

    private static AtomicReference<String> groupId = new AtomicReference();

    /**
     * 创建AT事务
     */
    public static String createATTransactionGroup() {
        String groupId = getUUId();

        JSONObject transaction = new JSONObject();
        transaction.put("command", "create");
        transaction.put("groupId", groupId);

        nettyClient.writeMsg(transaction.toJSONString());
        return groupId;
    }

    /**
     * 创建事务
     *
     * @param groupId
     * @param ATTransactionType
     * @return
     */
    public static ATTransaction createATTransaction(String groupId, ATTransactionType ATTransactionType) {
        String transactionId = getUUId();
        ATTransaction atTransaction = new ATTransaction(transactionId, groupId, ATTransactionType);
        transactionCache.put(groupId, atTransaction);
        return atTransaction;
    }

    /**
     * 新增事务
     *
     * @param atTransaction
     */
    public static void addATTransaction(ATTransaction atTransaction) {
        JSONObject transaction = new JSONObject();
        transaction.put("command", "add");
        transaction.put("groupId", atTransaction.getGroupId());
        transaction.put("transactionId", atTransaction.getTransactionId());
        transaction.put("transactionType", atTransaction.getATTransactionType());


        nettyClient.writeMsg(transaction.toJSONString());
    }

    public static ATTransaction getATTransaction(String groupId) {
        return transactionCache.get(groupId);
    }

    /**
     * 获取事务组ID
     *
     * @return
     */
    public static String getUUId() {
        return UUID.randomUUID().toString();
    }

    public static String getGroupId() {
        return groupId.get();
    }

    public static void setGroupId(String groupId) {
        ATTransactionServerManager.groupId.set(groupId);
    }

    public static void clearGroupId() {
        ATTransactionServerManager.groupId.set("");
    }
}
