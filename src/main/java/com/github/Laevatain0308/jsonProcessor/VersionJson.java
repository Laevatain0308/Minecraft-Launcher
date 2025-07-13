package com.github.Laevatain0308.jsonProcessor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionJson
{
    private AssetIndex assetIndex;
    private String assets;
    private Downloads downloads;
    private String id;
    private JavaVersion javaVersion;
    private List<Library> libraries;
    private Logging logging;
    private String mainClass;
    private String minecraftArguments;
    private int minimumLauncherVersion;
    private String releaseTime;
    private String time;
    private String type;
    private String clientVersion;


    public void setAssetIndex(AssetIndex assetIndex) { this.assetIndex = assetIndex; }
    public AssetIndex getAssetIndex() { return assetIndex; }

    public void setAssets(String assets) { this.assets = assets; }
    public String getAssets() { return assets; }

    public void setDownloads(Downloads downloads) { this.downloads = downloads; }
    public Downloads getDownloads() { return downloads; }

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }

    public void setJavaVersion(JavaVersion javaVersion) { this.javaVersion = javaVersion; }
    public JavaVersion getJavaVersion() { return javaVersion; }

    public void setLibraries(List<Library> libraries) { this.libraries = libraries; }
    public List<Library> getLibraries() { return libraries; }

    public void setLogging(Logging logging) { this.logging = logging; }
    public Logging getLogging() { return logging; }

    public void setMainClass(String mainClass) { this.mainClass = mainClass; }
    public String getMainClass() { return mainClass; }

    public void setMinecraftArguments(String minecraftArguments) { this.minecraftArguments = minecraftArguments; }
    public String getMinecraftArguments() { return minecraftArguments; }

    public void setMinimumLauncherVersion(int minimumLauncherVersion) { this.minimumLauncherVersion = minimumLauncherVersion; }
    public int getMinimumLauncherVersion() { return minimumLauncherVersion; }

    public void setReleaseTime(String releaseTime) { this.releaseTime = releaseTime; }
    public String getReleaseTime() { return releaseTime; }

    public void setTime(String time) { this.time = time; }
    public String getTime() { return time; }

    public void setType(String type) { this.type = type; }
    public String getType() { return type; }

    public void setClientVersion(String clientVersion) { this.clientVersion = clientVersion; }
    public String getClientVersion() { return clientVersion; }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssetIndex
    {
        private String id;
        private String sha1;
        private int size;
        private int totalSize;
        private String url;


        public void setId(String id) { this.id = id; }
        public String getId() { return id; }

        public void setSha1(String sha1) { this.sha1 = sha1; }
        public String getSha1() { return sha1; }

        public void setSize(int size) { this.size = size; }
        public int getSize() { return size; }

        public void setTotalSize(int totalSize) { this.totalSize = totalSize; }
        public int getTotalSize() { return totalSize; }

        public void setUrl(String url) { this.url = url; }
        public String getUrl() { return url; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Downloads
    {
        private Artifact client;
        private Artifact server;


        public void setClient(Artifact client) { this.client = client; }
        public Artifact getClient() { return client; }

        public void setServer(Artifact server) { this.server = server; }
        public Artifact getServer() { return server; }


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Artifact
        {
            private String sha1;
            private int size;
            private String url;


            public void setSha1(String sha1) { this.sha1 = sha1; }
            public String getSha1() { return sha1; }

            public void setSize(int size) { this.size = size; }
            public int getSize() { return size; }

            public void setUrl(String url) { this.url = url; }
            public String getUrl() { return url; }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JavaVersion
    {
        private String component;
        private int majorVersion;


        public void setComponent(String component) { this.component = component; }
        public String getComponent() { return component; }

        public void setMajorVersion(int majorVersion) { this.majorVersion = majorVersion; }
        public int getMajorVersion() { return majorVersion; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Library
    {
        private Downloads downloads;
        private Extract extract;
        private String name;
        private Map<String , String> natives;
        private List<Rule> rules;


        public void setDownloads(Downloads downloads) { this.downloads = downloads; }
        public Downloads getDownloads() { return downloads; }

        public void setExtract(Extract extract) { this.extract = extract; }
        public Extract getExtract() { return extract; }

        public void setName(String name) { this.name = name; }
        public String getName() { return name; }

        public void setNatives(Map<String, String> natives) { this.natives = natives; }
        public Map<String, String> getNatives() { return natives; }

        public void setRules(List<Rule> rules) { this.rules = rules; }
        public List<Rule> getRules() { return rules; }


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Downloads
        {
            private Artifact artifact;
            private Map<String, Artifact> classifiers;


            public void setArtifact(Artifact artifact) { this.artifact = artifact; }
            public Artifact getArtifact() { return artifact; }

            public void setClassifiers(Map<String, Artifact> classifiers) { this.classifiers = classifiers; }
            public Map<String, Artifact> getClassifiers() { return classifiers; }


            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Artifact
            {
                private String path;
                private String sha1;
                private int size;
                private String url;


                public void setPath(String path) { this.path = path; }
                public String getPath() { return path; }

                public void setSha1(String sha1) { this.sha1 = sha1; }
                public String getSha1() { return sha1; }

                public void setSize(int size) { this.size = size; }
                public int getSize() { return size; }

                public void setUrl(String url) { this.url = url; }
                public String getUrl() { return url; }
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Rule
        {
            private String action;
            private OS os;


            public void setAction(String action) { this.action = action; }
            public String getAction() { return action; }

            public void setOs(OS os) { this.os = os; }
            public OS getOs() { return os; }


            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class OS
            {
                private String name;
                private String arch;
                private String version;


                public void setName(String name) { this.name = name; }
                public String getName() { return name; }

                public void setArch(String arch) { this.arch = arch; }
                public String getArch() { return arch; }

                public void setVersion(String version) { this.version = version; }
                public String getVersion() { return version; }
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Extract
        {
            private List<String> exclude;


            public void setExclude(List<String> exclude) { this.exclude = exclude; }
            public List<String> getExclude() { return exclude; }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Logging
    {
        private Client client;


        public void setClient(Client client) { this.client = client; }
        public Client getClient() { return client; }


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Client
        {
            private String argument;
            private File file;
            private String type;


            public void setArgument(String argument) { this.argument = argument; }
            public String getArgument() { return argument; }

            public void setFile(File file) { this.file = file; }
            public File getFile() { return file; }

            public void setType(String type) { this.type = type; }
            public String getType() { return type; }


            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class File
            {
                private String id;
                private String sha1;
                private int size;
                private String url;

                public void setId(String id) { this.id = id; }
                public String getId() { return id; }

                public void setSha1(String sha1) { this.sha1 = sha1; }
                public String getSha1() { return sha1; }

                public void setSize(int size) { this.size = size; }
                public int getSize() { return size; }

                public void setUrl(String url) { this.url = url; }
                public String getUrl() { return url; }
            }
        }
    }
}
