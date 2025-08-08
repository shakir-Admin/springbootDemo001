package com.example.demo;

import com.example.demo.DTO.entity.Shop;
import com.example.demo.service.impl.ShopServiceImpl;
import com.example.demo.utils.CacheClient;
import com.example.demo.utils.RedisIdWorker;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.example.demo.utils.RedisConstatns.SHOP_ID_KEY;

@SpringBootTest
class SpringbootDemo001ApplicationTests {

	@Resource
	ShopServiceImpl shopService;

	@Resource
	CacheClient cacheClient;

	@Resource
	RedisIdWorker redisIdWorker;

	@Resource
	StringRedisTemplate stringRedisTemplate;

	private ExecutorService es = Executors.newFixedThreadPool(500);

	@Test
	void testRedisIdWorker() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(300);

		Runnable task = () -> {
			for(int i=0; i<100; i++){
				long orderId = redisIdWorker.nextId("order");
				System.out.println("orderId:"+orderId);
			}
			latch.countDown();
		};

		long begin = System.currentTimeMillis();
		for (int j=0; j<300; j++){
			es.submit(task);
		}
		latch.await();
		long end = System.currentTimeMillis();
		System.out.println("time ="+(end - begin));

	}

	@Test
	void testSaveShop() throws InterruptedException {

		Shop shop = shopService.selectByShopId("shopId_f7bd2ac5e13e43b98242a07cf7a17b66");
		String shopKey = SHOP_ID_KEY+shop.getShopId();
		cacheClient.setWithLogicalExpire(shopKey, shop, 20L, TimeUnit.SECONDS);

		//shopService.saveShop2Redis("shopId_f7bd2ac5e13e43b98242a07cf7a17b66",10L);

	}

	@Test
	void testHyperLogLog(){
		String[] values = new String[1000];
		int j = 0;
		for(int i=0; i<1000000; i++) {
			j = i % 1000;
			values[j] = "user_"+i;
			if(j == 999){
				stringRedisTemplate.opsForHyperLogLog().add("hl2", values);
			}
		}

		Long size = stringRedisTemplate.opsForHyperLogLog().size("hl2");
		System.out.println("hl2 size:"+size);
	}

}
