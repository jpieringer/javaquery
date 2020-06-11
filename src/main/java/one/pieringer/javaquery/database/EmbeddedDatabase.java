package one.pieringer.javaquery.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Objects;

public class EmbeddedDatabase {

    private static final Logger LOG = LogManager.getLogger(EmbeddedDatabase.class);

    private static final String HOST = "localhost";
    private static final int PORT = 7687;

    @Nonnull
    private final Path databaseDirectory;
    @Nonnull
    private final String databaseName;

    public EmbeddedDatabase(@Nonnull final Path databaseDirectory, @Nonnull final String databaseName) {
        this.databaseDirectory = Objects.requireNonNull(databaseDirectory);
        this.databaseName = Objects.requireNonNull(databaseName);
    }

    public AutoCloseable startEmbeddedDatabase() {
        LOG.info("Start startup of embedded database.");

        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(databaseDirectory.toFile())
                .setConfig(BoltConnector.enabled, true)
                .setConfig(BoltConnector.listen_address, new SocketAddress(HOST, PORT))
                .build();

        try {
            managementService.database(databaseName);
            LOG.info("Finished startup of embedded database.");
            return new Database(managementService);
        } catch (Exception e) {
            managementService.shutdown();
            throw e;
        }
    }

    @Nonnull
    public String getDatabaseUri() {
        return "bolt://neo4j:admin@" + HOST + ":" + PORT;
    }

    private static class Database implements AutoCloseable {

        @Nonnull
        private final DatabaseManagementService databaseManagementService;

        public Database(@Nonnull final DatabaseManagementService databaseManagementService) {
            this.databaseManagementService = Objects.requireNonNull(databaseManagementService);
        }

        @Override
        public void close() {
            LOG.info("Start shutdown of embedded database.");
            databaseManagementService.shutdown();
            LOG.info("Finished shutdown of embedded database.");
        }
    }
}
