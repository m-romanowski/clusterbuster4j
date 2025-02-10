package dev.marcinromanowski.clusterbuster4j.sql.extension;

import com.querydsl.sql.spatial.PostGISTemplates;

public class PostGISTemplateExtension extends PostGISTemplates {

    public PostGISTemplateExtension() {
        this('\\', false);
    }

    public PostGISTemplateExtension(boolean quote) {
        this('\\', quote);
    }

    public PostGISTemplateExtension(char escape, boolean quote) {
        super(escape, quote);
        this.add(SqlOpsExtension.FIRST, "FIRST({0})");
        this.add(SqlOpsExtension.DBSCAN, "ST_ClusterDBSCAN({0}, {1}, {2}) over ()");
        this.add(SqlOpsExtension.MAKE_ENVELOPE, "ST_MakeEnvelope({0}, {1}, {2}, {3}, {4})");
    }

}
