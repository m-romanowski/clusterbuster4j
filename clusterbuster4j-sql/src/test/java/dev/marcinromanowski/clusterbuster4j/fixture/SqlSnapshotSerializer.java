package dev.marcinromanowski.clusterbuster4j.fixture;

import au.com.origin.snapshots.Snapshot;
import au.com.origin.snapshots.SnapshotSerializerContext;
import au.com.origin.snapshots.serializers.SnapshotSerializer;
import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;

public class SqlSnapshotSerializer implements SnapshotSerializer {

    @Override
    public Snapshot apply(Object obj, SnapshotSerializerContext gen) {
        final var body = SqlFormatter.of(Dialect.StandardSql)
            .format(obj.toString());
        return gen.toSnapshot(body);
    }

    @Override
    public String getOutputFormat() {
        return "BASIC_SQL";
    }

}
