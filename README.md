# cfg4j-consul-vault #

## Introduction ##
https://github.com/cfg4j/cfg4j
www.cfg4j.org
**cfg4j** ("Configuration for Java") is a **configuration library for Java distributed apps** (and not only).
Most of users adopt cfg4j-consul to load properties from consul and consume in applications.
In many cases,there are many secrets need to store in vault [https://www.vaultproject.io/] which is a Tool for Managing Secrets.
This library is designed to process this scenario, load secret along with consul.

## How to use ##
        @Provides
    ConfigurationProvider provideConsulConfiguration() {
    	// rewrite new VaultRepository implement if DefaultVaultRepository is not satisfied requirement      
        VaultRepository vaultRepository=new DefaultVaultRepository();
        vaultRepository.setConsulEnvironment(config.getString(CONSUL_ENV_KEY));
        ConfigurationSource source = new ConsulVaultConfigurationSourceBuilder().build(vaultRepository);
        ReloadStrategy reloadStrategy = new PeriodicalReloadStrategy(config.getLong(RELOAD_IN_SECONDS_KEY),TimeUnit.SECONDS);
        // Create provider
        return new ConfigurationProviderBuilder()
                .withConfigurationSource(source)
                .withEnvironment(new ImmutableEnvironment(config.getString(CONSUL_ENV_KEY)))
                .withReloadStrategy(reloadStrategy)
                .build();
    }
