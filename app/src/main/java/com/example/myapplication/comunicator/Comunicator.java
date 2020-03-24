package com.example.myapplication.comunicator;
import redis.clients.jedis.Jedis;

public class Comunicator {
    private Jedis client;
    public boolean connect()  {
        client=new Jedis("ssh.ipv4.rin.anzupop.com",6379);
        try{
            client.ping();
        }
        catch(Exception e){
            return false;
        }
        return true;
    }
    public boolean ping(){//连接测试
        try{
            client.ping();
        }
        catch(Exception e){
            return false;
        }
        return true;
    }
    public boolean logOff(String userName)//删除账号，调试用接口
    {
        try{
            client.ping();
        }
        catch(Exception e){
            return false;
        }
        if(client.exists(userName)){
            client.del(userName);
            client.del("$"+userName);
            return true;
        }
        return false;
    }
    public void close() {
        client.close();
    }
    public boolean checkUserName(String name){
        if(name.isEmpty()||name.charAt(0)=='$') return false;
        return client.exists(name);
    }
    public boolean signUp(String userName,String password)
    {
        try{
            client.ping();
        }
        catch(Exception e){
            return false;
        }
        if(client.exists(userName)) return false;
        client.set(userName,password);
        return true;
    }
    public boolean signIn(String userName,String password)
    {
        try{
            client.ping();
        }
        catch(Exception e){
            return false;
        }
        if(client.exists(userName)){
            if(client.get(userName).equals(password))
            {
                System.out.println(client.get(userName));
                return true;
            }
            else return false;
        }
        return false;
    }

    public String unlock(String userName)//连接超时或用户名不存在则返回空串
    {
        try{
            client.ping();
        }
        catch(Exception e){
            return "";
        }
        if(client.exists(userName)){
            String lockNumber= client.get("$"+userName);
            client.del("$"+userName);//注销锁
            return lockNumber;
        }
        return "";
    }

    public boolean lock(String userName,String lockNumber)
    {
        try{
            client.ping();
        }
        catch(Exception e){
            return false;
        }
        if(client.exists(userName)){
            client.set('$'+userName,lockNumber);//注册锁
            return true;
        }
        return false;
    }
}
