package org.cfg4j.source.consul.vault;

import org.cfg4j.source.vault.repository.VaultRepository;


/**
 * Builder for {@link ConsulVaultConfigurationSource}.
 */
public class ConsulVaultConfigurationSourceBuilder {

  private String host;
  private int port;
  //private VaultRepository vaultRepository;

  /**
   * Construct {@link ConsulVaultConfigurationSource}s builder
   * <p>
   * Default setup (override using with*() methods)
   * <ul>
   * <li>host: localhost</li>
   * <li>port: 8500</li>
   * </ul>
   */
  public ConsulVaultConfigurationSourceBuilder() {
    host = "localhost";
    port = 8500;
  }

  /**
   * Set Consul host for {@link ConsulVaultConfigurationSource}s built by this builder.
   *
   * @param host host to use
   * @return this builder with Consul host set to provided parameter
   */
  public ConsulVaultConfigurationSourceBuilder withHost(String host) {
    this.host = host;
    return this;
  }

  /**
   * Set Consul port for {@link ConsulVaultConfigurationSource}s built by this builder.
   *
   * @param port port to use
   * @return this builder with Consul port set to provided parameter
   */
  public ConsulVaultConfigurationSourceBuilder withPort(int port) {
    this.port = port;
    return this;
  }
  
//  public ConsulVaultConfigurationSourceBuilder withVaultRepository(VaultRepository vaultRepository) {
//      this.vaultRepository=vaultRepository;
//      return this;
//    }

  /**
   * Build a {@link ConsulVaultConfigurationSource} using this builder's configuration
   *
   * @return new {@link ConsulVaultConfigurationSource}
   */
  public ConsulVaultConfigurationSource build(VaultRepository vaultRepository) {
     //this.vaultRepository=vaultRepository;
    return new ConsulVaultConfigurationSource(host, port,vaultRepository);
  }

  @Override
  public String toString() {
    return "ConsulVaultConfigurationSource{" +
        "host=" + host +
        ", port=" + port +
        '}';
  }
}
