package edu.kit.kastel.mcse.ardoco.core.api.text;

import java.io.Serializable;
import java.util.List;

public interface SimpleText extends Serializable {

    String getText();
    
    List<String> getLines();

}
