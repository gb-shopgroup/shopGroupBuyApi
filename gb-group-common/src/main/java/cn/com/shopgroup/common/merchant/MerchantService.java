package cn.com.shopgroup.common.merchant;

import cn.com.shopgroup.common.cache.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class MerchantService {


    private static final String MERCHANT_HASH_KEY = "merchant:";


    private static final String MERCHANT_ZSET_KEY = "merchantzset:%d:%s";


    private static final long MERCHANT_ZSET_EXPIRE_DAYS = 30;

    @Autowired
    private RedisHelper redisHelper;


    private String getCurrentMonth() {

        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }


    private String getMerchantMonthZSetKey(Long leaderId) {

        return String.format(MERCHANT_ZSET_KEY, leaderId, getCurrentMonth());
    }


    public void initMerchantInfo(Long busId, String merchantNo) {

        if (isMerchantInfoExist(busId)) return;
        String hashKey = MERCHANT_HASH_KEY + busId;
        redisHelper.putHashMap(hashKey, "id", String.valueOf(busId));
        redisHelper.putHashMap(hashKey, "name", merchantNo);
        redisHelper.expireByDay(hashKey, MERCHANT_ZSET_EXPIRE_DAYS);
    }


    public void initMerchantMoney(Long leaderId, Long busId, Integer money) {

        if (isMerchantMoneyExist(leaderId, busId)) return;
        String zSetKey = getMerchantMonthZSetKey(leaderId);
        Boolean isFirstAdd = redisHelper.addZSetItem(zSetKey, busId.intValue(), money);
        if (isFirstAdd) redisHelper.expireByDay(zSetKey, MERCHANT_ZSET_EXPIRE_DAYS);
    }


    public void removeMerchantMoney(Long leaderId, Long busId) {

        if (!isMerchantMoneyExist(leaderId, busId)) return;
        String zSetKey = getMerchantMonthZSetKey(leaderId);
        redisHelper.removeZSetItem(zSetKey, busId.intValue());
    }


    public void addMerchantMoney(Long leaderId, Long busId, Integer money) {

        if (!isMerchantMoneyExist(leaderId, busId)) return;
        String zSetKey = getMerchantMonthZSetKey(leaderId);
        redisHelper.incrementZSetItemScore(zSetKey, busId.intValue(), money);
    }


    public MerchantInfo getLeaderMonthMinMoneyMerchantInfo(Long leaderId) {


        String zSetKey = getMerchantMonthZSetKey(leaderId);
        Integer busId = redisHelper.getMiniIdFromZset(zSetKey);
        if (busId == null || busId == 0) return null;


        String hashKey = MERCHANT_HASH_KEY + busId;
        Object hashVal = redisHelper.getHashMap(hashKey, "name");
        if (hashVal == null) {
            return null;
        }
        String merchantNo = hashVal.toString();
        if (merchantNo == null || merchantNo.length() == 0) {
            return null;
        }


        MerchantInfo merchantInfo = new MerchantInfo();
        merchantInfo.setBusId(busId.longValue());
        merchantInfo.setMerchantNo(merchantNo);
        return merchantInfo;
    }


    public MerchantInfo getLeaderMerchantInfo(Long leaderId, Long busId) {


        String hashKey = MERCHANT_HASH_KEY + busId;
        Object hashVal = redisHelper.getHashMap(hashKey, "name");
        if (hashVal == null) {
            return null;
        }
        String merchantNo = hashVal.toString();
        if (merchantNo == null || merchantNo.length() == 0) {
            return null;
        }


        String zSetKey = getMerchantMonthZSetKey(leaderId);
        Double money = redisHelper.getZSetItemScore(zSetKey, busId.intValue());


        MerchantInfo merchantInfo = new MerchantInfo();
        merchantInfo.setBusId(busId);
        merchantInfo.setMerchantNo(merchantNo.toString());
        merchantInfo.setMoney(money == null ? 0 : money.intValue());
        return merchantInfo;
    }


    public boolean isMerchantInfoExist(Long busId) {

        String hashKey = MERCHANT_HASH_KEY + busId;
        return Boolean.TRUE.equals(redisHelper.hasKey(hashKey));
    }


    public boolean isMerchantMoneyExist(Long leaderId, Long busId) {

        String zSetKey = getMerchantMonthZSetKey(leaderId);
        return Boolean.TRUE.equals(redisHelper.isZSetItem(zSetKey, busId.intValue()));
    }


}
