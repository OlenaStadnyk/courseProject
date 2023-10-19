package com.example.tests;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

public class DockerConfig {

    private static final DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder().build();
    private static final DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

    public static DockerClient getDockerClient() {
        return dockerClient;
    }
}
