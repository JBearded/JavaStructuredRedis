import com.bj.redis.structure.RedisList;

/**
 * @author 谢俊权
 * @create 2016/5/18 17:48
 */
public class Main {

    public static void main(String[] args){

        RedisList.InternalList list = RedisList.key("users");
        //业务处理
        String result1 = "kevin";   //处理结果
        list.add(result1);
        //业务处理
        String result2 = "jun";   //处理结果
        list.add(result2);

        list.sync();
    }
}
