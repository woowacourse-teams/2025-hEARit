package com.onair.hearit.core.infrastructure.elasticsearch;

import java.nio.file.Paths;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.testcontainers.service.connection.Ssl;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@Testcontainers
public abstract class ElasticSearchTestContainer {

    static final String ELASTIC_IMAGE_NAME = "docker.elastic.co/elasticsearch/elasticsearch:8.17.10";

    @Ssl
    @Container
    @ServiceConnection
    static ElasticsearchContainer elasticsearchContainer = createElasticsearchContainer();

    private static ElasticsearchContainer createElasticsearchContainer() {
        String elasticsearchPath = Paths.get(System.getProperty("user.dir"))
                .getParent()
                .resolve("elasticsearch")
                .resolve("analysis")
                .toAbsolutePath()
                .toString();

        ImageFromDockerfile elasticsearchWithNori = new ImageFromDockerfile()
                .withDockerfileFromBuilder(builder -> builder
                        .from(ELASTIC_IMAGE_NAME)
                        .run("bin/elasticsearch-plugin", "install", "--batch", "analysis-nori")
                        .build());

        return new ElasticsearchContainer(DockerImageName.parse(elasticsearchWithNori.get())
                .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch"))
                .withCopyFileToContainer(MountableFile.forHostPath(elasticsearchPath),
                        "/usr/share/elasticsearch/config/analysis")
                .withPassword("test");
    }
}
