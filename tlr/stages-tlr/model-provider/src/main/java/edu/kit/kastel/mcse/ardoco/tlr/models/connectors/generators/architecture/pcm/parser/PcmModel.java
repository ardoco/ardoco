/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.fuchss.xmlobjectmapper.XML2Object;
import org.fuchss.xmlobjectmapper.XMLException;

public class PcmModel {

    private PcmRepository repository;

    public PcmModel(File repository) throws IllegalArgumentException {
        try (var inputStream = new FileInputStream(repository)) {
            this.load(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public PcmModel(InputStream repositoryStream) throws IllegalArgumentException {
        this.load(repositoryStream);
    }

    public PcmRepository getRepository() {
        return repository;
    }

    private void load(InputStream repositoryStream) throws IllegalArgumentException {
        try (repositoryStream) {
            XML2Object xml2Object = new XML2Object();
            xml2Object.registerClasses(PcmRepository.class, PcmComponent.class, PcmInterface.class, PcmSignature.class, PcmComponent.InterfaceId.class,
                    PcmDatatype.class, PcmParameter.class, PcmComponent.ComponentId.class);
            repository = xml2Object.parseXML(repositoryStream, PcmRepository.class);
            repository.init();
        } catch (ReflectiveOperationException | IOException | XMLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
