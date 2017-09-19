package org.pentaho.di.trans.steps.redisoutput;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.util.StringUtil;

import redis.clients.jedis.Jedis;

public class RedisOutputThread implements Runnable{
	private RedisOutput redisOutput;
	private Jedis jedis;
	private Object[] r;
	
	public RedisOutputThread(RedisOutput redisOutput,Jedis jedis,Object[] r){
		this.redisOutput=redisOutput;
		this.jedis=jedis;
		this.r=r;
	}
	@Override
	public void run() {
		long start = System.currentTimeMillis(); 

        // Get value from redis, don't cast now, be lazy. TODO change this?
        int idFieldIndex = redisOutput.getInputRowMeta().indexOfValue(redisOutput.meta.getIdFieldName());
        Object id=r[idFieldIndex];
//        if( redisOutput.rowkey.contains(id)){
//        	 jedis.close();
//        	return;
//        }
        StringBuffer calculate=new StringBuffer();
        for (int i=0;i<r.length;i++) {
        	Object object=r[i];
        	if(object!=null&&i!=idFieldIndex){
        		calculate.append(object);
        	}
		}
        String calculateMD5 = RedisUtil.calculateMD5(calculate.toString());
        String rediskey=redisOutput.tablename+"_id"+"_"+calculateMD5;
        //����MD5ȡ��
        String getstring =jedis.get(rediskey);
        if(getstring!=null&&!StringUtil.isEmpty(getstring)){
        	String idkey=redisOutput.tablename+"_0_"+getstring;
        	String getmd5 =jedis.get(idkey);
        	//������ڽ�״̬����Ϊ1
        	if(getmd5!=null&&!StringUtil.isEmpty(getmd5)){
        		String newidkey=redisOutput.tablename+"_1_"+getstring;
        		jedis.set(newidkey, getmd5);
        		jedis.del(idkey);
        	}
        }else{
        	if(id!=null&&!StringUtil.isEmpty(id+"")){//������޸� ���¼�޸�����md5 
        		jedis.set(rediskey, id+"");
        		String redisidkey=redisOutput.tablename+"_1_"+id;
        		jedis.set(redisidkey, calculateMD5);
        		//�ǿ���������� �������޸Ķ���Ϊ�������ݽ�����һ��
        		//ɸѡ���������ݽ�����һ��
        		try {
					redisOutput.putRow(redisOutput.data.outputRowMeta, r);
//					 redisOutput.rowkey.add(r[idFieldIndex]);
				} catch (KettleStepException e) {
					e.printStackTrace();
				} 
        		// copy row to possible alternate rowset(s).
        	}else{//id�ǿ���Ϊɾ��
        		
        	}

        }

       
        jedis.close();
        long end = System.currentTimeMillis();  
        redisOutput.logBasic(r[0]+"processRow  ���룺"+(end-start));
	}

}
