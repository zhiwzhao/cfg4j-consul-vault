package org.cfg4j.source.vault.repository;

import com.bettercloud.vault.Vault;
/***
 * interface for vault repository.
 * using this repository api to consul vault configuration source
 * @author zhiwzhao
 *
 */
public interface VaultRepository {
    String getSecret(String key);
    void setVaultClient(Vault vault);
    void setConsulEnvironment(String env);
    String getConsulEnvironment();
}
