package io.zeebe.zeeqs

import org.testcontainers.utility.DockerImageName

object ZeebeTestcontainerUtil {

    val ZEEBE_IMAGE_NAME = "ghcr.io/camunda-community-hub/zeebe-with-hazelcast-exporter:8.1.5"

    val ZEEBE_DOCKER_IMAGE = DockerImageName.parse(ZEEBE_IMAGE_NAME)

}