package org.wycliffeassociates.resourcecontainer;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import org.wycliffeassociates.resourcecontainer.errors.RCException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an instance of a resource container.
 */
public class ResourceContainer {

    /**
     * Returns the path to the resource container directory
     */
    public final File path;

    /**
     * Returns the resource container package information.
     * This is the package.json file
     */
    public ObjectReader manifest = null;

    /**
     * Instantiates a new resource container object
     *
     * @param dir the directory of the resource container
     * @throws Exception
     */
    public ResourceContainer(File dir) throws Exception {
        this.path = dir;

        File manifestFile = new File(dir, "manifest.yaml");
        if(manifestFile.exists()) {
            YamlReader reader = new YamlReader(new FileReader(manifestFile));
            this.manifest = new ObjectReader(reader.read());
        } else {
            this.manifest = new ObjectReader(null);
        }
    }

    /**
     * Retrieves the language information from the manifest
     * @return the language information
     */
    public ObjectReader language() {
        return this.manifest.get("dublin_core").get("language");
    }

    /**
     * Retrieves the resource information from the manifest.
     *
     * @return the resource information
     */
    public ObjectReader resource () {
        String slug = (String)this.manifest.get("dublin_core").get("identifier").value();
        String title = (String)this.manifest.get("dublin_core").get("title").value();
        String type = (String)this.manifest.get("dublin_core").get("type").value();
        String checkingLevel = (String)this.manifest.get("checking").get("checking_level").value();
        String version = (String)this.manifest.get("dublin_core").get("version").value();

        Map<String, Object> map = new HashMap<>();
        map.put("slug", slug);
        map.put("title", title);
        map.put("type", type);
        map.put("checkingLevel", checkingLevel);
        map.put("version", version);
        return new ObjectReader(map);
    }

    /**
     * Retrieves the project's details from the RC.
     *
     * If this RC contains multiple projects use project(identifier) instead
     *
     * @return the project information
     * @throws RCException if there are multiple projects.
     */
    public Object project() throws RCException {
        return project(null);
    }

    /**
     * Retrieves a project's details from the RC.
     *
     * @param identifier the project to be retrieved. This can be null if there is only one project
     * @return the project information
     * @throws RCException if there are multiple projects and no identifier is given
     */
    public ObjectReader project(String identifier) throws RCException {
        if(this.manifest.get("projects").size() == 0) return null;

        if(identifier != null && !identifier.isEmpty()) {
            // look up project
            for(Object project:(List)this.manifest.get("projects").value()) {
                ObjectReader p = new ObjectReader(project);
                if(p.get("identifier").value().equals(identifier)) {
                    return p;
                }
            }
        } else if(this.manifest.get("projects").size() == 1) {
            return this.manifest.get("projects").get(0);
        } else if(this.manifest.get("projects").size() > 1) {
            throw new RCException("Multiple projects found. Specify the project identifier.");
        }

        return null;
    }

    /**
     * Returns a list of project identifiers in this rc.
     * @return a list of project identifiers
     */
    public List<String> projectIds() {
        List<String> list = new ArrayList<>();
        for(Object project:(List)this.manifest.get("projects").value()) {
            ObjectReader p = new ObjectReader(project);
            list.add((String)p.get("identifier").value());
        }
        return list;
    }

    /**
     * Returns the number of projects contained in this RC.
     *
     * @return the project count
     */
    public int projectCount() {
        return this.manifest.get("projects").size();
    }

    /**
     * Returns the version of the RC spec used in this container.
     * This will strip off the 'rc' prefix.
     *
     * @return the RC version e.g. '0.2'
     */
    public String conformsTo() {
        String value = (String)this.manifest.get("dublin_core").get("conformsto").value();
        if(value != null) {
            return value.replaceAll("^rc", "");
        }
        return null;
    }

    /**
     * Returns an un-ordered list of chapter slugs in this resource container
     *
     * @return an array of chapter identifiers
     */
    public String[] chapters() throws Exception {
        return chapters(null);
    }

