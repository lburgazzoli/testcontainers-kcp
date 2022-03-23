package com.github.lburgazzoli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.SelinuxContext;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class KcpContainer extends GenericContainer<KcpContainer> {
    public static final DockerImageName IMAGE = DockerImageName.parse("ghcr.io/kcp-dev/kcp");
    public static final String IMAGE_TAG = "latest";
    public static final int KCP_PORT = 6443;
    public static final String KCP_USER_TOKEN = "tc-token";
    public static final String KCP_USER = "tc";

    private final List<Path> filesToRemove;

    public KcpContainer() {
        this(IMAGE.withTag(IMAGE_TAG));
    }

    public KcpContainer(DockerImageName imageName) {
        super(imageName);

        this.filesToRemove = new ArrayList<>();
    }

    @Override
    protected void configure() {
        withExposedPorts(KCP_PORT);
        waitingFor(Wait.forListeningPort());
        withCommand(
            "/kcp",
            "start",
            "--root-directory",
            "/tmp/kcp",
            "--token-auth-file",
            "/tmp/kcp-token");

        addFileSystemBind(
            "/tmp/kcp-token",
            String.join(",", KCP_USER_TOKEN, KCP_USER, UUID.randomUUID().toString(), "system:masters"));
    }

    @Override
    public void stop() {
        super.stop();

        for (Path path: filesToRemove) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
               // ignored
            }
        }
    }

    private void addFileSystemBind(String containerPath, String content) {
        var fp = PosixFilePermissions.fromString("rw-rw-rw-");

        try {
            Path f = Files.createTempFile(null, null, PosixFilePermissions.asFileAttribute(fp));
            Files.write(f, content.getBytes(StandardCharsets.UTF_8));

            addFileSystemBind(
                f.toAbsolutePath().toString(),
                containerPath,
                BindMode.READ_ONLY,
                SelinuxContext.SHARED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getMasterUrl() {
        return "https://" + getContainerIpAddress() + ":" + getMappedPort(KCP_PORT);
    }

    public String getToken() {
        return KCP_USER_TOKEN;
    }
}
