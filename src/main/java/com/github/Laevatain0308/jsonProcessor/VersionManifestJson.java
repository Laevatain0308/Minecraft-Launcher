package com.github.Laevatain0308.jsonProcessor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionManifestJson
{
    private Latest latest;
    private List<Version> versions;


    public void setLatest(Latest latest) { this.latest = latest; }
    public Latest getLatest() { return latest; }

    public void setVersions(List<Version> versions) { this.versions = versions; }
    public List<Version> getVersions() { return versions; }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Latest
    {
        private String release;
        private String snapshot;


        public void setRelease(String release) { this.release = release; }
        public String getRelease() { return release; }

        public void setSnapshot(String snapshot) { this.snapshot = snapshot; }
        public String getSnapshot() { return snapshot; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Version
    {
        private String id;
        private String type;
        private String url;
        private String time;
        private String releaseTime;


        public void setId(String id) { this.id = id; }
        public String getId() { return id; }

        public void setType(String type) { this.type = type; }
        public String getType() { return type; }

        public void setUrl(String url) { this.url = url; }
        public String getUrl() { return url; }

        public void setTime(String time) { this.time = time; }
        public String getTime() { return time; }

        public void setReleaseTime(String releaseTime) { this.releaseTime = releaseTime; }
        public String getReleaseTime() { return releaseTime; }
    }
}
