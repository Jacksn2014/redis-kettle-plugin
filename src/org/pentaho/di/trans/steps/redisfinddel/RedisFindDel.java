package org.pentaho.di.trans.steps.redisfinddel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
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
public class RedisFindDel extends BaseStep implements StepInterface {
	private static Class<?> PKG = RedisFindDelMeta.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

	protected RedisFindDelMeta meta;
	protected RedisFindDelData data;
	String keytype="string" ;
	String mastername ;
	String tablename ;
	String valueFieldName;
	public RedisFindDel(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
			Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	private static JedisSentinelPool pool = null;

	@Override
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		if (super.init(smi, sdi)) {
			try {
				// Create client and connect to redis server(s)
				Set<Map<String,String>> jedisClusterNodes = ((RedisFindDelMeta) smi).getServers();
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
				String masterName =((RedisFindDelMeta) smi).getMasterName();
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
				logError(BaseMessages.getString(PKG, "RedisFindDel.Error.ConnectError"), e);
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		Jedis jedis = pool.getResource();
		meta = (RedisFindDelMeta) smi;
		data = (RedisFindDelData) sdi;
		if (!data.hasNext) {
			setOutputDone();
			return false;
		}
		if (first) {
			first = false;
			valueFieldName = environmentSubstitute(meta.getValueFieldName());
			logBasic("valueFieldName:"+valueFieldName);
			tablename = environmentSubstitute(meta.getTableName());
            logBasic("tablename:"+tablename);
			mastername = meta.getMasterName();
			Set<String> keys = jedis.keys(tablename+"_0_*");
			data.allrow = keys.iterator();

			//���������е�Ԫ���ݣ�������Ϊ����е�Ԫ���ݡ�
			//����һ���µ�����С�

			RowMetaInterface rowMeta = new RowMeta();

			Object[] rowData = new Object[1];

			int valtype = ValueMeta.getType("String");

			ValueMetaInterface valueMeta = new ValueMeta( valueFieldName, valtype);

			// ���ֶ����� FileName1 �������� String

			valueMeta.setLength(-1);

			rowMeta.addValueMeta(valueMeta);

			RowMetaAndData metaAndData = new RowMetaAndData(rowMeta, rowData);

			data.outputRowMeta= metaAndData.getRowMeta();
		}
		if(!data.allrow.hasNext()){
			setOutputDone();
			return false;
		}
		String key =data.allrow.next();
		data.hasNext=data.allrow.hasNext();
		String rediskey=tablename+"_id"+"_"+jedis.get(key);
		String id=jedis.get(rediskey);

		//���µ�����׷�ӵ�ԭ���������ݵĺ��棬��Ϊ�µ������

		Object[] values = new Object[1];

		values[0]=id;


		//������е�Ԫ���ݺ����ݷŵ������������һ��������Զ�ȡ�ˣ�ע��Ԫ���ݵĸ��������ݵĸ���Ҫ��ȡ�

		putRow(data.outputRowMeta, values);

		if (checkFeedback(getLinesRead())) {
			if (log.isBasic()) {
				logBasic(BaseMessages.getString(PKG, "RedisFindDel.Log.LineNumber") + getLinesRead());
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
