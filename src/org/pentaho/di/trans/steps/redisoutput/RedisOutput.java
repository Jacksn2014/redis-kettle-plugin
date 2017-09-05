package org.pentaho.di.trans.steps.redisoutput;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

/**
 * The Redis Input step looks up value objects, from the given key names, from Redis server(s).
 */
public class RedisOutput extends BaseStep implements StepInterface {
    private static Class<?> PKG = RedisOutputMeta.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

    protected RedisOutputMeta meta;
    protected RedisOutputData data;
    String tablename ;
    String idfieldname ;
    public RedisOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                      Trans trans) {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    private static JedisSentinelPool pool = null;


    @Override
    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        if (super.init(smi, sdi)) {
            try {
                // Create client and connect to redis server(s)
                Set<Map<String,String>> jedisClusterNodes = ((RedisOutputMeta) smi).getServers();
                // �������ӳ����ò���
                JedisPoolConfig config = new JedisPoolConfig();
                // �������������
                config.setMaxTotal(2000);
                // �����������ʱ�䣬��ס�Ǻ�����milliseconds
                config.setMaxWaitMillis(1000);
                // ���ÿռ�����
                config.setMaxIdle(30);
                // jedisʵ���Ƿ����
                config.setTestOnBorrow(true);
                // �������ӳ�
                //��ȡredis����
                String password =null;
                int timeout=1000;
                String masterName =((RedisOutputMeta) smi).getMasterName();
                Set<String> sentinels = new HashSet<String>();
                Iterator<Map<String,String>> it = jedisClusterNodes.iterator(); 
                while (it.hasNext()) {  
                	Map<String,String>  hostAndPort= it.next();  
                	 password =hostAndPort.get("auth");
                	 sentinels.add(hostAndPort.get("hostname")+":"+hostAndPort.get("port"));
				}
                pool = new JedisSentinelPool(masterName, sentinels, config, timeout, password);
                
                
                return true;
            } catch (Exception e) {
                logError(BaseMessages.getString(PKG, "RedisInput.Error.ConnectError"), e);
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
        meta = (RedisOutputMeta) smi;
        data = (RedisOutputData) sdi;
        Jedis jedis = pool.getResource();
        Object[] r = getRow(); // get row, set busy!
        // If no more input to be expected, stop
        if (r == null) {
            setOutputDone();
            return false;
        }

        if (first) {
            first = false;
            // clone input row meta for now, we will change it (add or set inline) later
            data.outputRowMeta = getInputRowMeta().clone();
            // Get output field types
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            tablename =environmentSubstitute(meta.getTableName());
            logBasic("tablename:"+tablename);
        }

        // Get value from redis, don't cast now, be lazy. TODO change this?
        int idFieldIndex = getInputRowMeta().indexOfValue(meta.getIdFieldName());
        if (idFieldIndex < 0) {
            throw new KettleException(BaseMessages.getString(PKG, "RedisOutputMeta.Exception.KeyFieldNameNotFound"));
        }
        Object id=r[idFieldIndex];
        StringBuffer calculate=new StringBuffer();
        for (int i=0;i<r.length;i++) {
        	Object object=r[i];
        	if(object!=null&&i!=idFieldIndex){
        		calculate.append(object);
        	}
		}
        String calculateMD5 = RedisUtil.calculateMD5(calculate.toString());
        String rediskey=tablename+"_id"+"_"+calculateMD5;
        //����MD5ȡ��
        String getstring = jedis.get(rediskey);
        if(getstring!=null&&!StringUtil.isEmpty(getstring)){
        	String idkey=tablename+"_0_"+getstring;
        	String getmd5 =jedis.get(idkey);
        	//������ڽ�״̬����Ϊ1
        	if(getmd5!=null&&!StringUtil.isEmpty(getmd5)){
        		String newidkey=tablename+"_1_"+getstring;
        		jedis.set(newidkey, getmd5);
        		jedis.del(idkey);
        	}
        }else{
        	if(id!=null&&!StringUtil.isEmpty(id+"")){//������޸� ���¼�޸�����md5 
        		jedis.set(rediskey, id+"");
        		String redisidkey=tablename+"_1_"+id;
        		jedis.set(redisidkey, calculateMD5);
        		//�ǿ���������� �������޸Ķ���Ϊ�������ݽ�����һ��
        		//ɸѡ���������ݽ�����һ��
        		putRow(data.outputRowMeta, r); // copy row to possible alternate rowset(s).
        	}else{//id�ǿ���Ϊɾ��
        		
        	}

        }

        if (checkFeedback(getLinesRead())) {
            if (log.isBasic()) {
                logBasic(BaseMessages.getString(PKG, "RedisOutput.Log.LineNumber") + getLinesRead());
            }
        }
        jedis.close();
        return true;
    }
    
    @Override
    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
    	super.dispose(smi, sdi);
    	if(pool!=null){
    		pool.close();
    		pool.destroy();
    	}
    }
}
