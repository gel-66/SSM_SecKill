package org.seckill.dao;

import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private JedisPool jedisPool;
	public RedisDao(String ip,int port){
		jedisPool = new JedisPool(ip,port);
	}
	//拿到对象的schema格式
	private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);
	//拿到对象的字节数组，并通过sechma反序列化我们需要的对象
	public Seckill getSeckill(long seckillId){
		//redis操作逻辑
		try{
			Jedis jedis = jedisPool.getResource();
			try {
				String key = "seckill:"+seckillId;
				//并没有实现内部序列化操作
				//get->byte[] -> 反序列化 ->Object(Seckill)
				//采用自定义序列化
				//protostuff:projo
				byte[] bytes = jedis.get(key.getBytes());
				//缓存重新获得
				if(bytes != null){
					//空对象
					Seckill seckill = schema.newMessage();
					ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
					return seckill;
				}
			} finally {
				jedis.close();
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	//将拿到的对象通过schema序列化成字节数组
	public String putSeckill(Seckill seckill){
		//set Object(Seckill) -> 序列化 -> byte[]
		try{
			Jedis jedis = jedisPool.getResource();
			try {
				String key = "seckill:"+seckill.getSeckillId();
				byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema, 
						LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
				//超时缓存
				int timeout = 60*60;//1小时
				String result = jedis.setex(key.getBytes(), timeout, bytes);
				return result;
			} finally {
				jedis.close();
			}
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return null;
	}
}
