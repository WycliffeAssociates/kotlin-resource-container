package org.wycliffeassociates.resourcecontainer;

import com.esotericsoftware.yamlbeans.YamlWriter;

import org.wycliffeassociates.resourcecontainer.errors.InvalidRCException;
import org.wycliffeassociates.resourcecontainer.errors.OutdatedRCException;
import org.wycliffeassociates.resourcecontainer.errors.RCException;
import org.wycliffeassociates.resourcecontainer.errors.UnsupportedRCException;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handling the creation and loading of RCs
 */
public class Factory {

    public static final String conformsTo = "0.2";

    public ResourceContainer load(File dir) throws Exception {
        return load(dir, true);
    }

    /**
     * Loads a resource container from the disk.
     *
     * When strict mode is enabled this will throw and exception if validation fails.
     *
     * @param dir the RC directory
     * @param strict When false the RC  will not be validated.
     * @return the loaded RC
     * @throws Exception
     */
    public ResourceContainer load(File dir, boolean strict) throws Exception {
        ResourceContainer rc = new ResourceContainer(dir);

        if(strict) {
            if(rc.manifest.isNull()) {
                throw new InvalidRCException("Missing manifest.yaml");
            }
            if(rc.conformsTo() == null) {
                throw new InvalidRCException("Missing dublin_core.conformsto");
            }
            if(Semver.gt(rc.conformsTo(), conformsTo)) {
                throw new UnsupportedRCException("Found " + rc.conformsTo() + " but expected " + conformsTo);
            }
            if(Semver.lt(rc.conformsTo(), conformsTo)) {
                throw new OutdatedRCException("Found " + rc.conformsTo() + " but expected " + conformsTo);
            }
        }

        return rc;
    }

    /**
     * Creates a brand new resource container
     * @param dir the RC directory
     * @param manifest the manifest
     * @return the new RC
     * @throws Exception
     */
    public ResourceContainer create(File dir, HashMap<String, Object> manifest) throws Exception {
        if(dir.exists()) throw new RCException("Resource container already exists");
        ObjectReader reader = new ObjectReader(manifest);

        // default values
        Map<String, Object> dublinCore = new HashMap<>();
        dublinCore.put("type", "");
        dublinCore.put("conformsto", "rc" + conformsTo);
        dublinCore.put("format", "");
        dublinCore.put("identifier", "");
        dublinCore.put("title", "");
        dublinCore.put("subject", "");
        dublinCore.put("description", "");
        dublinCore.put("language", new HashMap());
        dublinCore.put("source", new ArrayList());
        dublinCore.put("rights", "");
        dublinCore.put("creator", "");
        dublinCore.put("contributor", new ArrayList());
        dublinCore.put("relation", new ArrayList());
        dublinCore.put("publisher", "");
        dublinCore.put("issued", "");
        dublinCore.put("modified", "");
        dublinCore.put("version", "");

        Map<String, Object> checking = new HashMap<>();
        checking.put("checking_entitiy", new ArrayList());
        checking.put("checking_level", "");

        List projects = new ArrayList();

        // validate user input
        if(reader.get("dublin_core").get("type").isNull()) {
            throw new InvalidRCException("Missing dublin_core.type");
        }
        if(reader.get("dublin_core").get("format").isNull()) {
            throw new InvalidRCException("Missing dublin_core.format");
        }
        if(reader.get("dublin_core").get("identifier").isNull()) {
            throw new InvalidRCException("Missing dublin_core.identifier");
        }
        if(reader.get("dublin_core").get("language").isNull()) {
            throw new InvalidRCException("Missing dublin_core.language");
        }
        if(reader.get("dublin_core").get("rights").isNull()) {
            throw new InvalidRCException("Missing dublin_core.rights");
        }

        // merge defaults
        dublinCore.putAll((Map)reader.get("dublin_core").value());
        if(!reader.get("checking").isNull()) {
            checking.putAll((Map) reader.get("checking").value());
        }
        if(!reader.get("projects").isNull()) {
            projects.addAll((List) reader.get("projects").value());
        }

        HashMap newManifest = new HashMap();
        newManifest.put("dublin_core", dublinCore);
        newManifest.put("checking", checking);
        newManifest.put("projects", projects);

        // build dirs and write manifest
        dir.mkdirs();
        File manifestFile = new File(dir, "manifest.yaml");
        YamlWriter yamlWriter = new YamlWriter(new FileWriter(manifestFile));
        yamlWriter.write(newManifest);
        yamlWriter.close();

        return new ResourceContainer(dir);
    }

}
