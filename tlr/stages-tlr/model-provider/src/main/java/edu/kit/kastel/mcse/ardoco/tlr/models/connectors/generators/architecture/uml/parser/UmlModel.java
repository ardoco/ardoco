/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.fuchss.xmlobjectmapper.XML2Object;
import org.fuchss.xmlobjectmapper.XMLException;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements.OwnedOperation;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements.PackagedElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements.Reference;

public class UmlModel {

    private final UmlModelRoot model;

    public UmlModel(File umlModel) throws IllegalArgumentException {
        try (var inputStream = new FileInputStream(umlModel)) {
            this.model = load(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public UmlModel(InputStream umlModelStream) throws IllegalArgumentException {
        this.model = load(umlModelStream);
    }

    public UmlModelRoot getModel() {
        return model;
    }

    private static UmlModelRoot load(InputStream repositoryStream) throws IllegalArgumentException {
        try (repositoryStream) {
            XML2Object xml2Object = new XML2Object();
            xml2Object.registerClasses(UmlModelRoot.class, PackagedElement.class, OwnedOperation.class, Reference.class);
            UmlModelRoot model = xml2Object.parseXML(repositoryStream, UmlModelRoot.class);
            model.init();
            return model;
        } catch (ReflectiveOperationException | IOException | XMLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
