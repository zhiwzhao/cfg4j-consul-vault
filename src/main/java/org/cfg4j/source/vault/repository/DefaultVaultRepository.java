package org.cfg4j.source.vault.repository;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;

public class DefaultVaultRepository implements VaultRepository {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultVaultRepository.class);
    private static final String SECRET="secret/";//TODO:move to consul KV
    private Vault vault = null;
    private String env;
    public DefaultVaultRepository() {
    }

    @Override
    public String getSecret(String key) {
        
        if(this.vault==null){
            LOG.error("vault is not initalizated!");
            throw new RuntimeException("vault is not initalizated!");
        }
        String content = null;
        try {
            String onlyKey = key;
            onlyKey=key.substring(SECRET.length());
            LOG.info("key is:{},onlykey is:{}",key,onlyKey);
            Map<String, String> dataSet=vault.logical().read(key).getData();
            LOG.info("temp size:{}",dataSet.size());
            for (String key1 : dataSet.keySet()){
                LOG.info("key:{},value:{}",key1,dataSet.get(key1));
            }
            content=dataSet.get(onlyKey);
            LOG.info("content:{}",content);
        } catch (VaultException e) {
            e.printStackTrace();
            LOG.error("unable to fetch value from vault,exception:{}", e);
        }
        return content;
    }

    @Override
    public void setVaultClient(Vault vault) {
        this.vault = vault;
    }

    @Override
    public void setConsulEnvironment(String env) {
       this.env=env;
        
    }

    @Override
    public String getConsulEnvironment() {
        return env;
    }
}

