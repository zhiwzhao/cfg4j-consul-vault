package org.cfg4j.source.consul.vault;

import static java.util.Objects.requireNonNull;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.model.kv.Value;

import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.SourceCommunicationException;
import org.cfg4j.source.context.environment.Environment;
import org.cfg4j.source.vault.repository.VaultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Note: use {@link ConsulVaultConfigurationSourceBuilder} for building
 * instances of this class.
 * <p>
 * Read configuration from the Consul K-V store.
 */
class ConsulVaultConfigurationSource implements ConfigurationSource {

    private static final Logger LOG = LoggerFactory.getLogger(ConsulVaultConfigurationSource.class);
    private static final String VAULT_KEY_PREFIX = "vault#";
    private static final String VAULT_URL = "vault.url";
    private static final String VAULT_ACCESS_TOEKN = "vault.accessToken";
    private static final String VAULT_ACCESS_TIMEOUT = "vault.timeout";
    private KeyValueClient kvClient;
    private Map<String, String> consulValues;
    private final String host;
    private final int port;
    private VaultRepository vaultRepository;

    /**
     * Note: use {@link ConsulVaultConfigurationSourceBuilder} for building
     * instances of this class.
     * <p>
     * Read configuration from the Consul K-V store located at
     * {@code host}:{@code port}.
     *
     * @param host
     *            Consul host to connect to
     * @param port
     *            Consul port to connect to
     */
    ConsulVaultConfigurationSource(String host, int port, VaultRepository vaultRepository) {
        this.host = requireNonNull(host);
        this.port = port;
        this.vaultRepository = vaultRepository;
    }

    @Override
    public Properties getConfiguration(Environment environment) {
        LOG.trace("Requesting configuration for environment: " + environment.getName());

        Properties properties = new Properties();
        String path = environment.getName();

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.length() > 0 && !path.endsWith("/")) {
            path = path + "/";
        }

        for (Map.Entry<String, String> entry : consulValues.entrySet()) {
            if (entry.getKey().startsWith(path)) {
                properties.put(entry.getKey().substring(path.length()).replace("/", "."), entry.getValue());
            }
        }

        return properties;
    }

    /**
     * @throws SourceCommunicationException
     *             when unable to connect to Consul client
     */
    @Override
    public void init() {
        try {
            LOG.info("Connecting to Consul client at " + host + ":" + port);
            
            Consul consul = Consul.builder().withHostAndPort(HostAndPort.fromParts(host, port)).build();
//            Consul consul = Consul.newClient(host, port);
//            Consul consul=Consul.newClient();
            kvClient = consul.keyValueClient();
        } catch (Exception e) {
            throw new SourceCommunicationException("Can't connect to host " + host + ":" + port, e);
        }

        reload();
    }
    @Override
    public void reload() {
        Map<String, String> newConsulValues = new HashMap<>();
        List<Value> valueList;
        String componentEnv = vaultRepository.getConsulEnvironment();
        try {
            LOG.debug("Reloading configuration from Consuls' K-V store");
            valueList = kvClient.getValues(componentEnv);// TODO:valueList =
                                                         // kvClient.getValues("/"+);
            LOG.info("root path:{}",componentEnv);
        } catch (Exception e) {
            throw new SourceCommunicationException("Can't get values from k-v store", e);
        }
        LOG.info("all of vault in consul:{}",valueList.size());
        for (Value value : valueList) {
            String val = "";
            if (value.getValueAsString().isPresent()) {
                val = value.getValueAsString().get();
            }
            LOG.info("Consul provided configuration key: " + value.getKey() + " with value: " + val);
            newConsulValues.put(value.getKey(), val);
        }

        String vaultURL = newConsulValues.get(componentEnv + VAULT_URL);
        String vaultAccessToken = newConsulValues.get(componentEnv + VAULT_ACCESS_TOEKN);
        Integer vaultTimeout = Integer.parseInt(newConsulValues.get(componentEnv + VAULT_ACCESS_TIMEOUT)) ;
        LOG.info("{},{},{}",vaultURL,vaultAccessToken,vaultTimeout);
        initializateVaultClient(vaultURL, vaultAccessToken,vaultTimeout);
        // process by vault
        Map<String, String> mapFromVault = converntVaultProperties(newConsulValues);

        consulValues = mapFromVault;
    }

    private Map<String, String> converntVaultProperties(Map<String, String> map) {
        Map<String, String> vaultMap = new HashMap<String, String>(map.size());
        for (String key : map.keySet()) {
            String value = map.get(key);
            if (value.toLowerCase().startsWith(VAULT_KEY_PREFIX)) {
                String keyInVault = value.substring(VAULT_KEY_PREFIX.length());
                LOG.info("keyinVault:{}",keyInVault);
                String vaultValue = vaultRepository.getSecret(keyInVault);
                if (vaultValue == null) {
                    LOG.error("unable to get secret for key in consul:{}, and key in vault:{}", key, keyInVault);
                    vaultValue = "ERROR";
                }
                vaultMap.put(key, vaultValue);
            } else {
                vaultMap.put(key, value);
            }
        }
        return vaultMap;
    }

    private void initializateVaultClient(String valutURL, String vaultToken,Integer timeout) {
        Vault vault = null;
        try {
            VaultConfig vaultConfig = new VaultConfig().address(valutURL).token(vaultToken).openTimeout(timeout)
                    .readTimeout(timeout).build();
            vault = new Vault(vaultConfig);
        } catch (VaultException e) {
            LOG.error("init vault exception:{}", e);
        }
        this.vaultRepository.setVaultClient(vault);
    }
}
