import com.redis.RedisPoolManager;
import com.redis.structure.Redis;
import com.redis.structure.RedisHash;
import com.redis.structure.RedisList;
import com.redis.structure.RedisSet;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 谢俊权
 * @create 2016/5/18 17:48
 */
public class Main {

    public static void main(String[] args){


        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMaxIdle(20);
        jedisPoolConfig.setMinIdle(20);
        jedisPoolConfig.setMaxWaitMillis(1000*5);
        RedisPoolManager.getInstance().init(jedisPoolConfig, "127.0.0.1", 6380, 1000*5);

        setTest();
    }

    public static void listTest(){
        RedisList<User> redisList = Redis.list("user-list", User.class);

        User user1 = new User(1, "da");
        User user2 = new User(2, "hu");
        User user3 = new User(3, "zi");
        redisList.push(user1);
        redisList.push(user2);
        redisList.push(user3);
        redisList.sync();

        List<User> list = redisList.range(0, 2);
        for (User user : list) {
            System.out.println(user.getName());
        }
    }

    public static void setTest(){

        RedisSet<User> redisSet = Redis.set("user-set", User.class);

        User user1 = new User(1, "da");
        User user2 = new User(2, "hu");
        User user3 = new User(3, "zi");

        redisSet.add(user1);
        redisSet.add(user1);
        redisSet.add(user2);
        redisSet.add(user2);
        redisSet.add(user3);
        redisSet.add(user3);
        redisSet.sync();

        Set<User> set = redisSet.members();
        Iterator<User> it = set.iterator();
        while(it.hasNext()){
            User user = it.next();
            System.out.println(user.getName());
        }

    }

    public static void hashTest() {

        RedisHash redisHash = Redis.hash("user-hash", User.class);

        User user1 = new User(1, "da");
        User user2 = new User(2, "hu");
        User user3 = new User(3, "zi");
        redisHash.set("1", user1);
        redisHash.set("2", user2);
        redisHash.set("3", user3);
        redisHash.sync();


        Map<String, User> map = redisHash.get();
        Iterator<String> it = map.keySet().iterator();
        while(it.hasNext()){
            String key = it.next();
            User user = map.get(key);
            System.out.println(user.getName());
        }
    }
}