    /**
     * Returns an un-ordered list of chapter slugs in this resource container
     *
     * @param projectIdentifier the project who's chapters will be returned
     * @return an array of chapter identifiers
     */
    public String[] chapters(String projectIdentifier) throws Exception {
        ObjectReader p = project(projectIdentifier);
        if(p == null) return new String[]{};

        File contentPath = new File(path, (String)p.get("path").value());
        String[] chapters = contentPath.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return new File(dir, filename).isDirectory();
            }
        });
        if(chapters == null) chapters = new String[0];
        return chapters;
    }

    /**
     * Returns an un-ordered list of chunk slugs in the chapter
     *
     * @param chapterSlug the chapter who's chunks will be returned
     * @return an array of chunk identifiers
     */
    public String[] chunks(String chapterSlug) throws Exception {
        return chunks(null, chapterSlug);
    }

    /**
     * Returns an un-ordered list of chunk slugs in the chapter
     *
     * @param projectIdentifier the project who's chunks will be returned
     * @param chapterSlug the chapter who's chunks will be returned
     * @return an array of chunk identifiers
     */
    public String[] chunks(String projectIdentifier, String chapterSlug) throws Exception {
        ObjectReader p = project(projectIdentifier);
        if(p == null) return new String[]{};

        File contentDir = new File(path, (String)p.get("path").value());
        File chapterDir = new File(contentDir, chapterSlug);
        final List<String> chunks = new ArrayList<>();
        chapterDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                chunks.add(filename.split("\\.")[0]);
                return false;
            }
        });
        return chunks.toArray(new String[chunks.size()]);
    }

    /**
     * Returns the contents of a chunk.
     *
     * @param chapterSlug the chapter who's chunk will be read
     * @param chunkSlug the contents of the chunk or an empty string if it does not exist
     * @return the chunk contents
     */
    public String readChunk(String chapterSlug, String chunkSlug) throws Exception {
        return readChunk(null, chapterSlug, chunkSlug);
    }

    /**
     * Returns the contents of a chunk.
     *
     * @param projectIdentifier the project who's chunk will be read
     * @param chapterSlug the chapter who's chunk will be read
     * @param chunkSlug the contents of the chunk or an empty string if it does not exist
     * @return the chunk contents
     */
    public String readChunk(String projectIdentifier, String chapterSlug, String chunkSlug) throws Exception {
        ObjectReader p = project(projectIdentifier);
        if(p == null) return "";

        File contentDir = new File(path, (String)p.get("path").value());
        File chunkFile = new File(new File(contentDir, chapterSlug), chunkSlug + "." + chunkExt());
        if(chunkFile.exists() && chunkFile.isFile()) {
            return FileUtil.readFileToString(chunkFile);
        }
        return "";
    }

    /**
     * Writes content to a chunk.
     * The path will be created if it does not already exist.
     *
     * @param chapterIdentifier the chapter who's chunk will be written to
     * @param chunkIdentifier the chunk that will be created
     * @param content the content to be written to the chunk
     * @throws Exception
     */
    public void writeChunk(String chapterIdentifier, String chunkIdentifier, String content) throws Exception {
        writeChunk(null, chapterIdentifier, chunkIdentifier, content);
    }

    /**
     * Writes content to a chunk.
     * The path will be created if it does not already exist.
     *
     * @param projectIdentifier the project who's chunk will be written to
     * @param chapterIdentifier the chapter who's chunk will be written to
     * @param chunkIdentifier the chunk that will be created
     * @param content the content to be written to the chunk
     * @throws Exception
     */
    public void writeChunk(String projectIdentifier, String chapterIdentifier, String chunkIdentifier, String content) throws Exception {
        ObjectReader p = project(projectIdentifier);
        if(p == null) return;

        File contentDir = new File(path, (String)p.get("path").value());
        File chunkFile = new File(new File(contentDir, chapterIdentifier), chunkIdentifier + "." + chunkExt());
        if(content.isEmpty()) {
            FileUtil.deleteQuietly(chunkFile);
        } else {
            chunkFile.getParentFile().mkdirs();
            FileUtil.writeStringToFile(chunkFile, content);
        }
    }

    /**
     * Returns the file extension to use for content files (chunks)
     * @return the extension name
     */
    private String chunkExt() {
        // TODO: 1/26/17 I'd rather not hard code the file extensions in here.
        // it would be better if the library can just figure it out.
        String defaultExt = "txt";
        switch ((String)manifest.get("dublin_core").get("format").value()) {
            case "text/usx":
                return "usx";
            case "text/usfm":
                return "usfm";
            case "text/markdown":
                return "md";
            case "audio/mp3":
                return "mp3";
            case "video/mp4":
                return "mp4";
            default:
                // unknown format
                return defaultExt;
        }
    }

    /**
     * Convenience method to get the type of the resource container.
     *
     * @return the RC type
     */
    public String type() {
        return (String)this.manifest.get("dublin_core").get("type").value();
    }

    /**
     * Returns the resource container data configuration.
     * This is the config.yaml files under the project content directory.
     *
     * @return the config information
     */
    public ObjectReader config() throws RCException {
        return config(null);
    }

    /**
     * Returns the project configuration
     * This is the config.yaml files under the project content directory.
     *
     * @param projectIdentifier the project who's config will be returned
     * @return the config information
     */
    public ObjectReader config(String projectIdentifier) throws RCException {
        ObjectReader p = project(projectIdentifier);
        if(p == null) return null;

        File contentDir = new File(path, (String)p.get("path").value());
        File configFile = new File(contentDir, "config.yaml");
        if(configFile.exists()) {
            try {
                YamlReader reader = new YamlReader(new FileReader(configFile));
                return new ObjectReader(reader.read());
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return new ObjectReader(null);
    }

    /**
     * Returns the project table of contents
     * This is the toc.yaml files under the project content directory.
     *
     * @return the toc information
     * @throws RCException
     */
    public ObjectReader toc() throws RCException {
        return toc(null);
    }

    /**
     * Returns the project table of contents
     * This is the toc.yaml files under the project content directory.
     *
     * @param projectIdentifier the project who's toc will be returned
     * @return the toc information
     */
    public ObjectReader toc(String projectIdentifier) throws RCException {
        ObjectReader p = project(projectIdentifier);
        if(p == null) return null;

        File contentDir = new File(path, (String)p.get("path").value());
        File tocFile = new File(contentDir, "toc.yaml");
        if(tocFile.exists()) {
            try {
                YamlReader reader = new YamlReader(new FileReader(tocFile));
                return new ObjectReader(reader.read());
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return new ObjectReader(null);
    }

    /**
     * Creates a toc yaml for the project
     *
     * @param content the toc to be written. If empty the toc will be removed
     */
    public void writeTOC(String content) throws Exception {
        writeTOC(null, content);
    }

    /**
     * Creates a toc yaml for the project
     * @param projectIdentifier the project who's toc will be written.
     * @param content the toc to be written. If empty the toc will be removed
     * @throws Exception
     */
    public void writeTOC(String projectIdentifier, String content) throws Exception {
        ObjectReader p = project(projectIdentifier);
        if(p == null) return;

        File contentDir = new File(path, (String)p.get("path").value());
        File tocFile = new File(contentDir, "toc.yaml");
        if(content == null || content.isEmpty()) {
            FileUtil.deleteQuietly(tocFile);
        } else {
            tocFile.getParentFile().mkdirs();
            YamlWriter yamlWriter = new YamlWriter(new FileWriter(tocFile));
            yamlWriter.write(content);
            yamlWriter.close();
        }
    }

    /**
     * Creates a config yaml for the project
     *
     * @param content the config to be written. If empty the config will be removed
     */
    public void writeConfig(String content) throws Exception {
        writeConfig(null, content);
    }

    /**
     * Creates a config yaml for the project
     * @param projectIdentifier the project who's config will be written.
     * @param content the config to be written. If empty the config will be removed
     * @throws Exception
     */
    public void writeConfig(String projectIdentifier, String content) throws Exception {
        ObjectReader p = project(projectIdentifier);
        if(p == null) return;

        File contentDir = new File(path, (String)p.get("path").value());
        File configFile = new File(contentDir, "config.yaml");
        if(content == null || content.isEmpty()) {
            FileUtil.deleteQuietly(configFile);
        } else {
            configFile.getParentFile().mkdirs();
            YamlWriter yamlWriter = new YamlWriter(new FileWriter(configFile));
            yamlWriter.write(content);
            yamlWriter.close();
        }
    }
}
