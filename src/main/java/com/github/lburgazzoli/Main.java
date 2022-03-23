package com.github.lburgazzoli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        KcpContainer kcp = new KcpContainer();
        kcp.start();

        Config config = new ConfigBuilder()
            .withMasterUrl(kcp.getMasterUrl())
            .withOauthToken(kcp.getToken())
            .withTrustCerts(true)
            .build();

        KubernetesClient client = new DefaultKubernetesClient(config);
        LOGGER.info("create {}", client.namespaces().create(new NamespaceBuilder().withNewMetadata().withName("foo").endMetadata().build()));
        LOGGER.info("get    {}", client.namespaces().list());
    }
}
